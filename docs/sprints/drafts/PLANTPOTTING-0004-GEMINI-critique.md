# Critique: PLANTPOTTING-0004 Sprint Drafts

**Date:** 2026-05-15  
**Subject:** Evaluation of GEMINI, CODEX, and CLAUDE drafts for the PLANTPOTTING-0004 fix sprint.

---

## Evaluation of CODEX Draft

### 1. Stronger than GEMINI
- **TDD Discipline (Phase 0):** Codex defines a far superior Phase 0 (Tasks 0.4–0.12) that locks the contract with granular RED tests. Specifically, Task 0.12 (manifest vs interpreter dtype comparison) is a vital "belt-and-braces" check that ensures the manifest doesn't lie.
- **Source Set Hygiene (Phase 3):** Task 3.1 correctly identifies that the global `TestIdentifyModule` is the root cause of the testing gap and deletes it. This is more sustainable than Gemini's "just add a test" approach, as it forces all future tests to be explicit about their bindings.
- **Refactoring Depth (Phase 1):** Task 1.3 explicitly cleans up the redundant `testTagsAsResourceId` wrappers in `RecommendationScreen`, which were "leaked" hacks from previous attempts.
- **Back-compat (Task 2.11):** It ensures the FLOAT32 path remains test-covered, allowing for future model swaps without code changes.

### 2. Weaker than GEMINI
- **PreprocessedImage representation:** Codex keeps a less-than-ideal data representation compared to the more modern `ByteBuffer` approach suggested by Claude (and hinted at in Gemini's Phase 1).

### 3. Missing Tasks
- **Output Dequantization:** Like Gemini, Codex assumes Bug 1 is only an *input* problem. The AIY V1/3 model is fully quantized (UINT8 input and output). Without dequantizing the output tensor in the facade, the `ModelScoreMapper` will receive raw bytes (0–255) and interpret them as probabilities (0.0–1.0), likely resulting in 100% confidence for everything or garbage results.

### 4. Risks Underweighted/Ignored
- **Dequantization Error:** Ignored. Passing raw UINT8 scores to the float-based mapper is a silent failure that would bypass the "doesn't throw" assertion but fail acceptance.

### 5. Sequencing Errors
- None. Sequencing is logical and follows the RED-first mandate.

---

## Evaluation of CLAUDE Draft

### 1. Stronger than GEMINI
- **Technical Precision (Bug 1):** Claude is the ONLY draft to correctly identify that the AIY V1/3 model is fully quantized (Section 3.1, Task 1.9). It tasks the dequantization of the UINT8 output tensor via `quantizationParams()`, which is essential for a functional fix.
- **Idiomatic TFLite (Decision 4.4):** It proposes refactoring `PreprocessedImage` to carry a `ByteBuffer`. This is the most efficient way to handle TFLite inputs and avoids unnecessary allocations/casts.
- **Accessibility Analysis (Decision 4.2 / Risk 7.2):** It provides a professional analysis of the `testTagsAsResourceId` impact on TalkBack, ensuring the fix doesn't regress UX for disabled users.
- **Hilt "Inject Concrete" Trick (Decision 4.3):** While less "clean" for the source set than Codex's delete-everything approach, the trick of injecting `OnDevicePlantIdentifier` directly is a highly efficient way to bypass the interface binding swap without refactoring 5+ other test files.

### 2. Weaker than GEMINI
- **Task Granularity:** Tasks like 1.9 are "megatasks" that combine loading, inference, and dequantization. Gemini's breakdown is easier to track turn-by-turn.
- **RED-first evidence:** While it mandates TDD, the actual Phase 0 is lighter on "lock-the-contract" assertions compared to Gemini/Codex.

### 3. Missing Tasks
- **Cleanup of redundant bridges:** It misses the opportunity to remove the local `testTagsAsResourceId` wrappers in `RecommendationScreen` that Codex caught.

### 4. Risks Underweighted/Ignored
- **Hilt Assumption:** It relies on Hilt's behavior of allowing concrete injection alongside interface swaps. While correct, it's a "clever" dependency that could be brittle if Hilt's internal graph resolution changes.

### 5. Sequencing Errors
- None. Parallelization between Phase 1 and 2 is correctly identified.

---

## Merging Strategy

If merging these drafts, I would prioritize **CLAUDE's technical model** and **CODEX's testing rigor**.

### KEEP:
- **From CLAUDE:** The `PreprocessedImage` -> `ByteBuffer` refactor and the **UINT8 output dequantization logic**. These are the "missing links" that make the fix actually work for the specific AIY model.
- **From CODEX:** The Phase 0 **Contract Lock** suite and the decision to **delete the global `TestIdentifyModule`**. This is the correct way to close the test gap permanently rather than just adding one "special" test.
- **From CODEX:** The cleanup of the `RecommendationScreen` local bridge hacks.

### DISCARD:
- **GEMINI's (my) Bug 1 plan:** It was too shallow on the quantization details (missed output dequantization).
- **CLAUDE's "Inject Concrete" trick:** While clever, Codex's plan to fix the source set by deleting the global replacement is better engineering for long-term maintainability.
- **Codex's inter-phase dependencies:** Use Claude's cleaner "Phase 2 is fully parallel" model.

**Signal for the Merge:** The key failure of the GEMINI draft was missing the **output dequantization**. Both CODEX and GEMINI focused on the "Cannot convert... UINT8 to [[[[F" error as an input problem, but CLAUDE correctly saw it as a systemic quantization mismatch. Use CLAUDE's Section 3.1/1.9 as the blueprint for the `InterpreterFacade` implementation.
