#!/bin/bash

REQUIRED_PACKAGES=(
  build-essential
  libssl-dev
  zlib1g-dev
  libbz2-dev
  libreadline-dev
  libsqlite3-dev
  libffi-dev
  liblzma-dev
  libncurses5-dev
  libgdbm-dev
  libnss3-dev
  uuid-dev
  tk-dev
  libxml2-dev
  libxmlsec1-dev
  zip
)

MISSING=()

for pkg in "${REQUIRED_PACKAGES[@]}"; do
  if [ "$pkg" = "libncurses5-dev" ]; then
    dpkg -s libncurses5-dev &> /dev/null || dpkg -s libncurses-dev &> /dev/null || MISSING+=("$pkg")
  else
    dpkg -s "$pkg" &> /dev/null || MISSING+=("$pkg")
  fi
done

if [ ${#MISSING[@]} -eq 0 ]; then
  echo "All required packages are installed."
else
  echo "Missing packages:"
  for pkg in "${MISSING[@]}"; do
    echo "  $pkg"
  done
  echo
  echo "To install them, run:"
  echo "sudo apt-get update && sudo apt-get install -y ${MISSING[*]}"
  exit 1
fi

set -e

if [ $# -lt 1 ]; then
  echo "Usage: $0 <python-tarball-url> [requirements.txt]"
  exit 1
fi

rm -rf ./Python*
rm -rf python*

PYTHON_URL="$1"
REQS="${2:-requirements.txt}"

# 1. Download and extract
TARBALL=$(basename "$PYTHON_URL")
wget "$PYTHON_URL"
tar -xzf "$TARBALL"
SRC_DIR=$(tar -tf "$TARBALL" | head -1 | cut -f1 -d"/")

# 2. Configure and build portable Python
cd "$SRC_DIR"
./configure --prefix="$PWD/../python-portable" --enable-shared
make -j$(nproc)
make install
cd ..

# 3. Install required modules
PORTABLE_PY="$PWD/python-portable/bin/python3"
export LD_LIBRARY_PATH=$HOME/python-portable/lib:$LD_LIBRARY_PATH
"$PORTABLE_PY" -m ensurepip
"$PORTABLE_PY" -m pip install --upgrade pip
"$PORTABLE_PY" -m pip install jep
if [ -f "$REQS" ]; then
  "$PORTABLE_PY" -m pip install -r "$REQS"
fi

# 4. Package the portable Python as a zip
if ! command -v zip &> /dev/null; then
  echo "zip utility not found. Please install it (sudo apt-get install zip)."
  exit 1
fi

zip -r nix_env.zip python-portable

rm -rf Python*
rm -rf python*

echo "Portable Python created: nix_env.zip"
