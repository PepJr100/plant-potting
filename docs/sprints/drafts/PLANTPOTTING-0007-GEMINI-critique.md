# Critique of PLANTPOTTING-0007 Drafts

## Draft A (Codex)

**What is stronger than yours:**
The explicitly named candidate list is far superior. Tasks like "Evaluate a `plantnet_300k_mobilenet_v3_small_int8` candidate" and "Evaluate an `inat_plants_tflite` candidate" turn a vague research step into a concrete hit-list. The evidence reporting tasks in "Phase 1 — Candidate Survey And Decision Matrix" and "Phase 3 — Multi-Model Evaluation Harness" (requiring a `model-candidate-matrix.md` and a machine-readable `model-swap-eval.csv`) are excellent and much more rigorous than my generic instruction to "Document the evaluation results". It also includes critical app-specific integration gates like running `scripts/check-stub-isolation.sh`.

**What is weaker than yours:**
It is excessively verbose with over 40 checklist items, diluting the critical path. The task "Evaluate a `kb16_houseplant_mobilenet_v3_small_int8` fallback candidate" requires training a new ML head, which directly violates my strict "No new ML training" scope boundary and introduces massive feature creep.

**What tasks are missing:**
It lacks a task to actively measure model inference latency. A highly accurate model that takes 3 seconds to run on-device is a regression, but the Phase 3 harness tasks only measure size, licensing, and accuracy.

**What risks are underweighted:**
The risk of the "KB16 fallback" scope creep is severely underweighted. Training a custom model from disjoint CC images could easily derail the sprint into a week-long data-collection exercise, but the mitigation assumes it is trivial.

**What sequencing is wrong:**
In "Phase 6 — Network-Free, Size, And Integration Gates", it schedules "Run `verifyNoNetworking` after candidate dependencies and model wiring are in place". Checking `verifyNoNetworking` should happen immediately after adding the model bundle or acquisition scripts in Phase 3/4. If a downloaded model bundle or script introduces a network call, catching it in Phase 6 means unwinding days of work.

---

## Draft B (Claude)

**What is stronger than yours:**
Outstanding context setting. Referencing the exact 0006 results (the 0.1055 Jade confidence) anchors the sprint perfectly. It explicitly identifies exactly which existing infra components to reuse (`OnDeviceModelRealInterpreterTest`, `ModelScoreMapper`, `ImagePreprocessor`), strictly bounding the implementation effort. The "Phase 6 — Constraint, size & integration gates (cheap → expensive)" sequencing is a brilliant way to fail fast.

**What is weaker than yours:**
Like Draft A, it includes the "KB16 MobileNetV3-Small head INT8" task which violates the core constraint of finding/evaluating pre-trained models. The document is also very dense, prioritizing comprehensive prose over scanability.

**What tasks are missing:**
Similar to Draft A, it misses any task to evaluate inference latency in "Phase 3 — Swap-evaluation harness". It also misses an explicit task to verify that candidate models perform reasonably on our specific 480x480 center-cropped image format compared to their native training distribution.

**What risks are underweighted:**
Risk "R7 — TFLite I/O shape/dtype mismatch tempts a seam break" severely underestimates the difficulty of adapting arbitrary model outputs (like PlantNet's 300K output tensor) into our specific `IdentificationResult` format without breaking the seam. 

**What sequencing is wrong:**
It places the `verifyNoNetworking` check in Phase 6 ("verifyNoNetworking GREEN after candidate deps + wiring land"). If a Gradle dependency for a new TFLite op breaches the network boundary in Phase 5, catching it in Phase 6 is too late. It should be a continuous gate evaluated immediately upon candidate selection.

---

## Merging Recommendation

If I were merging, I'd keep the **explicit candidate hit-list and CSV evaluation harness** from Draft A, and the **"cheap → expensive" gating and infra-reuse callouts** from Draft B. However, I would strictly reject both drafts' "KB16 fallback" tasks to enforce my draft's absolute "no ML training" boundary.
