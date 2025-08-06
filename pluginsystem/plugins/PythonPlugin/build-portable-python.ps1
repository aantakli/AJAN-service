param(
    [Parameter(Mandatory=$true)]
    [string]$PythonZipUrl,
    [string]$Requirements = "requirements.txt"
)

$ErrorActionPreference = "Stop"

# Clean up previous runs
Remove-Item -Recurse -Force .\python-portable, .\Python*, .\python*.zip, .\get-pip.py, .\win_env.zip, .\jep*.whl -ErrorAction SilentlyContinue

# 1. Download and extract Python embeddable zip
$zipName = Split-Path $PythonZipUrl -Leaf
Invoke-WebRequest -Uri $PythonZipUrl -OutFile $zipName
Expand-Archive -Path $zipName -DestinationPath "python-portable"

# 2. Prepare folders and python._pth for pip
$sitePackages = ".\python-portable\Lib\site-packages"
$scripts = ".\python-portable\Scripts"
New-Item -ItemType Directory -Force -Path $sitePackages | Out-Null
New-Item -ItemType Directory -Force -Path $scripts | Out-Null

# Ensure 'import site' is enabled in python._pth
$pthFile = Get-ChildItem .\python-portable\python*._pth | Select-Object -First 1
if ($pthFile) {
    (Get-Content $pthFile.FullName) | ForEach-Object {
        if ($_ -match "^#?import site") { "import site" } else { $_ }
    } | Set-Content $pthFile.FullName
}

# 3. Download get-pip.py and install pip in portable env
Invoke-WebRequest -Uri "https://bootstrap.pypa.io/get-pip.py" -OutFile "get-pip.py"
& .\python-portable\python.exe get-pip.py --no-warn-script-location

# 4. Use system Python to build jep wheel
python -m pip install --upgrade pip wheel
python -m pip wheel jep

# 5. Install jep wheel into portable env
$jepWheel = Get-ChildItem .\jep*.whl | Select-Object -First 1
if ($jepWheel) {
    & .\python-portable\python.exe -m pip install $jepWheel.FullName
}

# 5b. Copy jep native files from system Python to portable Python (if available)
try {
    $sysJep = & python -c "import jep, os; print(os.path.dirname(jep.__file__))" 2>$null
} catch {
    $sysJep = $null
}
$portJep = Resolve-Path ".\python-portable\Lib\site-packages\jep" -ErrorAction SilentlyContinue
if ($sysJep -and (Test-Path $sysJep) -and $portJep) {
    Copy-Item "$sysJep\*" $portJep -Recurse -Force
}

# 6. Install requirements if present
if (Test-Path $Requirements) {
    & .\python-portable\python.exe -m pip install -r $Requirements
}

# 7. Package as zip (overwrite if exists)
Compress-Archive -Path .\python-portable\* -DestinationPath win_env.zip -Force

# Clean up
Remove-Item -Recurse -Force .\python-portable, .\Python*, .\python*.zip, .\get-pip.py, .\jep*.whl

Write-Host "Portable Python created: win_env.zip"