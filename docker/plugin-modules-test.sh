#!/usr/bin/env bash
# Test for plugin-modules.sh: commented-out modules (single-line, multi-line, inline) must not count.
# Usage: docker/plugin-modules-test.sh   (exit code 0 = pass)
set -euo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
tmp="$(mktemp)"
trap 'rm -f "$tmp"' EXIT

cat > "$tmp" <<'POM'
<project>
  <modules>
    <module>Alpha</module>
    <!-- <module>SingleLineOff</module> -->
    <!--
      <module>MultiLineOff</module>
      <module>MultiLineOff2</module>
    -->
    <module>Beta</module> <!-- trailing note -->
    <!-- start of block
    <module>OffAgain</module>
    end of block --> <module>Gamma</module>
  </modules>
</project>
POM

actual="$("$here/plugin-modules.sh" "$tmp")"
expected=$'Alpha\nBeta\nGamma'
if [ "$actual" != "$expected" ]; then
    echo "FAIL: expected [$expected] but got [$actual]" >&2
    exit 1
fi
echo "plugin-modules-test: OK"
