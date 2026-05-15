# Critique of PLANTPOTTING-0003 Sprint Drafts (CODEX & CLAUDE)

This document provides a technical critique of the CODEX and CLAUDE drafts for the PLANTPOTTING-0003 sprint, comparing them against the GEMINI draft.

---

## 1. Critique of CODEX Draft

### 1.1 What is stronger than yours
- **Tooling Verification Discipline**: CODEX's **Phase 0** is significantly more rigorous. Task **0.2** (recording baseline results) and **0.4** (`PlantIdentifierContractTest`) ensure the "seam" isn't accidentally broken before work begins.
- **PowerShell Testing**: Task **1.1** ("Add a PowerShell-focused script test") addresses the difficulty of testing script logic without a live device, which my draft (GEMINI) handles only via manual emulator runs.
- **Mapping Detail**: The separation of `model_manifest.json`, `kb_mapping.json`, and `labels.csv` (Task **2.1**) is more modular and "production-ready" than my single `model_mapping.json`.
- **Paired Testing**: The "Every implementation task has a paired test task" constraint is a superior enforcement mechanism for TDD compared to my looser checklist.

### 1.2 What is weaker than yours
- **Performance & Hardware Optimization**: CODEX completely ignores performance telemetry, GPU delegation, and thread-tuning. My draft (**Phase 4.5**) treats "Real-time" feel as a first-class citizen.
- **Mapping Strategy**: CODEX lacks the "Genus-level fallback" logic (my **Tier 2**). If a plant is a *Monstera* but not *deliciosa*, CODEX likely forces a manual picker, whereas GEMINI maps it to the `aroid-chunky` archetype automatically, reducing user friction.

### 1.3 What tasks are missing
- **No Performance Telemetry**: No tasks for logging inference latency or warm-up timing.
- **No Hardware Acceleration**: No `GpuDelegate` or `NnApiDelegate` configuration.

### 1.4 What risks are underweighted
- **Latency/ANR Risk**: By not planning for model "warm-up" (pre-loading the Interpreter), CODEX risks a "janky" first-shutter experience.
- **Inference Reliability**: CODEX doesn't specify a background thread dispatcher (e.g., `Dispatchers.Default`) as explicitly as GEMINI.

### 1.5 What sequencing is wrong
- **Bug A Fix Timing**: While Phase 1 is fine, putting it before model artifacts is okay, but it doesn't utilize the script to verify the new ML UI until the very end.

---

## 2. Critique of CLAUDE Draft

### 2.1 What is stronger than yours
- **Programmatic Guardrails**: Task **1.3** (`VerifyNoNetworkingRegressionTest`) which greps the dependency tree for forbidden coordinates is a brilliant way to automate the "No Networking" mandate.
- **Archetype Picker UX**: The transition from Mid-confidence (Top-3) to Low-confidence (Archetype list) in **§4.3** is a more sophisticated UX than my binary "UnsureScreen".
- **Documentation for Handoff**: The inclusion of `docs/kb/ml-mapping-notes.md` (Task **2.5**) to justify editorial decisions is excellent for long-term maintainability.
- **Navigation Robustness**: CLAUDE's **§4.6** defines the exact navigation route shapes (`result/{speciesId}?source={source}`) which prevents ambiguity during implementation.

### 2.2 What is weaker than yours
- **Model Choice**: CLAUDE picks the `AIY Vision Plants V1` model, which is older and potentially less accurate on modern mobile hardware than `MobileNetV3-Small`.
- **Task Granularity**: Some tasks (e.g., **3.3**) are "mega-tasks" that involve model loading, bitmap resizing, inference, and thresholding in one block. My draft breaks these into smaller, more verifiable units.

### 2.3 What tasks are missing
- **No Hardware Acceleration**: Similar to CODEX, it ignores GPU/NNAPI delegation.
- **No Orientation-Aware Cropping**: Missing the `CenterCropOp` logic explicitly mentioned in GEMINI **§4.3**.

### 2.4 What risks are underweighted
- **Compose Navigation Complexity**: Passing complex encoded candidates via nav-args (Task **4.5**) can hit URL-encoding limits or cause runtime crashes if not handled carefully in Compose.
- **Thermal/Power**: No mention of the impact of running a ~25MB model on battery/heat.

### 2.5 What sequencing is wrong
- **Implementation before Testing**: Some implementation tasks (like **3.3**) are quite large and could benefit from intermediate test points rather than one giant "impl" task.

---

## 3. If I were merging, I'd keep:

1.  **From CODEX**: The **Phase 0 Baseline & Contract Tests** and the **PowerShell Mock/Shim** for Bug A verification (Task **6.2**).
2.  **From CLAUDE**: The **`VerifyNoNetworkingRegressionTest`** (automated dependency checking) and the **`ArchetypePickerScreen`** logic as a second-tier fallback for when Top-3 fails.
3.  **From GEMINI (Myself)**: The **Hardware Acceleration (GPU Delegate)** tasks and the **Three-Tier Funnel** (Genus-level auto-mapping) to minimize manual picker interruptions.
4.  **From CLAUDE**: The **`ml-mapping-notes.md`** requirement to ensure all mapping decisions are documented and defensible.
