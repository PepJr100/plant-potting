# REFRESH draft — ROADMAP sections, window PLANTPOTTING-0006 → 0013 (+ Launch / monetisation fold-ins)

Drafted by `claude`. Scope per `_REFRESH-0013-prompt.md`: rewrite **Known gaps**,
**Species/Model Coverage**, and **Proposed Sprint Path** only — the orchestrator owns Current
State, Layer Status mechanics, Sprint History, frontmatter, and title/intent. Two inbox items are
folded in this pass: **A** the Launch track (Google Play Store release, PolyForm Noncommercial) and
**B** the monetisation spike as a single deferred Known-Gap bullet. Every number below is grounded
in the current `docs/ROADMAP.md`, the 0012/0013 feedback, and `play-store-publishing.md`.

---

## Known gaps

**The window's headline arc: confident-wrong was measured and roughly halved (0011), the
pothos↔Pilea boundary was closed without re-opening it (0012 gate → 0013 direct card), and the
KB/coverage delta was filled to 39-of-47. What's still open is honest *per-species* depth, the
messy-capture accuracy frontier, a handful of Pilea residuals, the deferred fine-tune — and, net-new
this pass, Play Store release-readiness.** Re-prioritized after the 0013 ship/review and the two
accepted inbox fold-ins:

0. **★ Real-world accuracy: measured & reduced under clean conditions (0011), but messy-capture
   accuracy and thin per-species depth stay open.** 0011 built the first honest `AccuracyEvalTest`
   scorecard behind the frozen seam (no model swap, no training) and drove **confident-wrong
   0.382 → 0.179** (0.098 on clean) via TTA-6 + a 0.30 abstain margin — top-1 held flat, held-out
   validated (tune-on-clean/eval-on-perturbation 0.186; leave-one-species-out 0.167–0.188), at an
   accepted pick-manually cost of 0.089 → 0.323. **What's still open:** the win is
   *measured-under-clean + synthetic-robustness, NOT real-world solved*, and the fixture set —
   though grown to **66 photos / 39 species** (0012 +19, 0013 +6) — still rests **31 of 39 species on
   1–2 photos**, so the per-species headline isn't trustworthy. The principal's standing ask (0013
   review): **≥3 CC0/PD photos per species**, then sweep higher **TTA ×8/×10/×20** mapping
   confident-wrong vs latency up to a **~2 s worst-case budget** (0011 capped TTA at 6 ≈ 125 ms).
   The license-clean supply ceiling is the blocker (0008's 1.0–6.8% figure; Begonia + Schlumbergera
   already hit documented ceilings — see gap 2c). Fine-tuning stays **deferred** (gap 5).
   `feedback/PLANTPOTTING-0011/feedback.md`, `feedback/PLANTPOTTING-0013/feedback.md`.

1. **The 28 delta mappings (0009's 16 + 0010's 12) are unprobed — editorial coverage, not
   real-photo calibrated.** Mapped coverage moved **10 → 26 → 38 → 39 of 47**, but the new rows'
   per-class confidence behaviour is assumed-from-routing, **not measured against real photos**
   (unlike the 0006/0007 in-vocab probes for Monstera and Crassula). A coarse/genus row could fire a
   confident-but-marginal card. Closing it needs imagery/probing — folds into the gap-0 fixture-depth
   work. **8 model classes remain deliberately unmapped** (gap routes through `LowConfidencePicker` /
   the 0010 "Add this plant" flow). Further text-only expansion over the remaining slice is still cheap.

2. **Pilea boundary + direct card SHIPPED (0012 gate → 0013 direct card) — CLOSED; residual tuning
   open.** The 0009 deferral is lifted: 0012 mapped Pilea (38→39) only in lockstep with a
   `ModelScoreMapper` boundary gate (top-1 = Pilea → picker with Pilea + pothos surfaced; CI-bound by
   `pileaMappingRequiresBoundaryGate`), and 0013 added a **direct** Pilea card above an elevated
   `per_species_thresholds["pilea-peperomioides"] = 0.98`, composed with — never replacing — the gate
   and **CI-bound > 0.9661** by `HousePlantClassMapValidationTest`. Validated by a pre-registered
   leave-one-out + author-separated sweep (6 distinct-author fixtures, +0 confident-wrong every fold)
   and **confirmed in the wild** (0013 review: real Pilea → correct direct card; 6/6 real-Pilea
   fixtures, 0 pothos→direct-Pilea). The headline gap is closed. **Residual / open pieces from the
   0013 review** (`feedback/PLANTPOTTING-0013/feedback.md`):
   - **(a) Re-derive a lower-but-safe `T_pilea`** to admit more genuine Pilea. ~0.97 stays CI-green:
     the `> 0.9661` floor is the *single-crop diagnostic* pothos ceiling, while the **shipped tta6**
     ceiling is only **0.9063** — real headroom. (The principal's initial "drop to 0.9" was flagged
     unsafe — below both the tta6 ceiling and the CI bind.) Needs more real Pilea photos (gap 0).
   - **(b) Harmonise the per-species "bespoke rules."** Pilea is the **only** species with bespoke
     rules (`boundary_pairs` + `per_species_thresholds`); every other species runs the global 0.55 +
     0.30 abstain margin. Design call: a **general per-species/per-pair mechanism** any species can
     opt into (keeps the asymmetric pothos→Pilea guard — recommended) vs. collapsing back to global.
   - **(c) AIY second-opinion cascade — SPIKE, measure first.** Route primary-model abstentions
     through the still-bundled AIY anchor (with TTA-6). **Weak prior** (AIY vocab overlaps only ~5 of
     39 species) + confident-wrong risk → cheapest first step is an **offline recoverable-abstention
     count** on the 66-fixture set; better framing may be **agreement-as-confidence** than AIY
     override. Must clear the same +0-confident-wrong held-out bar Pilea was held to.

3. **★ Pilea direct card shows NO hero image (placeholder only) — found in 0013 review.** Among the
   39 mapped species, `pilea-peperomioides` is the **only one** with no CC0/PD `.webp` in
   `res/drawable-nodpi/` — `PlantImageResolver` falls back to `ic_plant_placeholder`. Pilea was
   strict-picker until 0013, so its image was never added (it has shown the placeholder in the picker
   since 0012). `ReferenceImageManifestTest` only guards image→manifest (license), not species→image,
   so nothing went red. **Close:** source a CC0/PD Pilea photo via the `/reference-photos` skill +
   manifest + `docs/licenses/`, and add a test binding card-reachable species → non-placeholder
   drawable so a newly-promoted species can't regress. Small, self-contained.

4. **6 KB species remain out-of-vocab + the fine-tune is DEFERRED (training-bound).** The 0008 spike
   answered the data question — **PARTIAL-GO**: 5 OOV species (chlorophytum-comosum,
   philodendron-hederaceum, hoya-carnosa, monstera-adansonii, ficus-lyrata) have sufficient CC
   imagery to fine-tune; the pothos/Pilea boundary was CONDITIONAL (Pilea side thin ~76);
   `philodendron-pink-princess` is **NO-GO** (cultivar-proven CC imagery only ~5–15). The user's
   **no-self-shot constraint** removes the two paths the outline leaned on, so pink-princess + the
   Pilea-balance work are out. The 5-GO-class fine-tune stays viable on CC data alone but is **deferred
   behind the cheaper KB/accuracy levers** as lower ROI per effort. Reusable when revisited: the
   `finetune-sprint-outline.md` approach (47→52, float16 export, `ModelSwapEvaluationTest`,
   `ACTIVE_MODEL_ROOT`). `feedback/PLANTPOTTING-0008/feedback.md`.

5. **★ Not release-ready for the Google Play Store (blocks publication) — net-new this pass (fold-in
   A).** Licensing is already commercial-clean (gap 6); the blockers are engineering/process. (1)
   `targetSdk`/`compileSdk` = **34 < Play minimum** (API 35 now, API 36 from 31 Aug 2026) — bump to 35
   (eval 36) + fix edge-to-edge / predictive-back Compose regressions; (2) **no release signing** —
   add `signingConfig` + Play App Signing, secrets out of repo; (3) **debug APK only** — Play needs a
   signed **`.aab`** via `bundleRelease`; (4) **placeholder launcher icon** — real adaptive icon +
   512×512 Play icon; (5) **R8 disabled** — enable + TFLite keep rules (`org.tensorflow.**`), verify
   inference + `AccuracyEvalTest` on the minified release variant (ship minify-off for v1 if fragile).
   **Likely sprint:** `PLANTPOTTING-0014 (release readiness)`. `docs/future-ideas/play-store-publishing.md`.

6. **No project LICENSE and no privacy policy — net-new this pass (fold-in A).** The repo is
   "all rights reserved" by default (weak deterrent); Play requires a privacy-policy URL (the app holds
   `CAMERA`). Add a root `LICENSE` (**PolyForm Noncommercial 1.0.0**) + a short README licensing
   section (keep the existing per-asset `LICENSE-*.txt` and `docs/licenses/reference-images.md`), and
   author `docs/PRIVACY.md` (on-device-only; camera used only during ID; images not retained/transmitted;
   no analytics) hosted at a public URL. **Licensing is already commercial-clean:** both bundled ML
   models are Apache-2.0, all 44 reference photos are Unsplash/Pexels/CC0 (CC-BY-SA banned by policy),
   no GPL/copyleft, `verifyNoNetworking` holds — so PolyForm binds *licensees*, not the principal, and
   a free launch now keeps later monetisation / dual-licensing open.

7. **Monetisation options unexplored — DEFERRED, gated behind the free Launch (0014) — fold-in B.**
   Raw idea #1 in the inbox: a monetisation-options spike (brainstorm ~40 levers → top-5
   easiest-to-implement + top-5 biggest-profit → 2×2 ease-vs-revenue scoring). Named candidate levers:
   in-app ads, a paid/pro Play tier, subscription, substrate-supplier affiliate revenue, vertical
   integration (sell substrate directly), horizontal referral partnerships (pots / supplies). **Not
   scoped this pass** — recorded as a single forward-looking gap; it is gated behind shipping the free
   Launch and is **not** a sprint or milestone yet. `docs/future-ideas/feature-ideas.md` (raw idea #1).

### Standing non-goals (carried from 0005 §2.4)

- **Delegate / quantization variants.** No INT8 / GPU / NNAPI delegate yet. *(0014 R8/minify is build
  shrinking, not a delegate change.)*
- **No net-new screens.** No `CaptureFailedScreen`, `Settings`, or `ModelInfoScreen`.
- **No ML training or retraining.**
- **`PlantIdentifier` / `IdentificationResult` interface changes.** Frozen per 0003 §4.4.
- **KB edits.** Locked.
- **AGP / Kotlin / Compose / Hilt / TFLite version bumps.** Locked — **except `targetSdk`/`compileSdk`
  34→35 (eval 36), which PLANTPOTTING-0014 LIFTS** to clear the Play target-API requirement (fold-in A).
- **README rewrite.** Append-only — 0014 **appends a licensing section** (allowed); link this roadmap
  otherwise.
- **`expected-artifacts` re-baselining.** Off the table.

### Closed this window (removed, not carried)

- ~~**The model recognises wild flora, not houseplants.**~~ Closed in 0007: AIY V1/3 replaced as the
  default by `house_plant_species_mobilenetv2` (in-vocab 2/16 → 10/16, top-1 high-conf 1 → 6, latency
  43 → 33 ms). Residue tracked as gap 4 (6 OOV + pothos).
- ~~**Pilea silently unmapped / pothos↔Pilea confident-wrong boundary.**~~ Closed across 0012 (gate)
  + 0013 (direct card) — see gap 2; only residual tuning remains.

### Accepted as-is (recorded, not an open gap)

- **Failure-banner live visual (§7.6).** No production-accessible path drives the camera into
  `CameraUiState.Failure` for a live visual check; the user explicitly waived this — code review + JVM
  coverage accepted. Recorded for history only.

---

## Species/Model Coverage

How much of the bundled KB the on-device model identifies, plus the measured accuracy and
calibration state — rebuilt post-0012/0013.

| Axis | State | Notes |
| --- | --- | --- |
| KB species | **45** | `species.json`; +16 by 0009, +12 by 0010, +1 (Pilea) by 0012 (append-only; original 16 untouched). |
| Mapped (active model) | **39 of 47** | 10 → 26 (0009) → 38 (0010) → **39 (0012, Pilea)**. **Pilea now ships WITH a direct card** above `per_species_thresholds["pilea-peperomioides"] = 0.98` (0013), composed with the 0012 boundary gate, CI-bound **> 0.9661**. The 28 delta rows (0009+0010) are **unprobed** — editorial coverage, not real-photo calibrated (gap 1). |
| Unmapped model classes | **8 of 47** | Pilea left this set in 0012. The remaining 8 (Rattlesnake Plant, Iron Cross begonia, seasonal flowering bulbs, etc.) stay deliberately unmapped → `LowConfidencePicker` / the 0010 "Add this plant" flow. |
| KB species OOV in active model | **6** | monstera-adansonii, both Philodendrons, ficus-lyrata, chlorophytum-comosum, hoya-carnosa → route to `LowConfidencePicker`. 0008 confirmed CC fine-tune data for 5 of 6 (pink-princess NO-GO); fine-tune **deferred** (gap 4). |
| Fixture set (honest eval) | **66 photos / 39 species** | 8 → 41 (0011) → 60 (0012, +19) → **66 (0013, +6)** CC0/PD/CC-BY, with license + integrity + CSV-schema guard tests. **31 of 39 species still rest on 1–2 photos** (the principal's ≥3-per-species ask, gap 0). Begonia + Schlumbergera at 1 each — documented license-clean supply ceilings (wild-only Begonia; CC0 Schlumbergera all *S. truncata* ≠ KB *bridgesii*). |
| Real-photo accuracy | per-base clean top-1 **0.417 → 0.530**; confident-wrong **0.179** | Confident-wrong 0.382 → **0.179** (0.098 clean) via TTA-6 + 0.30 abstain margin (0011), held-out validated. Per-base clean top-1 rose **0.417 → 0.530** (0013) — mainly the 6 real-Pilea fixtures flipping picker → correct direct card (+ 4/6 new Thread-B second-photos correct); confident-wrong stayed flat (clean 0.117 → 0.121, pert 0.221 → 0.210). |
| Probe results (8 in-vocab fixtures) | 6 high-conf correct | monstera 1.0000, snake 1.0000, calathea 1.0000, orchid 1.0000, ZZ 0.9350, jade 0.5825; weak peace lily 0.4468 (correct, sub-threshold). The original pothos→Pilea confusion is now gated. |
| Routing | threshold-gated + abstention + Pilea direct-card bar | Top-1 vs per-class threshold falling back to the 0.55 global, then the 0.30 abstain margin, then the top-1=Pilea boundary gate / 0.98 direct-card bar → `ResultScreen` or `LowConfidencePicker`. |
| Latency | **~125 ms median (TTA-6)** | Single-crop ~20 ms; production runs 6 TTA views (centre + 4 corners + full-frame), 125 ms median / 539 ms worst on a one-shot identify. TTA stayed 6 through 0012/0013 (grid tiling went OOD; 3×3 busted the ~2 s cap at 2686 ms). |
| Calibration | ✓ abstention-backed (0011) + one Pilea direct-card threshold (0013) | `high_confidence_abstain_margin = 0.30` downgrades low-margin verdicts to the picker; `per_species_thresholds` holds **exactly one entry — `pilea-peperomioides = 0.98`** (the elevated direct-card bar above the pothos→Pilea ceiling), **never** seeded to bless a weak prediction (0006 anti-overfit discipline holds). |

**Honesty caveat (load-bearing):** these are *measured-under-clean + synthetic-robustness* numbers on
a set that still under-represents messy phone captures and rests **31/39 species on 1–2 photos** — **NOT
"real-world accuracy solved."** The harness is in place to measure any future lever honestly.

---

## Proposed Sprint Path

### Active horizon (detailed)

#### Shipped — window 0009 → 0013 (all reviewed clean; one-liners)
- **0009** — text-only KB-expansion: `species.json` 16 → 32, mapped 10 → 26 of 47, +`carnivorous-peat-sand`
  archetype; Pilea left unmapped (CI-enforced); new mappings unprobed. v0.3.0.
- **0010** — app-experience sprint behind the frozen seam: DataStore persistence, Home/landing, My
  Plants, confidence %+bar, contained search, "Add this plant" wireframe, 44 CC0/PD reference photos,
  shared Home button; KB 32 → 44 / 26 → 38-of-47. v0.4.0. **Review surfaced the headline gap: real-world
  accuracy is poor (snake plant ~1/3 correct, confidently wrong otherwise).**
- **0011** — accuracy & trust (measure → improve → abstain): first honest `AccuracyEvalTest` scorecard,
  fixtures 8 → 41, TTA-6 adopted, `high_confidence_abstain_margin = 0.30` → **confident-wrong 0.382 →
  0.179**; thicker confidence bar + all 11 botanical plates → real photos. v0.5.0.
- **0012** — pothos↔Pilea boundary fix: Pilea mapped (38→39) only behind a `ModelScoreMapper` gate
  (top-1=Pilea → picker), +19 CC0 fixtures (→60/39), **+0 confident-wrong** vs +2/+9/+6 naive; Pilea
  strict-picker. v0.6.0.
- **0013** — direct Pilea card at `T_pilea = 0.98` (LOO + author-separated, +0 confident-wrong, CI-bound
  > 0.9661; on-device 6/6 real-Pilea → correct direct card); +6 CC0 fixtures (→66/39); per-base clean
  top-1 0.417 → 0.530; stale Pilea-deferral prose scrubbed. v0.7.0. **Review: clean** — one cosmetic bug
  (no Pilea hero image, gap 3) + forward threads below.

#### Next — PLANTPOTTING-0014: release readiness (Launch track) — *detailed; fold-in A*
- **Entry conditions:** 0013 `status: done` and its review PR merged to `origin/main`; principal
  decisions locked (public repo, PolyForm Noncommercial 1.0.0; **Personal** Play account; **free
  launch**, monetisation deferred). Runs **parallel to V1 accuracy** behind the frozen seam — does not
  block or depend on accuracy work.
- **Code / config track:**
  1. Root `LICENSE` = PolyForm Noncommercial 1.0.0 (filled licensor line) + short README licensing
     section; keep per-asset licenses.
  2. Bump `targetSdk`/`compileSdk` **34 → 35** (eval 36) + fix API-35 edge-to-edge / predictive-back
     Compose regressions.
  3. Release `signingConfig` + Play App Signing; keystore + secrets **out of the repo** (current
     no-keystore state is the safe baseline).
  4. `bundleRelease` → signed **AAB** (Play no longer accepts APKs for new apps).
  5. Enable R8 (`isMinifyEnabled = true`) + TFLite keep rules (`org.tensorflow.**`); **verify inference
     + `AccuracyEvalTest` on the minified release variant** (TFLite + reflection can break under R8) —
     ship minify-off for v1 if fragile.
  6. Real launcher icon (replace placeholder `ic_launcher_foreground.xml`) + 512×512 Play icon.
  7. `docs/PRIVACY.md` (on-device classification; camera only during ID; images not retained/transmitted;
     local records; no analytics) at a public URL (GitHub Pages).
  8. Version → **v1.0.0** (`versionCode 8`); keep gates green (`ktlintFormat` first per the Windows
     autocrlf note).
- **Play-process track (plan-tracked, not code):** $25 Personal account + ID verification; Data Safety =
  *none collected*; content rating (IARC, likely Everyone); store-listing assets (512×512 icon,
  1024×500 feature graphic, ≥2 phone screenshots, descriptions, category); the **12-tester /
  14-continuous-day closed test** (Personal accounts post-Nov-2023 — recruit testers **first**, it's the
  schedule long pole). The existing tag-triggered `release.yml` (debug APK → GitHub) can stay for
  side-loaders, separate from Play.
- The tag-only `release.yml` publishes a GitHub Release **only on a pushed `v*` tag** — a `versionName`
  bump alone never publishes.

#### Also-next — V1 accuracy continuation (candidates from the 0013 review) — *runs alongside the Launch track; SID TBD*
- **Quick fix — Pilea hero image (gap 3).** Source a CC0/PD Pilea photo via `/reference-photos`, wire it
  into `PlantImageResolver` + manifest + `docs/licenses/`, and add a test binding card-reachable species →
  non-placeholder drawable. Small; could ride along with any sprint.
- **Deepen the fixture set to ≥3 photos/species + re-derive a lower-but-safe `T_pilea` (gaps 0, 2a).**
  Target ≥3 CC0/PD fixtures per species (31/39 still on 1–2); with more real Pilea in hand, re-derive the
  lowest `T_pilea` that still clears the shipped tta6 pothos ceiling (0.9063) with margin (~0.97 stays
  CI-green). Widen sources beyond iNaturalist (Wikimedia / GBIF / Pexels) to break the Begonia +
  Schlumbergera ceilings; consider whether `schlumbergera-bridgesii` should remap to *truncata*.
- **Harmonise the per-species bespoke rules (design, gap 2b).** A general per-species/per-pair mechanism
  any species can opt into (keeps the asymmetric pothos→Pilea guard — recommended) vs. collapsing to
  global rules.
- **AIY second-opinion cascade (SPIKE, measure first, gap 2c).** Offline recoverable-abstention count on
  the 66-fixture set first; better framing may be agreement-as-confidence. Must clear the +0-confident-wrong
  held-out bar.
- **Notes:** **no self-shot / first-party imagery anywhere** (hard user constraint) and **no model
  training** — behind the frozen seam. A residual 0010 refinement (My-Plants-survives-uninstall via Auto
  Backup or local export — mind the no-cloud-sync non-goal) can fold in opportunistically.

### Milestone ladder (skeleton — sprints become detailed as they approach)

#### MVP — On-device ID + KB-driven potting-mix recommendation — *(shipped ~PLANTPOTTING-0003)*
Camera → on-device identification → routing decision → KB-driven recipe, network-free, for the bundled
species set.

#### V0.1 — Trustworthy confidence calibration — *(met ~PLANTPOTTING-0006)*
- **Met:** real-photo probes for both in-vocab species (*Monstera deliciosa* @ 0.8984, high-conf;
  *Crassula ovata* @ 0.1055, low-conf) with accuracy assertions in CI; the per-species threshold
  mechanism wired and `perSpeciesThresholds` empty by design. Further depth required the V1 model swap.

#### V1 — Broad species coverage — *(active since PLANTPOTTING-0007, substantially advanced)*
- **Exit criteria:** most common houseplants identify directly so the low-confidence path is the
  exception, not the rule — at honest, measured accuracy.
- **Progress:** 0007 swap moved coverage 2/16 → 10/16; 0009/0010 took the cheap text-only lever to
  **39 of 47** mapped; 0011 measured & roughly halved confident-wrong (0.382 → 0.179) behind an
  abstention mechanism; 0012/0013 closed the pothos↔Pilea boundary and shipped a direct Pilea card. **The
  open piece is honest per-species depth** (31/39 on 1–2 photos), calibration of the unprobed delta rows,
  and the messy-capture frontier — see gaps 0–2.
- **Skeleton sprints:** ~~model-selection spike (0007 ✓)~~; ~~training-data spike (0008 ✓ PARTIAL-GO)~~;
  ~~KB-expansion (0009 ✓)~~; ~~accuracy & trust (0011 ✓)~~; ~~pothos↔Pilea gate + direct card
  (0012/0013 ✓)~~; **next → ≥3-photos/species fixture deepening + lower-but-safe `T_pilea` + rule
  harmonisation + AIY-cascade spike**; *deferred* → fine-tune for the 5 GO OOV classes (CC data only, no
  self-shot).

#### Launch — Google Play Store public release — *(new this pass; parallel to V1, does not block it)*
- **Exit criteria:** a signed release **AAB** of a min-API-35 build, a PolyForm-Noncommercial-licensed
  public repo, a live privacy policy, store listing + Data Safety + content rating complete, and the
  **12-tester / 14-continuous-day closed test** passed → production rollout.
- **Free launch** (PolyForm Noncommercial 1.0.0; Personal Play account). Licensing already commercial-clean
  (both ML models Apache-2.0; all reference photos Unsplash/Pexels/CC0; no copyleft; `verifyNoNetworking`
  holds) — the work is release-engineering + Play process, behind the frozen seam.
- **Skeleton sprints:** `PLANTPOTTING-0014` release readiness; then a 0014-review + closed-test-management
  follow-up if the tester gate surfaces issues. *(Monetisation is a separate, deferred raw idea gated
  behind this free Launch — gap 7; not a milestone.)*

#### V2 — Richer care guidance
- **Exit criteria:** beyond the one-shot recipe — repotting schedule, care reminders, or personalization
  to the user's plant set.
- **Skeleton sprints:** care-profile model; reminder UX; plant-collection persistence.

#### Beyond V2 — Plant-health diagnostics
- Identify stress / pest / over-watering signs from the same photo pipeline (sketch only).
