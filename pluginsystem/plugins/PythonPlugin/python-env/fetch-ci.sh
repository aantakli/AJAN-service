#!/usr/bin/env bash
# Downloads nix_env.zip and win_env.zip from the latest successful ci-branch run on the
# current branch (or $1) into dist/. Needs the GitHub CLI (gh) authenticated.
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BRANCH="${1:-$(git -C "$HERE" rev-parse --abbrev-ref HEAD)}"
RUN_ID="$(gh run list --workflow ci-branch.yml --branch "$BRANCH" --status success --limit 1 --json databaseId --jq '.[0].databaseId')"
if [ -z "$RUN_ID" ] || [ "$RUN_ID" = "null" ]; then
  echo "No successful ci-branch run found for branch $BRANCH" >&2; exit 1
fi
echo "Using run $RUN_ID on $BRANCH"
mkdir -p "$HERE/dist"
gh run download "$RUN_ID" -n python-env-linux -D "$HERE/dist"
gh run download "$RUN_ID" -n python-env-windows -D "$HERE/dist"
ls -l "$HERE/dist"
