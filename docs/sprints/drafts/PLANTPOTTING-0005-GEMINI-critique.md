# Critique: PLANTPOTTING-0005 — GEMINI Draft

This document critiques the `PLANTPOTTING-0005-GEMINI.md` draft against the `CODEX` and `CLAUDE` alternatives for the "Polish, Calibration, and Un-deferral" sprint.

## 1. Comparison with PLANTPOTTING-0005-CODEX

### Stronger than GEMINI
- **Phase 0 Baseline Inventory:** CODEX includes a dedicated phase for baseline inventory (listing specific tests, running `check-stub-isolation.sh`) that GEMINI lacks. This ensures the environment is healthy before any changes.
- **Granular Migration List:** CODEX explicitly lists the six test classes to be migrated (e.g., `EndToEndFlowTest`, `CameraPreviewLayoutTest`), whereas GEMINI only mentions "total 6".
- **Product Philosophy:** The "Working Decisions" section in CODEX (e.g., "Preserve honest confidence language") provides better guidance for the implementer on the *tone* of the UI polish.

### Weaker than GEMINI
- **UI Specificity:** GEMINI is more explicit about cleaning up the "skeletal TopCenter text overlay," whereas CODEX is slightly vaguer about "augmenting" the surface.
- **Documentation:** GEMINI includes a specific task for updating `docs/kb/ml-mapping-notes.md` with calibration logic, which CODEX omits.

### Missing Tasks
- **Calibration Documentation:** CODEX lacks the task to document *how* thresholds were determined (GEMINI §3.3).
- **Cleanup:** CODEX doesn't explicitly mention the removal of the old text overlay in `CameraScreen`.

### Underweighted Risks
- **Threshold Sprawl:** GEMINI identifies "threshold sprawl" as a risk; CODEX focuses more on fixture overfitting.

---

## 2. Comparison with PLANTPOTTING-0005-CLAUDE

### Stronger than GEMINI
- **Architectural Rigor (Decisions):** CLAUDE’s Section 4 ("Decisions") is significantly stronger, providing the "why" behind choices like `@BindValue` vs `@TestInstallIn`.
- **Safety (Contract-Locks):** CLAUDE’s Section 4.9 ("Contract-lock pattern") is a major improvement. Requiring RED tests for file absence and manifest properties ensures the implementer proves the fix.
- **Empirical Calibration (Probe Run):** CLAUDE’s Phase 5.5 ("Probe run") is the correct way to handle the real-photo fixture. It acknowledges that model output is a "known-unknown" and captures data before committing an assertion.
- **Technical Nuance:** CLAUDE identifies the risk of GMD environment disruption (Risk 7.10) from intent leakage, a detail missed by both GEMINI and CODEX.

### Weaker than GEMINI
- **Complexity:** CLAUDE’s draft is nearly 3x the length of GEMINI's. While thorough, it risks overwhelming the implementer with metadata and "nice-to-haves" (§3.2).
- **Conciseness:** GEMINI's "Acceptance Criteria" are easier to scan for a final sign-off.

### Missing Tasks
- **Manual KB Notes:** Like CODEX, CLAUDE misses the manual documentation update for `ml-mapping-notes.md`.

### Underweighted Risks
- **Threshold Maintenance:** CLAUDE focuses heavily on the *mechanism* of thresholds but less on the long-term maintenance risk of a large manifest.

---

## 3. Summary of Sequencing & Risks

### Sequencing Issues in GEMINI
- GEMINI starts with "Foundation" (ROADMAP.md) and "Infrastructure Cleanup." Both CODEX and CLAUDE correctly prioritize a **Baseline Phase (Phase 0)**. Running tests and recording current state must happen before any file is touched.
- GEMINI places UI Polish before Calibration. While not "wrong," CLAUDE’s approach of running a **Probe** before finalizing the Calibration tasks is a more logical flow for research-heavy tasks.

### Underweighted Risks in GEMINI
- **Test Fragility:** GEMINI underweights the complexity of the `@BindValue` migration (which CLAUDE identifies as finicky in Kotlin).
- **Model Uncertainty:** GEMINI assumes the real photo will just "work" for accuracy assertions. CLAUDE correctly treats this as speculative until the probe is run.

---

## 4. Merging Recommendation

If I were merging these drafts, I would:

1.  **Keep CLAUDE’s Section 4 (Decisions) and Section 4.9 (Contract-lock pattern):** These are the gold standard for engineering safety and clarity.
2.  **Keep CODEX’s Phase 0 Inventory:** Explicitly listing the tests to be migrated reduces ambiguity.
3.  **Keep CLAUDE’s Phase 5.5 (Probe run):** Essential for a falsifiable calibration task.
4.  **Keep GEMINI’s Section 6 (Acceptance Criteria):** It strikes a good balance between brevity and completeness for the final gate.
5.  **Incorporate CLAUDE’s Risk 7.10 (Intent leakage):** This is a high-value insight for GMD testing stability.
