# Round-2 drafter/critiquer evaluation — Pattern-B grounded drafting

**Date:** 2026-06-08 · **Evaluator:** Claude (Opus 4.8), the orchestrating session ·
**Method:** the structure the principal specified — mock-plan the next sprint (direct Pilea card), have
5 OpenRouter candidates + 2 incumbents (Claude, Codex; agy still quota-blocked) **draft via Pattern B**
(codex custom-provider, repo-grounded), have the 5 candidates **critique** the two incumbent drafts via
Pattern A, have Opus critique Codex + each candidate draft, then score the lot.

Artifacts: the per-model `drafts/`, `critiques/`, the shared `intent.md`, `metrics.txt`, and the
`run_draft.sh` / `critique.py` harness were kept **local only** (not committed — write-ups-only policy).
This file is the record of the round.

> **Headline.** Under **Pattern B (agentic)**, only the **two paid** candidates (DeepSeek V4 Pro, MiniMax
> M2.1) reliably read the repo and wrote their own draft. **All three free candidates failed the agentic
> write** — one gateway-died twice (qwen3-coder:free), two generated a full draft but never actually wrote
> the file (gpt-oss printed to stdout; nemotron botched a PowerShell write and timed out). Under **Pattern A
> (critique)**, the split flips: **4 of 5 critiqued well**, including both surviving free models, and all four
> independently found the deepest flaw in the incumbent drafts. **Conclusion: free OpenRouter models are
> viable critiquers, not viable agentic drafters. The repo-grounding gap is closed for the paid models —
> DeepSeek V4 Pro is the standout fallback drafter.**

---

## 1. Reliability — the real story (again)

### Draft phase (Pattern B, agentic, 7 agents, parallel)

| Agent | Model | Self-wrote? | Latency | Tokens | Outcome |
|---|---|---|---|---|---|
| CLAUDE *(incumbent)* | claude (Opus) | ✅ | 218 s | — | 18.8 KB, clean |
| CODEX *(incumbent)* | gpt-5.5 | ✅ | 173 s | 61.6k | 15.0 KB, clean |
| **DEEPSEEK** | deepseek/deepseek-v4-pro | ✅ | 292 s | 64.2k | 16.3 KB, clean |
| **MINIMAX** | minimax/minimax-m2.1 | ✅ | 353 s | 49.8k | 7.3 KB, clean |
| **GPTOSS** | openai/gpt-oss-120b:**free** | ❌ | 159 s | 18.4k | **printed draft to stdout, 0 bytes written** — salvaged |
| **NEMOTRON** | nvidia/nemotron-3-super-120b:**free** | ❌ | 421 s (timeout) | — | **botched `$var` shell write across processes, looped to timeout** — salvaged |
| **QWENCODER** | qwen/qwen3-coder:**free** | ❌❌ | 42 s + 39 s | — | **OpenRouter gateway "high demand" ×2, 0 content** — DNF |

**4 of 7 self-wrote; 0 of 3 free candidates did.** The two salvageable free drafts were recovered from
their logs for *quality* scoring, but as fallback drafters all three free models are failures: the agentic
loop (many tool calls, multi-line file writes, Windows-shell quoting) is exactly where weak tool-calling and
free-tier rate limits bite hardest — precisely the §7B caveat, now reproduced.

### Critique phase (Pattern A, single-shot completion, 5 candidates)

| Candidate | Model | Outcome | Latency | Out tok | Cost |
|---|---|---|---|---|---|
| DEEPSEEK | deepseek-v4-pro | ✅ | 137 s | 3,123 | $0.0243 |
| MINIMAX | minimax-m2.1 | ✅ | 84 s | 3,778 | $0.0120 |
| GPTOSS | gpt-oss-120b:free | ✅ | 89 s | 2,859 | $0 |
| NEMOTRON | nemotron-3-super:free | ✅ | 64 s | 3,472 | $0 |
| QWENCODER | qwen3-coder:free | ❌ HTTP 429 (rate-limited upstream, after backoff) | — | — | — |

**4 of 5 critiqued successfully.** Single-shot Pattern A is far lighter than an agentic loop, so the two
free models that *failed as drafters* (gpt-oss, nemotron) **succeeded as critiquers**. Only qwen3-coder:free
was unusable in either mode today (gateway/rate-limit, 3 failures total).

---

## 2. Drafter axis — quality (1–5; only agents that produced content)

| Agent | Reliab. | Grounding | Central-challenge grasp | Sequencing | Anti-overfit | Concrete | Notes |
|---|---|---|---|---|---|---|---|
| **CLAUDE** *(inc.)* | ✅ | **5** | **5** | **5** | **5** | **5** | Decision **Fork A/B**; names all 6 fixture authors + `ModelScoreMapper.kt:89-103` exactly; honest that non-separability is the likely outcome. Reference-class. |
| **CODEX** *(inc.)* | ✅ | 5 | 4.5 | 3 | 5 | 5 | Failing-test-first; sharpest anti-overfit clause ("reject if threshold chosen after seeing held-out rows"). **Flaw: builds before it evaluates, no fork.** |
| **DEEPSEEK** | ✅ | 4.5 | 4.5 | **5** | 4.5 | 5 | **Best candidate.** Eval-first phases (A→G), "files touched / not touched" tables, risk-table abandonment path. Slip: `ModelManifestReader` (parsing is in `ModelManifest.kt`); fork lives in risks, not structure. Competitive with the incumbents. |
| **MINIMAX** | ✅ | 4 | 3.5 | 2.5 | 3.5 | 3.5 | Competent, correct paths, readable — but **config+logic before eval, no fork, no manifest-shape design, no fixture thickening.** Thinnest success. Solid B. |
| **NEMOTRON** | ❌ | 4 | **2** | 3.5 | 3 | 3.5 | **Misreads the hazard** — imagines the gate discriminates on "pothos mass," but the `0.9661` hit has *no* second-place mass. Also edits a closed `feedback.md`. + reliability fail. |
| **GPTOSS** | ❌ | 3 | 3 | 3 | 2.5 | 3.5 | Grasped the composed gate but `top1=0.85`/`margin=0.05` are wrong magnitudes; mislocated the fixtures; eval is a step not a gate. + reliability fail. |
| **QWENCODER** | ❌❌ | — | — | — | — | — | DNF — produced nothing. |

**Drafter ranking:** CLAUDE ≈ CODEX (incumbents) → **DEEPSEEK** (genuinely competitive, best candidate) →
MINIMAX (usable scaffold) → NEMOTRON / GPTOSS (shallow + unreliable) → QWENCODER (DNF).

**Grounding is the round-1→round-2 vindication.** In round 1 (Pattern A) the API models invented paths
(`assets/fixtures/`, `LicenseValidationTest`, …). In round 2 (Pattern B) every agent that ran cited **real**
paths — DeepSeek/MiniMax named the true mapper, manifest, and test files; Claude reproduced six fixture
author names and the exact `boundaryPair` line range verbatim from disk. **Pattern B closes the
hallucination gap empirically.**

## 3. Critiquer axis — quality (1–5)

| Candidate | Score | Why |
|---|---|---|
| **DEEPSEEK** | **5** | Deepest. Found the same #1 flaw (Codex builds-before-eval), praised Claude's fork, and added **original** ideas neither draft had: a *TTA-6 pothos pre-check before thickening fixtures*, and the *offline-CSV-sweep-vs-live-code verification gap*. Even caught a subtle flaw inside Claude's Phase 3A. |
| **MINIMAX** | **5** | Wrote the **concrete manifest stanza** both drafts omitted (`{"plain":0.97,"margin_over_second":0.20}`), flagged the `margin_over_second` *semantics* ambiguity (abs vs ratio vs softmax gap), and noted the device-eval CSV-pull workflow was in A not B. |
| **GPTOSS** *(free)* | 4 | Thorough (tabular); found all core issues + a *pre-fork sanity test* + a *cross-manifest parser test* + splitting doc-cleanup into pre/post-decision. Slightly process-heavy but genuinely useful. |
| **NEMOTRON** *(free)* | 3.5 | Correct on every core point, well-grounded (cited the 0011 ktlint/autocrlf lesson, the `bestProb≥P ∧ (bestProb−secondProb)≥M` logic), but the least original — "no major sequencing issue" for A understates nothing but adds nothing. |
| **QWENCODER** *(free)* | — | Failed (429). |

**Critiquer ranking:** DEEPSEEK ≈ MINIMAX → GPTOSS → NEMOTRON → (QWENCODER failed). **All four that ran
found the decisive issue** (Codex's implement-before-evaluate; Claude's fork is the right posture) — strong
evidence that critique-over-a-supplied-artifact neutralises the no-repo handicap, exactly as the round-1
eval predicted. **Cheap models punch above their weight as critiquers** — and two of the four good critiques
were **free**.

## 4. Cost & latency

- **Total OpenRouter spend for the whole round ≈ $0.09** (paid drafts ~$0.05 + critiques $0.036; the 3 free
  models $0). Incumbent drafts (Codex gpt-5.5, Claude Opus) are billed to their own subscriptions, not OpenRouter.
- Pattern-B drafts ran **159–421 s** (agentic); Pattern-A critiques **64–137 s** (single-shot). The paid
  models were *slower* but *reliable*; the free models were faster only because they failed early.

---

## 5. Verdict & how it updates the §7D picks

1. **Drafter (agentic, Pattern B): paid only.** **`deepseek/deepseek-v4-pro` is confirmed the fallback
   drafter** — reliable agentic write, repo-grounded, a draft competitive with the Codex/Claude incumbents,
   ~$0.03/run. `minimax/minimax-m2.1` is a reliable but *shallower* second. **Drop the free models from the
   drafter picks** — empirically they cannot complete the agentic write (gateway flakiness *or* weak
   tool-calling). This is stronger than §7D's hedged "keep free off the critical path": for *drafting*, free
   is off the table, not just off the critical path.
2. **Critiquer (Pattern A): free is fine.** `gpt-oss-120b:free` and `nemotron-3-super-120b:free` both
   produced genuinely useful critiques at $0. Keep them as **bonus critiquers** with retry-and-skip. Paid
   `deepseek-v4-pro` and `minimax-m2.1` are the best critics but cost a few cents; use one paid + one free
   for diversity.
3. **Avoid `qwen/qwen3-coder:free` entirely right now** — 3/3 failures today (drafting ×2, critique ×1),
   all gateway/rate-limit. Its on-paper strength (best free tool-calling, >70% SWE-bench) is moot if the
   free endpoint won't serve it. Re-test another day before trusting it.
4. **The harness lessons** (for wiring this into `sprint-*`): judge a Pattern-B drafter by the **written
   file**, never stdout (gpt-oss looked "done" at exit 0 with nothing on disk); give free models **one retry
   then skip** (the agy-fallback rule); resolve the OpenRouter key **once per shell**; and note that
   Windows-PowerShell file writes are a real failure surface for weaker agents (nemotron's `$var`-across-
   processes botch).

**One-line bottom line:** *Pattern B makes DeepSeek V4 Pro a real, repo-grounded, ~$0.03 fallback drafter
that rivals the incumbents; free models can't finish the agentic write but make solid free critiquers;
qwen3-coder:free is currently unusable on the free tier.*
