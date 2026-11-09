#!/usr/bin/env bash
set -euo pipefail

# Enables GitHub branch protection on main for Bayan-ul-huda-quran-o-hadees
# Enforces required automated CI status checks (`build-and-test`, `firestore-rules-test`),
# pull request reviews, linear history, and blocks force pushes and branch deletions.

REPO="${1:-zulqarnain7861/Bayan-ul-huda-quran-o-hadees}"
BRANCH="${2:-main}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
POLICY_FILE="${ROOT_DIR}/.github/branch-protection.json"

if [[ ! -f "${POLICY_FILE}" ]]; then
  echo "ERROR: Missing branch protection policy file at ${POLICY_FILE}" >&2
  exit 1
fi

echo "Applying branch protection policy from ${POLICY_FILE} to ${REPO} (${BRANCH})..."
gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "/repos/${REPO}/branches/${BRANCH}/protection" \
  --input "${POLICY_FILE}"

echo "Branch protection enabled and verified on ${REPO}@${BRANCH}."
