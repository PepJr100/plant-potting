# PLANTPOTTING-0003 — Results

**Sprint:** PLANTPOTTING-0003 — On-device ML plant identifier + Bug A fix + source-driven badge
**Status during fill-in:** in-progress
**Executor:** opus (in-session)
**Plan:** `docs/sprints/PLANTPOTTING-0003.md`

---

## 0. Baseline (§0.2)

Captured on a clean `main` (commit `5a63826`) **before** any sprint edits.

| Command | Result |
| --- | --- |
| `./gradlew --no-daemon assembleDebug testDebugUnitTest lint ktlintCheck verifyNoNetworking` | `BUILD SUCCESSFUL in 2m 5s` (74 tasks: 5 executed, 3 from cache, 66 up-to-date) |
| `bash scripts/check-stub-isolation.sh` | `stub isolation OK` |

Both gates were green at the start of the sprint, so any failure surfaced later is owned by sprint edits.

### Phase 0 §0.4 contract test (RED-first)

`PlantIdentifierContractTest` was added in Phase 0 and intentionally fails until §3.1 lands the additive `lowConfidence` field on `IdentificationResult`. The test uses reflection so the file still compiles on the pre-§3.1 source — the failure mode is a runtime `containsExactly` mismatch, which is the documented intent of the §6.1 hard gate.

---

## 1. Model choice (§4.1)

_To be filled in when Phase 2 lands._

## 2. Mapping coverage summary (§4.2)

_To be filled in when Phase 2 lands._

## 3. Identifier behaviour evidence (§3.8)

_To be filled in when Phase 3 lands._

## 4. Source-driven badge (UX 2)

_To be filled in when Phase 5 lands._

## 5. Bug A — retroactive closure of PLANTPOTTING-0002 §8 line 310

_To be filled in when Phase 7 lands. PLANTPOTTING-0002.md is intentionally **not edited** per §7.6._

## 6. Final verify (§8.5)

_To be filled in at sprint close-out._

## 7. Known gaps / handoff for PLANTPOTTING-0004

_To be filled in at sprint close-out._
