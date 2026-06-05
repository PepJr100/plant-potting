# Critique of CODEX and CLAUDE Drafts for PLANTPOTTING-0008

## CODEX Draft
**Stronger than mine:** 
- Exhaustive detail and rigorous tracking. The creation of `source-counts.csv` and `query-log.md` in "Phase 1 - Research Artifact Skeleton" ensures reproducibility.
- The test coverage specified in "Phase 7 - Camera Back-From-Results Shutter Fix" is excellent, specifically citing existing tests like `CameraScreenSmokeTest.kt`, `CameraScreenBindStateTest`, and specific `CameraViewModelTest` cases to maintain.

**Weaker than mine:**
- The draft is overly granular and bloated. "Phase 4 - Per-Source Count Collection" contains over 30 separate checkbox tasks (one for every species/source combination), making it tedious to track progress and read during a spike.

**Missing tasks:**
- Lacks a task to define a clear methodology *before* starting the queries, such as Claude's "Write a one-page methodology note" or defining exact license exclusions clearly in a single reference file. 
- Misses the context framing of the previous sprint (PLANTPOTTING-0007).

**Risks underweighted:**
- It fails to identify the risk of API rate limits when querying these sources programmatically, which my draft explicitly mitigates ("Automated API scripts might violate rate limits").
- It glosses over the specific trap of `CC-BY-NC` licenses on iNaturalist inflating counts, which is a critical commercial-use blocker.

**Sequencing is wrong:**
- The sequencing rule "Run Phase 8 after both the report and camera fix are complete" unnecessarily blocks running `ktlintCheck` and `testDebugUnitTest` for the independent code fix until the desk-research report is finalized. Code checks should run as soon as the code is done.

## CLAUDE Draft
**Stronger than mine:**
- The contextual framing in the intro linking back to `PLANTPOTTING-0007` is brilliant.
- "Phase 0 — Scaffolding & inputs" sets up the spike perfectly with a "methodology note" and a clear task to record the "disjoint-split exclusion set".
- "Phase 3 — Shutter-on-return UX fix" identifies the exact root cause and line numbers (`CameraScreen.kt:191`, `CameraViewModel.kt:51`), which removes all guesswork for the engineer.
- R1 explicitly identifies the `CC-BY-NC` license issue inflating iNat counts.

**Weaker than mine:**
- Tracking the core research is too compressed. "Phase 1 — Per-species data assessment" folds all species into single source queries (e.g., "iNaturalist counts. For each target, query..."), making it harder to check off individual species progress compared to my draft's per-species tasks.

**Missing tasks:**
- Missing a task to create a machine-readable CSV for the counts (like Codex's `source-counts.csv`), relying only on a markdown table ("Add a roll-up matrix docs/sprints/evidence/PLANTPOTTING-0008/go-no-go-matrix.md").

**Risks underweighted:**
- Like Codex, it misses the risk of "Automated API scripts might violate rate limits" (from my draft), focusing only on network leakage and Windows portability (R6, R10).

**Sequencing is wrong:**
- The dependency diagram and "Phase 4 — Gates" funnels everything to run at the end. The integration/unit tests for the UX fix shouldn't need to wait for the research phases (1-2) to complete. Code validation should run as soon as Phase 3 is done.

## If I were merging, I'd keep:
- **X from CODEX**: The rigorous test citations (`CameraScreenSmokeTest.kt`, etc.) in Phase 7 and the machine-readable `source-counts.csv` requirements.
- **Y from CLAUDE**: The excellent PLANTPOTTING-0007 context, the `CC-BY-NC` license trap warning, the "Phase 0" methodology setup, and the specific root-cause analysis for the camera bug (`CameraScreen.kt:191`).
