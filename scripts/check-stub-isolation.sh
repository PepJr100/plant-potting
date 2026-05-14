#!/usr/bin/env bash
# Fails if any production source file outside app/src/main/.../identify/
# references StubPlantIdentifier by class name. Enforces the §4.5 seam rule.

set -euo pipefail

bad=$(grep -RIln "StubPlantIdentifier" app/src/main/ 2>/dev/null \
  | grep -v "/identify/" || true)

if [ -n "$bad" ]; then
    echo "StubPlantIdentifier is referenced outside identify/:"
    echo "$bad"
    exit 1
fi

echo "stub isolation OK"
