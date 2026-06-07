# Evaluation: NVIDIA NIM & OpenRouter free models as sprint drafters / critiquers

**Status:** DRAFT proposal (idea-funnel). Experiment run 2026-06-07.
**Author:** Claude (Opus 4.8), at principal request.
**Decision asked of reader:** whether to fold any of the *Proposed skill updates* (§7) into the
`sprint-planner` (and `sprint-execute` / `sprint-review`) skills. **No skill files were changed by this
experiment** — this is an evaluation + proposal only.

Raw artifacts (every prompt, draft, critique, metric) live in
[`alt-drafter-models-eval/`](alt-drafter-models-eval/). Reproduce with the harness there
(`run_models.py`, keys via 1Password — see §8).

---

## 1. Why this experiment

The multi-model sprint skills lean on three CLIs — `claude`, `codex`, and `agy` (Antigravity/Gemini).
`agy` has been **quota-blocked** for two sprints running (0011, 0012), repeatedly dropping us to a
two-model plan. The principal asked whether **NVIDIA NIM** (`integrate.api.nvidia.com`) and
**OpenRouter free models** (`openrouter.ai`) could serve as alternative or supplementary drafters /
critiquers — for resilience, diversity, and cost.

The honest question underneath: *do these endpoints add real planning value, or just noise?*

## 2. Method

To make it a **fair, like-for-like test**, every drafter received the **identical** self-contained
brief ([`alt-drafter-models-eval/brief.txt`](alt-drafter-models-eval/brief.txt)) — the same
PLANTPOTTING-0012 pothos↔Pilea sprint context the real `codex`/`claude` drafts were built from,
condensed to remove the "go read the repo" instruction (the API models *can't* read the repo, so
leaving it in would have been an unfair tax). For parity, `codex` and `claude` were **re-run as fresh
drafters under this same brief**, explicitly told to draft from the brief only.

- **Draft pass:** 5 API models + `codex` + `claude`, same brief, full sprint plan in Markdown.
- **Critique pass:** the two reliably-working API models (`gpt-oss-120b`, `qwen3.5-397b`) plus a
  `codex`↔`claude` cross-critique, all critiquing a common target draft with the same instruction.

A hard structural fact shaped everything (see §5): **NIM/OpenRouter expose chat-completion endpoints,
not agentic CLIs.** They cannot read files or write files. To use them at all, the orchestrator (this
Claude session) had to build a wrapper that injects context into the prompt and writes the returned
Markdown to disk itself.

## 3. Models researched

- **OpenRouter:** 341 models total, **27 free** (`:free` suffix, $0 prompt+completion). Strongest free
  general/reasoning options: `moonshotai/kimi-k2.6:free`, `nvidia/nemotron-3-ultra-550b-a55b:free`,
  `openai/gpt-oss-120b:free`, `qwen/qwen3-next-80b-a3b-instruct:free`, `z-ai/glm-4.5-air:free`,
  `meta-llama/llama-3.3-70b-instruct:free`.
- **NVIDIA NIM:** 120 models on the hosted endpoint (free-tier / build.nvidia.com credits, rate-limited,
  no per-token charge on this key). Strongest planning candidates: `qwen/qwen3.5-397b-a17b`,
  `qwen/qwen3.5-122b-a10b`, `deepseek-ai/deepseek-v4-pro`, `nvidia/llama-3.1-nemotron-ultra-253b-v1`,
  `z-ai/glm-5.1`, `minimaxai/minimax-m2.7`, `mistralai/mistral-large-3-675b-instruct-2512`,
  `moonshotai/kimi-k2.6`.

**Shortlist actually tested (5 drafters)** — chosen for breadth across providers + model families:

| Tag | Provider | Model |
|---|---|---|
| `OR-kimi-k2.6` | OpenRouter (free) | `moonshotai/kimi-k2.6:free` |
| `OR-nemotron-ultra` | OpenRouter (free) | `nvidia/nemotron-3-ultra-550b-a55b:free` |
| `OR-gpt-oss-120b` | OpenRouter (free) | `openai/gpt-oss-120b:free` |
| `NV-deepseek-v4-pro` | NVIDIA NIM | `deepseek-ai/deepseek-v4-pro` |
| `NV-qwen3.5-397b` | NVIDIA NIM | `qwen/qwen3.5-397b-a17b` |

## 4. Results

### 4.1 Draft pass (identical brief)

| Drafter | Outcome | Latency | Output | Checkboxes | Notes |
|---|---|---|---|---|---|
| `NV-qwen3.5-397b` | ✅ **OK** | 63 s | 9.1 KB | 39 | Best of the API models; tradeoff table; handled the core challenge reasonably |
| `OR-gpt-oss-120b:free` | ✅ **OK** | 49 s | 8.0 KB | 30 | Competent structure; **mishandled the central challenge** (see §5) |
| `OR-kimi-k2.6:free` | ❌ **429** ×2 | 1 s | — | — | "temporarily rate-limited upstream" both attempts — unusable now |
| `OR-nemotron-ultra-550b:free` | ❌ **504 / 502** ×2 | 1–300 s | — | — | Gateway errors both attempts — unreliable |
| `NV-deepseek-v4-pro` | ❌ **timeout / 504** ×2 | 300 s | — | — | Too slow: blows the NVIDIA **~300 s server-side ceiling** before finishing |
| `CLI-codex` (eval brief) | ✅ OK | ~2 min | 14 KB | **140** | Incumbent; repo-grounded; very granular |
| `CLI-claude` (eval brief) | ✅ OK | ~2 min | 15 KB | 54 | Incumbent; repo-grounded |

**3 of 5 API models failed even after a fair retry.** Two of those are *free* OpenRouter models hitting
upstream rate-limits / gateway errors; one is a frontier NVIDIA reasoning model that is simply too slow.

### 4.2 Critique pass (common target draft)

| Critiquer | Outcome | Latency | Output | Quality |
|---|---|---|---|---|
| `NV-qwen3.5-397b` | ✅ OK | 44 s | 7.0 KB | **Strong, on-context** |
| `OR-gpt-oss-120b:free` | ✅ OK | 76 s | 12 KB | Detailed but some off-context suggestions |
| `CLI-codex` / `CLI-claude` | ✅ OK | ~1–2 min | — | Incumbent baseline |

### 4.3 Cost

Effectively **$0** for this experiment. OpenRouter `:free` models report `cost: 0`. The NVIDIA key bills
no per-token cost (free-tier credits). The real currency here is **rate limits and reliability**, not money.

## 5. Honest quality assessment

### Two structural disadvantages the API models can't escape

1. **No repo access → hallucinated specifics.** Every API draft invented file paths and identifiers that
   don't exist in this repo: `assets/fixtures/`, `fixtures_attribution.txt`, `LicenseValidationTest`,
   `app/src/test/assets/fixtures/`, `"Pilea_peperomioides": 46`, `./gradlew checkLicenseCleanFixtures`,
   etc. The real `ModelScoreMapper.kt`, `plant_class_map.json`, `HousePlantClassMapValidationTest`,
   `model_manifest.json` paths were *never* named correctly. The incumbent CLIs name them right because
   they can grep the tree. **For drafting, repo grounding is decisive.**
2. **They are completion endpoints, not agents.** They can't read the brief file, can't write the draft,
   can't run a gate. Anything beyond "text in → text out" must be built around them by the orchestrator.

### Where they were genuinely good

- **`qwen3.5-397b` (NVIDIA) is the standout.** Its draft had a clean candidate **tradeoff table**
  (A/B/C/D), picked a defensible **B+C hybrid**, sequenced fixtures→measure→implement→TTA correctly, and
  set a real latency-budget acceptance bar. Fast (44–63 s) and reliable across all three calls.
- **Critique is where cheap models punch above their weight.** Both `qwen3.5-397b` and `gpt-oss-120b`
  independently surfaced the *same* high-value gaps that the repo-grounded `codex`/`claude` critiques
  found: **over-abstention on correct pothos is never gated**; the **baseline is sequenced after fixture
  expansion**; there's **no quantitative confident-wrong target**; you **can't test Pilea routing before
  the mapping exists**. `qwen` also added a genuinely good idea neither incumbent stressed — a curated
  **"poison set" of hard-negative pothos images** that must never produce a confident Pilea card, plus
  **committing the pre-change CSV as a locked regression baseline**. Critique is reasoning-over-a-supplied-
  artifact, so the no-repo-access handicap mostly disappears.

### Where they were bad / risky

- **`gpt-oss-120b` mishandled the central challenge** in both its draft and critique: it reverted to
  **margin-based gating** (top-1 pothos AND top-2 Pilea, margin < 0.30) — exactly what the brief says
  *cannot* work, because the 0.9661 error has no second-place mass. It also repeatedly proposed
  **synthetic augmentation / style-transfer** to pad the thin Pilea set (violates the no-self-shot,
  honest-eval rule) and **unrealistic fixture counts** (≥100 clean Pilea when the ceiling is ~76).
- **`gpt-oss` critique drifted into enterprise process** that doesn't fit this project: **canary /
  remote-config rollout to 1% of devices** (impossible — the app is network-free, on-device), `@owner`
  handles and a two-approver PR policy (solo principal), etc. Useful signal buried in inapplicable noise.
- **`qwen` had minor off-context items** too (physical-device/thermal-throttling validation, when this
  project deliberately uses the `pixel6Api34` emulator) — but far fewer.

### Verdict on raw quality

For **drafting**, the incumbents (`codex`/`claude`) are clearly better because they ground in the repo;
the best API model (`qwen3.5-397b`) is a **solid B+** first draft whose value the Opus merge could
already extract — but it needs repo anchors injected to stop hallucinating. For **critiquing**, the gap
narrows sharply: `qwen3.5-397b` produced a critique fully competitive with the incumbents and added
original, on-context ideas. `gpt-oss-120b:free` is a usable but noisier second opinion.

## 6. Reliability findings (the real story)

| Finding | Evidence | Implication |
|---|---|---|
| **Free OpenRouter models are rate-limited / flaky** | `kimi:free` 429 ×2; `nemotron-ultra:free` 504+502 | Don't put a free `:free` model on the critical path |
| **NVIDIA has a ~300 s server-side response ceiling** | `deepseek-v4-pro` 504 at 302 s (client timeout ruled out) | Avoid big reasoning models for long (8k-token) generations; keep client timeout ≤ ~250 s and prefer fast MoE models |
| **`qwen3.5-397b` (NVIDIA) was reliable & fast** | 3/3 calls OK, 44–63 s | Best single API candidate for integration |
| **`gpt-oss-120b:free` (OpenRouter) was reliable enough** | 2/2 calls OK, 49–76 s | Acceptable free second-opinion critiquer |
| **Parallel `op` calls can race to an empty key** | one deepseek retry got HTTP 500 "Authorization not found" | Resolve the key **once** per shell, not concurrently across background subshells |

## 7. Proposed skill updates (NOT applied — for the principal to approve)

Framed by the core insight: **these models are mediocre drafters but strong critiquers**, and the most
reliable one is `qwen3.5-397b` on NVIDIA. So the highest-value, lowest-risk moves are about *critique
breadth* and *drafter resilience*, not replacing the incumbents.

**Two integration patterns exist, and they have very different ceilings:**

- **Pattern A — completion wrapper** (what the experiment used): the orchestrator injects context into a
  prompt, POSTs to `/chat/completions`, and writes the returned Markdown itself. Simple, but the model is
  *blind to the repo* → it hallucinates file paths. Fine for **critiquing** (the artifact is in the
  prompt); weak for **drafting**.
- **Pattern B — agentic CLI backend** (see §7B): drive the model through an agent harness that accepts an
  OpenAI-compatible base URL, so it **reads the repo and writes files itself**, exactly like
  `codex`/`claude`/`agy`. This **closes the hallucination gap** and is the better path for a *drafter*.
  Both NIM and OpenRouter are OpenAI-compatible and support tool-calling for the relevant models
  (`qwen3.5-397b`, Llama 3.x, Mistral, GLM 4.7/5.1, Kimi K2, Nemotron-3-Super).

### 7A. Pattern A items (completion wrapper)

1. **Add a wrapper helper to the skill** — `scripts/api-model.py` (or similar) that takes
   `(provider, model, prompt-file, out-file)`, reads the key from 1Password, POSTs to the OpenAI-compatible
   endpoint, and writes the response. This is the missing piece that lets a non-agentic model participate.
   The experiment's `run_models.py` is a working prototype to adapt.
   - [ ] Decide whether to ship this wrapper in the skill.

2. **Add cheap API models as *extra critiquers* (recommended, highest ROI).** After the three CLI drafts,
   also run `NV-qwen3.5-397b` and `OR-gpt-oss-120b:free` as cross-critiquers (orchestrator injects the
   draft text, writes the critique file). The Opus merge then weighs **5 critiques instead of 3** — and
   the evidence shows these two reliably find real, independent gaps.
   - [ ] Approve adding 1–2 API critiquers to `sprint-planner` step 5.

3. **Designate `NV-qwen3.5-397b` as the *fallback drafter* when `agy` is quota-blocked.** It restores a
   true three-draft pass without waiting ~3 days for the agy quota to reset. The wrapper must **inject the
   repo anchors** (the file paths from the real brief) into its prompt to suppress hallucination, and the
   Opus merge still corrects residual drift.
   - [ ] Approve `qwen3.5-397b` as the agy fallback in `sprint-planner` step 4.

4. **Guardrails to encode in the skill if any of the above land:**
   - [ ] Keep API models **off the critical drafting path** unless agy is down (incumbents first).
   - [ ] **Never** use a free OpenRouter `:free` model where reliability matters; if used, retry with
     backoff and treat failure as "skip this drafter," exactly like the agy-quota fallback.
   - [ ] Client timeout ≤ ~250 s; **avoid** `deepseek-v4-pro` / 250B+ reasoning models (NVIDIA 300 s ceiling).
   - [ ] Resolve each 1Password key **once per shell**; never echo it; never commit it.
   - [ ] Treat all API-model output as **un-grounded** — the Opus merge and the repo-grounded CLI drafts
     are the correctness backstop; an API draft alone must never become `{SID}.md`.

5. **Explicitly reject** using these as primary *executors* (`sprint-execute`) — execution requires
   reading and writing many repo files and running gates, which only the agentic CLIs can do.

### 7B. Pattern B — drive NIM/OpenRouter through an *agentic* harness (recommended for a real fallback drafter)

The skills already shell out to agentic CLIs (`codex`/`claude`/`agy`). The cleanest way to add a
NIM/OpenRouter model as a *grounded* drafter is to add another agentic CLI backed by one of these
endpoints — it reads the repo and writes its own draft, so it does **not** hallucinate paths. Verified
options (June 2026):

6. **Codex CLI with a custom provider (best fit — reuses the existing pattern).** Codex CLI reads
   `~/.codex/config.toml`; add a provider block and select it per invocation. This is the lowest-friction
   route because the skills already invoke `codex`.
   ```toml
   [model_providers.nvidia]
   base_url = "https://integrate.api.nvidia.com/v1"
   env_key = "NVIDIA_API_KEY"          # never hard-code the key
   wire_api = "chat"                    # gateways implement /chat/completions, not /responses
   requires_openai_auth = false         # key isn't an sk- prefix
   # (OpenRouter variant: base_url = "https://openrouter.ai/api/v1")
   ```
   Invoke: `codex --config model_provider=nvidia --config model=qwen/qwen3.5-397b-a17b exec "<prompt>"`.
   Reserved provider IDs are `openai`/`ollama`/`lmstudio` — use a different name.
   - [ ] Approve a **codex-with-NVIDIA-`qwen3.5-397b` profile as the `agy`-quota fallback drafter**
     (preferred over the Pattern-A item 3, because it's repo-grounded).

7. **OpenCode or Aider as an additional agentic CLI.** Both accept OpenRouter natively and NIM via a
   custom OpenAI-compatible provider; OpenCode is the open-source Claude-Code-alike. Heavier than reusing
   codex, but viable if a second independent agent harness is wanted.
   - [ ] Consider only if codex-with-custom-provider proves insufficient.

8. **`claude-code-router` (ccr) — only if you want NIM/OpenRouter models running *as Claude Code
   subagents*.** ccr proxies Anthropic ↔ OpenAI-compatible and supports per-subagent model override
   (`<CCR-SUBAGENT-MODEL>provider,model</CCR-SUBAGENT-MODEL>` at the top of the subagent prompt). Caveat:
   it reroutes the **whole** Claude Code session through the proxy ("run Claude Code on cheaper models"),
   not "add one NVIDIA subagent to an otherwise-Claude session" — so it's a heavier, all-or-nothing switch
   and **not** recommended for the sprint skills (Pattern 6 keeps the main Claude session intact).
   - [ ] Note as an option; not recommended for `sprint-*`.

**Pattern B caveats (agentic loops are harsher than one-shot calls):**
   - [ ] Needs reliable **tool-calling** — confirmed on NIM for `qwen3.5-397b`, Llama 3.1/3.2/3.3,
     Mistral, GLM 4.7/5.1, Kimi K2, Nemotron-3-Super; free OpenRouter models' tool-calling is spottier.
   - [ ] An agent makes **many** calls, so the free-tier rate-limits and NVIDIA's ~300 s ceiling bite
     *harder* mid-loop — keep free `:free` models off the agentic path; `qwen3.5-397b` on NIM is the reliable pick.
   - [ ] Still ~$0 but burns far more tokens than single-shot drafting → rate limits arrive sooner.

### 7C. Paid-tier option — OpenRouter credit (principal adding $10, 2026-06-07)

**Decision:** the principal is adding **$10 to OpenRouter, primarily for the free-tier bonus.** Purchasing
≥$10 in credit *once* raises the OpenRouter `:free` daily limit from **50 → 1000 requests/day** and the
boost **persists even if the balance later drops below $10** (a **20 req/min** ceiling still applies). The
per-token credit is a secondary benefit, not the main motive.

**What the $10 actually buys (verified June 2026):**

| Lever | Effect | Relevance to `sprint-*` |
|---|---|---|
| Free-tier limit 50 → **1000/day** | The reliable OpenRouter free lane (`openai/gpt-oss-120b:free`) becomes usable at sprint cadence | Main reason for the spend |
| Paid routes bypass `:free` contention | Removes the `kimi:free` 429 / `nemotron:free` 502/504 flakiness *if* a paid route is chosen | Reliability backstop |
| Paid **DeepSeek V4 Pro** reachable | ~**$0.44/M in, $0.87/M out** (cache-hit in $0.0036/M). The model that 504'd on **NIM's ~300 s ceiling** runs fine here (OpenRouter upstreams have generous timeouts + streaming) | Unblocks DeepSeek for us |
| Paid **Qwen 3.7 Max** reachable | ~**$1.25/M in, $3.75/M out**, **90% cache discount ($0.25/M)**, 1M ctx, agent-tuned | Strong grounded-drafter candidate for Pattern B |

> **Scope note:** the $10 boosts **OpenRouter** `:free` models only. `qwen3.5-397b` lives on **NVIDIA NIM**
> (a separate provider) and is unaffected — but it was already free + reliable there, so no loss.

**What the credit does NOT fix:** the **grounding gap is architectural, not a model-quality issue.** A paid
completion model in **Pattern A** is still blind to the repo and still hallucinates file paths — so paying
for Pattern-A *drafting* spends money on the wrong axis. The payoff is **paid model + Pattern B** (agentic
backend): a grounded, high-quality drafter.

**Cost at this cadence (~1 sprint / 1–2 days):**
- Pattern A one-shot (~4k tokens): DeepSeek V4 Pro ≈ **$0.002**/call; Qwen 3.7 Max ≈ **$0.011**/call — negligible.
- Pattern B agentic (~50–200k tokens/run): DeepSeek V4 Pro ≈ **~$0.10–0.15**/run; Qwen 3.7 Max ≈
  **~$0.4–0.7**/run (its 90% cache discount lowers repeat-context runs). **$10 covers dozens-to-hundreds of runs.**

**Recommended allocation:**
- [ ] **Drafting (where paid + grounding pays off):** if reaching for a paid model, drive **DeepSeek V4 Pro**
  (cheapest) or **Qwen 3.7 Max** (strongest agent-tuned) through **Pattern B** (codex custom provider, §7B).
- [ ] **Critiquing (keep cheap):** stay on free `qwen3.5-397b` (NIM) + the now-1000/day `gpt-oss-120b:free`;
  add paid DeepSeek V4 Pro only for a diverse second opinion. Don't pay Qwen-Max rates for critiques.
- [ ] **Do not** spend on paid Pattern-A *drafting* — same hallucination ceiling as the free models.
- [ ] Mind the **20 RPM** ceiling (fine for the skills' handful-of-calls-per-phase pattern; only a concern under heavy fan-out).

## 8. Reproduce / appendix

- Keys: 1Password items `Nvidia_DarkFactoryProjectAIKey` and `Openrouter_DarkFactoryProjectAIKey`,
  field `credential`. Read with `op item get "<item>" --fields credential --reveal` (desktop-app
  integration authorizes per command; `op whoami` may say "not signed in" yet item reads still work).
- Endpoints (both OpenAI-compatible `/chat/completions`):
  `https://integrate.api.nvidia.com/v1` (NVIDIA), `https://openrouter.ai/api/v1` (OpenRouter).
- Harness: [`alt-drafter-models-eval/run_models.py`](alt-drafter-models-eval/run_models.py)
  (`draft` / `critique` modes; per-tag filtering; metrics JSON).
- Outputs: [`alt-drafter-models-eval/drafts/`](alt-drafter-models-eval/drafts/),
  [`alt-drafter-models-eval/critiques/`](alt-drafter-models-eval/critiques/),
  `metrics-*.json`.
- One-line bottom line: **Adopt `qwen3.5-397b` (NVIDIA) as an extra critiquer now (Pattern A); for the
  agy fallback *drafter*, prefer Pattern B — a codex CLI custom-provider profile pointed at NIM
  `qwen3.5-397b` — so the fallback is repo-grounded. Skip the flaky free OpenRouter models on the critical
  path; keep the agentic CLIs as the grounded core.** The principal added **$10 OpenRouter credit
  (2026-06-07)** mainly for the free-tier boost (50 → 1000 `:free` req/day); paid DeepSeek V4 Pro /
  Qwen 3.7 Max are now reachable and worth spending **only on Pattern-B (agentic) drafting**, not Pattern-A
  — see §7C.

### Pattern B references (verified June 2026)
- Codex CLI custom provider: [OpenRouter × Codex CLI](https://openrouter.ai/docs/guides/coding-agents/codex-cli),
  [custom OAI-compatible provider setup](https://ofox.ai/blog/codex-cli-custom-model-providers-byo-setup/),
  [Codex advanced config](https://developers.openai.com/codex/config-advanced).
- NIM tool/function calling: [NIM LLM function-calling docs](https://docs.nvidia.com/nim/large-language-models/1.10.0/function-calling.html).
- OpenCode / Aider: [OpenRouter × OpenCode](https://openrouter.ai/docs/cookbook/coding-agents/opencode-integration),
  [NVIDIA NIM + OpenCode (free tier)](https://medium.com/@vignarajj/beyond-the-hype-supercharging-my-local-development-workflow-with-nvidia-nim-and-opencode-free-6ff12d6f851e).
- Claude Code subagents via proxy: [claude-code-router](https://github.com/musistudio/claude-code-router),
  [Claude Code on OpenRouter/NIM/Ollama](https://www.mindstudio.ai/blog/claude-code-cheaper-models-openrouter-nvidia-nim-ollama).
