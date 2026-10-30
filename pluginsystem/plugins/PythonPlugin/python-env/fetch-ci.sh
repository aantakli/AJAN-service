#!/usr/bin/env bash
# Downloads nix_env.zip and win_env.zip into dist/ from the latest successful run on the current
# branch (or $1) that still has the artifacts: ci-branch.yml first, then docker-image.yml (master)
# and python-env.yml (manual runs). Needs the GitHub CLI (gh) authenticated.
# The artifact names must match python-env.yml (python-env-linux / python-env-windows).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BRANCH="${1:-$(git -C "$HERE" rev-parse --abbrev-ref HEAD)}"
mkdir -p "$HERE/dist"

for WORKFLOW in ci-branch.yml docker-image.yml python-env.yml; do
  RUN_ID="$(gh run list --workflow "$WORKFLOW" --branch "$BRANCH" --status success --limit 1 \
    --json databaseId --jq '.[0].databaseId // empty')"
  [ -n "$RUN_ID" ] || continue
  echo "Trying $WORKFLOW run $RUN_ID on $BRANCH"
  # gh run download does not overwrite existing files
  rm -f "$HERE/dist/nix_env.zip" "$HERE/dist/win_env.zip"
  if gh run download "$RUN_ID" -n python-env-linux -D "$HERE/dist" \
      && gh run download "$RUN_ID" -n python-env-windows -D "$HERE/dist"; then
    ls -l "$HERE/dist"
    exit 0
  fi
  echo "Run $RUN_ID has no (unexpired) python-env artifacts" >&2
done

echo "No successful run with python-env artifacts found for branch $BRANCH." >&2
echo "Start one with: gh workflow run python-env.yml --ref $BRANCH" >&2
exit 1
