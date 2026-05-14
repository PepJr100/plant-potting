# Critique: PLANTPOTTING-0002-GEMINI

This critique evaluates `PLANTPOTTING-0002-GEMINI.md` (the "Original") against `PLANTPOTTING-0002-CODEX.md` (Draft A) and `PLANTPOTTING-0002-CLAUDE.md` (Draft B).

## 1. Comparison with Draft A (CODEX)

### What is stronger than GEMINI
- **Code Hotspots:** Draft A provides specific file paths and line ranges (e.g., `PermissionScreen.kt:148-154`). This reduces research time for the implementer.
- **TDD Sequencing:** Draft A explicitly mandates landing failing tests *before* the implementation (e.g., "Land the Bug 4 failing instrumentation test before changing..."). GEMINI lists tests as tasks but is less emphatic about the red-green-refactor sequence.
- **Navigation Detail:** For Bug 4, Draft A identifies the need to keep `onGranted()` idempotent to avoid navigation stack destabilization, a subtle but critical technical risk GEMINI misses.

### What is weaker than GEMINI
- **Task Granularity:** Draft A groups logic into large bullets, whereas GEMINI breaks them down into sub-tasks (1.1, 1.2), which is better for tracking progress.

### Missing Tasks
- **KDoc Updates:** Draft A includes a task to remove/update stale KDoc in `PermissionScreenHost`, ensuring documentation reflects the new lifecycle-aware reality.

### Underweighted Risks
- **Test Brittleness:** Draft A correctly identifies that "Settings round-trip tests can be brittle" and suggests stubbing intents over driving system UI, whereas GEMINI assumes `Intents.intending` is sufficient without discussing the fallback.

---

## 2. Comparison with Draft B (CLAUDE)

### What is stronger than GEMINI
- **Phase 0 (Sprint Setup):** Draft B includes a mandatory baseline check (Task 0.2) to ensure the CI chain is green on `main` before starting. GEMINI assumes a clean state.
- **Manifest Policy:** Draft B's treatment of Bug 3 is far more rigorous. It defines a "Manifest policy" (hard-fail vs. build-only) and includes a task (3.2) to create a repro script for the adb fallback.
- **Deterministic Evidence:** For Bug 3, Draft B specifies adding a `RecipeRowTag` test-tag (Task 3.10) to ensure the integration script can count rows via semantics rather than fragile XML scraping.

### What is weaker than GEMINI
- **Verbosity:** Draft B is extremely long (~400 lines). While comprehensive, it risks overwhelming an implementer or consuming excessive context tokens. GEMINI’s brevity is more "CLI-friendly" but sacrifices necessary detail.

### Missing Tasks
- **Icon Set Audit:** Draft B includes a specific check for `Icons.Default.CameraAlt` availability (Risk 5.3) and a contingency for adding the extended icon set.
- **Result Source Piping:** For UX 2, Draft B identifies that `IdentificationResult.source` is currently dropped during navigation and needs to be piped through (Task 6.1). GEMINI treats the refactor as simple UI work without acknowledging the underlying data flow gap.

### Underweighted Risks
- **Sandbox Environment:** Draft B explicitly calls out the "Sandbox filesystem overlay" risk (5.6), noting that writes outside the project tree might not reach the host disk. This is a critical environment-specific detail GEMINI lacks.

---

## 3. Sequencing and Risks

- **Sequencing:** Both Draft A and B are superior in their enforcement of TDD. GEMINI treats tests as "also-ran" tasks rather than the driving force of the fix.
- **Risks:** GEMINI’s risk section is thin. It misses the "Hilt + lifecycle + Compose" complexity (Draft B, 5.2) and the "Navigation Double-fire" risk (Draft B, 5.1).

---

## 4. Merging Recommendations

If I were merging these into a final plan, I would:

1.  **Keep the Phasing Structure from GEMINI:** Its categorization (Must/Should/Optional) is the most readable and provides clear de-scope paths.
2.  **Keep Phase 0 from CLAUDE:** Baseline validation on `main` is essential for a fix sprint.
3.  **Keep Hotspots from CODEX:** Reference specific line numbers for every "Must Land" bug.
4.  **Keep the Manifest Policy and `RecipeRowTag` from CLAUDE:** This makes the integration script (Bug 3) actually verifiable and robust.
5.  **Keep the TDD Constraints from CLAUDE:** Use the `(test)` suffix and the "Hard Gates" section to ensure the implementer doesn't skip verification.
6.  **Keep the Environment Risks from CLAUDE:** Specifically the sandbox filesystem warning and the Hilt/Lifecycle test complexity.
