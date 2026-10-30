#!/usr/bin/env bash
# Prints the active <module> names of a Maven pom, one per line, sorted.
# XML comments (<!-- ... -->, single- or multi-line) are removed first, so a commented-out
# module is not reported. Used by assemble-dist.sh; tested by plugin-modules-test.sh.
# Usage: docker/plugin-modules.sh <pom.xml>
set -euo pipefail

if [ "$#" -ne 1 ]; then
    echo "usage: $0 <pom.xml>" >&2
    exit 2
fi

awk '
{
    line = $0; out = ""
    while (length(line) > 0) {
        if (in_comment) {
            i = index(line, "-->")
            if (i == 0) { line = "" } else { line = substr(line, i + 3); in_comment = 0 }
        } else {
            i = index(line, "<!--")
            if (i == 0) { out = out line; line = "" } else { out = out substr(line, 1, i - 1); line = substr(line, i + 4); in_comment = 1 }
        }
    }
    print out
}' "$1" \
    | sed -n 's|.*<module>[[:space:]]*\([^<[:space:]]*\)[[:space:]]*</module>.*|\1|p' | sort
