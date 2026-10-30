# Builds dist\win_env.zip (portable CPython + pinned packages incl. jep) for Windows x86_64.
# Needs: JAVA_HOME (JDK 25), MSVC on PATH (run from a VS dev prompt / ilammy/msvc-dev-cmd), tar.exe.
# Usage: .\build-windows.ps1 [-OutDir dist]
param([string]$OutDir = 'dist')
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'

$here = Split-Path -Parent $MyInvocation.MyCommand.Path
function Get-Prop([string]$name) {
    $line = Get-Content (Join-Path $here 'python-env.properties') | Where-Object { $_ -match "^$([regex]::Escape($name))=" } | Select-Object -First 1
    if (-not $line) { throw "missing property $name" }
    return ($line -split '=', 2)[1].Trim()
}
function Assert-Ok([string]$what) {
    if ($LASTEXITCODE -ne 0) { throw "$what failed with exit code $LASTEXITCODE" }
}

$tag = Get-Prop 'pbs.tag'; $pyver = Get-Prop 'python.version'; $sha = Get-Prop 'sha256.windows'
if (-not $env:JAVA_HOME) { throw 'JAVA_HOME is not set' }
if (-not (Get-Command cl.exe -ErrorAction SilentlyContinue)) { throw 'MSVC (cl.exe) not on PATH' }

$tarball = "cpython-$pyver+$tag-x86_64-pc-windows-msvc-install_only.tar.gz"
$url = "https://github.com/astral-sh/python-build-standalone/releases/download/$tag/" + $tarball.Replace('+', '%2B')

$build = Join-Path ([IO.Path]::GetTempPath()) 'ajan-pyenv-build'
if (Test-Path $build) { Remove-Item -Recurse -Force $build }
New-Item -ItemType Directory -Path $build | Out-Null
Push-Location $build
try {
    Invoke-WebRequest -Uri $url -OutFile $tarball -UseBasicParsing
    $actual = (Get-FileHash -Algorithm SHA256 $tarball).Hash.ToLowerInvariant()
    if ($actual -ne $sha.ToLowerInvariant()) { throw "SHA-256 mismatch for ${tarball}: expected $sha, got $actual" }
    tar -xzf $tarball          # -> python\
    Assert-Ok 'tar'
    Rename-Item python python-portable

    $py = Join-Path $build 'python-portable\python.exe'
    $env:PYTHONUTF8 = '1'
    # jep's sdist build links against python313.lib (libs\) and the JDK; point MSVC at them explicitly.
    $env:LIB = "$build\python-portable\libs;$env:LIB"
    $env:INCLUDE = "$build\python-portable\include;$env:INCLUDE"
    & $py -m pip install --no-cache-dir --require-hashes -r (Join-Path $here 'requirements.lock')
    Assert-Ok 'pip install'

    # smoke test
    & $py -c "import rdflib, asyncua, orjson, aiofiles, pytz; print('imports ok')"
    Assert-Ok 'import smoke test'
    $jepDll = Join-Path $build 'python-portable\Lib\site-packages\jep\jep.dll'
    if (-not (Test-Path $jepDll)) { throw "jep.dll missing: $jepDll" }
    Write-Host 'jep.dll present'
    foreach ($f in 'python.exe', 'python313.dll') {
        if (-not (Test-Path (Join-Path $build "python-portable\$f"))) { throw "$f missing" }
    }

    # slim down
    Get-ChildItem -Path python-portable -Recurse -Directory -Filter __pycache__ -Force | Remove-Item -Recurse -Force
    foreach ($d in 'Lib\test', 'Lib\idlelib', 'include', 'libs', 'tcl', 'Lib\tkinter') {
        $p = Join-Path 'python-portable' $d
        if (Test-Path $p) { Remove-Item -Recurse -Force $p }
    }
    & $py -c "import rdflib, asyncua, orjson, aiofiles, pytz"
    Assert-Ok 'import smoke test after slimming'
    Get-ChildItem -Path python-portable -Recurse -Directory -Filter __pycache__ -Force | Remove-Item -Recurse -Force

    $out = if ([IO.Path]::IsPathRooted($OutDir)) { $OutDir } else { Join-Path $here $OutDir }
    New-Item -ItemType Directory -Force -Path $out | Out-Null
    $zip = Join-Path $out 'win_env.zip'
    if (Test-Path $zip) { Remove-Item -Force $zip }
    Compress-Archive -Path (Join-Path $build 'python-portable') -DestinationPath $zip -CompressionLevel Optimal
    Write-Host ("Done: $zip ({0:N1} MB)" -f ((Get-Item $zip).Length / 1MB))
} finally {
    Pop-Location
}
