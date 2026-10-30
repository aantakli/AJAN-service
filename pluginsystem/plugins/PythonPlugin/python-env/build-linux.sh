#!/usr/bin/env bash
# Builds dist/nix_env.zip (portable CPython + pinned packages incl. jep) for Linux x86_64.
# Usage: ./build-linux.sh [outdir]   (default: ./dist). Run from the host (needs Docker);
# the build itself is re-run inside eclipse-temurin:25-jdk-noble.
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE="eclipse-temurin:25-jdk-noble"

if [ "${AJAN_PYENV_IN_CONTAINER:-}" != "1" ]; then
  OUT="${1:-dist}"
  mkdir -p "$HERE/dist"
  # MSYS_NO_PATHCONV stops Git Bash on Windows from mangling the container paths.
  MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*' docker run --rm \
    -e AJAN_PYENV_IN_CONTAINER=1 \
    -e HOST_UID="$(id -u 2>/dev/null || echo 0)" -e HOST_GID="$(id -g 2>/dev/null || echo 0)" \
    -v "$(cd "$HERE" && (pwd -W 2>/dev/null || pwd)):/work" \
    "$IMAGE" bash /work/build-linux.sh "/work/${OUT#/work/}"
  echo "Done: $HERE/${OUT}/nix_env.zip"
  exit 0
fi

# ---- inside the container ----
OUTDIR="${1:-/work/dist}"
cd /work
prop() { grep -E "^$1=" python-env.properties | head -1 | cut -d= -f2- | tr -d '\r'; }
TAG="$(prop pbs.tag)"; PYVER="$(prop python.version)"; SHA="$(prop sha256.linux)"
TARBALL="cpython-${PYVER}+${TAG}-x86_64-unknown-linux-gnu-install_only.tar.gz"
URL="https://github.com/astral-sh/python-build-standalone/releases/download/${TAG}/${TARBALL/+/%2B}"

export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq --no-install-recommends build-essential curl zip ca-certificates >/dev/null

BUILD=/tmp/build; rm -rf "$BUILD"; mkdir -p "$BUILD"; cd "$BUILD"
curl -fsSL -o "$TARBALL" "$URL"
echo "${SHA}  ${TARBALL}" | sha256sum -c -
tar xzf "$TARBALL"            # -> python/
mv python python-portable

export JAVA_HOME=/opt/java/openjdk
# PBS sysconfig names clang as the compiler; use the gcc from build-essential instead.
export CC=gcc
# PBS sysconfig points LIBDIR at /install/lib; link jep against the unpacked libpython instead.
export LIBRARY_PATH="$BUILD/python-portable/lib"
PY=python-portable/bin/python3
"$PY" -m pip install --no-cache-dir --require-hashes -r /work/requirements.lock

# smoke test
"$PY" -c "import rdflib, asyncua, orjson, aiofiles, pytz; print('imports ok')"
SP="$(echo python-portable/lib/python3.*/site-packages)"
test -f "$SP/jep/libjep.so" && echo "libjep.so present"
ls python-portable/lib/libpython3.13.so* >/dev/null

# slim down (install_only is unstripped; drop debug info from shared objects)
find python-portable -name '*.so*' -type f -exec strip --strip-unneeded {} + 2>/dev/null || true
find python-portable -name __pycache__ -type d -prune -exec rm -rf {} +
find python-portable -name '*.a' -delete
rm -rf python-portable/lib/python3.*/test python-portable/lib/python3.*/idlelib \
\
       python-portable/include python-portable/share
"$PY" -c "import rdflib, asyncua, orjson, aiofiles, pytz"
find python-portable -name __pycache__ -type d -prune -exec rm -rf {} +

mkdir -p "$OUTDIR"; rm -f "$OUTDIR/nix_env.zip"
zip -r -X -q "$OUTDIR/nix_env.zip" python-portable
chown "${HOST_UID:-0}:${HOST_GID:-0}" "$OUTDIR/nix_env.zip" 2>/dev/null || true
ls -l "$OUTDIR/nix_env.zip"
