# REFRESH brief — PlantPotting ROADMAP, window PLANTPOTTING-0006 → 0013

You are refreshing `docs/ROADMAP.md`. This is the first true REFRESH since 0005 — every
close-out from 0006 onward was a cheap BUMP, so the heavy sections have drifted. **Read the
current `docs/ROADMAP.md` and the sprint plans + feedback for the window**: PLANTPOTTING-0006,
0007, 0008, 0009, 0010, 0011, 0012, 0013 (`docs/sprints/{SID}.md` and
`docs/sprints/feedback/{SID}/feedback.md`). Also read `docs/future-ideas/feature-ideas.md`
and `docs/future-ideas/play-store-publishing.md` — two inbox items are being folded into this
pass (see "Inbox fold-ins" below).

Write your draft to **`docs/roadmap/drafts/REFRESH-0006-to-0013-<AGENT>.md`** (your output path
is given to you on the command line — use exactly that filename).

## What to produce

Update ONLY these sections (the orchestrator handles Current State, Layer Status mechanics,
Sprint History, frontmatter, and title/intent — do NOT rewrite those):

1. **Known Gaps reconciliation.** Which gaps the window's sprints (0006–0013) closed, which
   new gaps emerged from feedback, justify each delta. The current ROADMAP Known Gaps list is
   already partly reconciled in prose (gaps #0–#4) — tighten it, remove what's truly closed,
   and make sure it reflects the 0011 accuracy work, the 0012 Pilea gate, and the 0013 direct
   Pilea card + its residuals (lower-but-safe T_pilea, per-species rule harmonisation, the
   missing Pilea hero image, the AIY second-opinion cascade spike). Plus the two fold-ins below.

2. **Species/Model Coverage.** The table is STALE — it still says "44 KB species / 38-of-47
   mapped" while reality (post-0012/0013) is **45 species / 39-of-47 mapped**, Pilea now mapped
   WITH a direct card at `per_species_thresholds["pilea-peperomioides"]=0.98`, fixtures **66
   photos / 39 species**, per-base clean top-1 **0.417 → 0.530**. Rebuild the table honestly:
   in-vocab coverage, mapped-vs-unmapped counts, real-photo accuracy (confident-wrong 0.179),
   calibration state, latency (TTA-6 ~125 ms), and the honesty caveat (still 31/39 species on
   1–2 photos; measured-under-clean + synthetic-robustness, NOT real-world-solved).

3. **Proposed Sprint Path.** Rebuild. The active horizon currently lists 0009–0013 as
   "Shipped" blocks and an open "Next (candidates from 0013 review)". Collapse the shipped
   blocks to brief one-liners, and turn the 0013-review candidate threads into a concrete
   **Next** block. Re-skeleton the milestone ladder; promote milestones whose exit criteria are
   met (MVP shipped ~0003, V0.1 met ~0006, V1 active since 0007 and substantially advanced).
   Always sketch one milestone beyond V2. Then add the Launch milestone + 0014 Next block (below).

## Inbox fold-ins (BOTH accepted by the principal this pass)

**A. Launch track — publish to Google Play Store (PolyForm Noncommercial license).** This is a
net-new direction, not a sprint-window reconcile. ALL content is pre-mapped in
`docs/future-ideas/play-store-publishing.md` under "Content pre-mapped to ROADMAP sections" —
use it verbatim where it fits. In summary:
   - **Known Gaps:** add a ★ "not release-ready for Play Store" gap (5 blockers: targetSdk 34 <
     Play min 35; no release signing; debug-APK-only not AAB; placeholder launcher icon; R8
     off) + a smaller "no project LICENSE / no privacy policy" gap. Likely sprint =
     `PLANTPOTTING-0014 (release readiness)`.
   - **Proposed Sprint Path:** add a **Launch — Google Play Store public release** milestone
     (runs PARALLEL to V1 accuracy, does not block it) and a detailed **Next** block for
     `PLANTPOTTING-0014 — release readiness` (code track + Play-process track from the write-up).
   - Note in standing non-goals that 0014 LIFTS the targetSdk/compileSdk 34→35 (eval 36) lock
     and allows a README licensing-section append; all other locks stay (frozen seam, KB, no
     training, no net-new screens).
   - Licensing is already commercial-clean (both ML models Apache-2.0; all reference photos
     Unsplash/Pexels/CC0; no copyleft; `verifyNoNetworking` holds).

**B. Monetisation spike — KNOWN GAP ONLY (do NOT develop or scope).** Reflect raw idea #1 from
the inbox as a single forward-looking Known Gap / future-milestone note: a monetisation-options
spike (40 ideas → top-5 easiest-to-implement + top-5 biggest-profit → 2×2 ease-vs-revenue
scoring; candidate levers named in the inbox: in-app ads, paid/pro Play tier, subscription,
substrate-supplier affiliate, vertical integration, horizontal referral partnerships).
**Deferred — gated behind the free Launch (0014).** Do not turn it into a sprint or milestone;
one gap bullet is enough.

## Style
Be concrete and honest. Match the existing ROADMAP voice (dense, evidence-cited, with real
numbers). Skeleton items are one-liners; the next 1–3 sprints get full blocks. Do not invent
file paths — ground every claim in the repo and the sprint docs you read.
