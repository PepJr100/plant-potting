# Wiring OpenRouter into Codex as an *agentic*, repo-grounded drafter

**Status:** verified working 2026-06-08 (codex-cli 0.137.0). Companion to
[`alt-drafter-models-eval.md`](alt-drafter-models-eval.md) (§7) and its
[`round2/EVALUATION.md`](alt-drafter-models-eval/round2/EVALUATION.md) road-test.

This is the **how-to** for driving an OpenRouter model through the Codex CLI's own agent harness, so the
model **reads the repo and writes its own files** — exactly like `codex`/`claude`/`agy`, and unlike a raw
chat-completion wrapper that is blind to the repo and hallucinates file paths. Used as the **fallback
drafter/critiquer** when `agy` is quota-blocked or `codex` fails (see the `sprint-planner` / `roadmap`
skills). The default model is **`deepseek/deepseek-v4-pro`** — the only candidate that proved both reliable
and repo-grounded in the round-2 evaluation.

---

## 1. Why this works (and why the naïve version doesn't)

There are two ways to use an OpenRouter model:

- **Pattern A — chat-completion wrapper.** POST the prompt to `/chat/completions`, write the reply yourself.
  Simple, but the model is *blind to the repo* → it invents paths (`assets/fixtures/`, tests that don't
  exist, …). Fine for **critiquing** (the artifact is in the prompt); weak for **drafting**.
- **Pattern B — agentic CLI backend (this doc).** Point the Codex CLI at OpenRouter via a custom provider.
  Codex runs its normal tool loop *using the OpenRouter model as the brain* — it greps the tree, reads
  files, and writes its draft to disk. This **closes the hallucination gap**. It is the right pattern for a
  drafter.

Round-2 proof: under Pattern B, `deepseek-v4-pro` cited only **real** paths (the true `ModelScoreMapper.kt`,
`model_manifest.json`, the real test classes) — the round-1 path-hallucination problem disappeared.

## 2. Prerequisites

- **Codex CLI ≥ 0.137** (`codex --version`). The wire-API requirement below changed at 0.137.
- **An OpenRouter API key** with credit. `deepseek-v4-pro` is a *paid* route (~$0.43/M in, $0.87/M out →
  **~$0.03 per grounded draft**). A one-time **$10 top-up** also raises the free-tier limit to 1000 req/day,
  but the fallback drafter itself is the paid DeepSeek route. Key lives in 1Password:
  item `Openrouter_DarkFactoryProjectAIKey`, field `credential`.
- **`op` (1Password CLI)** signed in (desktop-app integration authorizes per command).

## 3. The config block

Add to `~/.codex/config.toml` (on Windows: `C:\Users\<you>\.codex\config.toml`). This file is **machine-local
and not in any repo** — the key is *never* hard-coded here; codex reads it from the `env_key` env var.

```toml
[model_providers.openrouter]
name = "OpenRouter"
base_url = "https://openrouter.ai/api/v1"
env_key = "OPENROUTER_API_KEY"
wire_api = "responses"        # see CAVEAT — "chat" no longer works on codex 0.137+
requires_openai_auth = false  # key isn't an sk- prefix
```

> **⚠️ CAVEAT — `wire_api = "chat"` is dead on codex 0.137+.** It hard-errors with
> *"`wire_api = "chat"` is no longer supported … set `wire_api = "responses"`"*
> ([openai/codex#7782](https://github.com/openai/codex/discussions/7782)). OpenRouter **does** expose a
> Responses-compatible endpoint (`POST https://openrouter.ai/api/v1/responses` → HTTP 200,
> `object: "response"`), so `wire_api = "responses"` works. (NVIDIA NIM's `/responses` support is
> *unverified* — OpenRouter is the confirmed path.)

Reserved provider IDs are `openai` / `ollama` / `lmstudio` — don't reuse those names.

## 4. Invocation

Resolve the key **once per shell** (parallel `op` calls can race to an empty key), then invoke:

```bash
export OPENROUTER_API_KEY="$(op item get "Openrouter_DarkFactoryProjectAIKey" --fields credential --reveal)"

codex --config model_provider=openrouter \
      --config model=deepseek/deepseek-v4-pro \
      exec --dangerously-bypass-approvals-and-sandbox "<your prompt — tell it to write its file>"
```

The prompt should instruct the model to **explore the repo and write its output to a specific file path**
(the same prompt you'd give `codex`/`agy`), because success is judged by the **written file**, not stdout.

## 5. Two non-fatal quirks to expect

1. **Model-list refresh noise.** Codex tries to GET OpenRouter's `/models` and can't parse it (it expects a
   `models` field; OpenRouter returns `data`), so it dumps ~0.4–1.5 MB of catalog JSON to **stderr**. This is
   **harmless** — the `exec` still completes. Never gate success on stdout; check the written file.
2. **Windows path gotcha (if you script around this).** Git-Bash's `/tmp` ≠ Windows-Python's `/tmp`, and
   Windows Python needs `D:/...` not `/d/...`. If a helper script can't find the key file or a repo file,
   that's why. Prefer passing the key via the `OPENROUTER_API_KEY` env var over a temp file.

## 6. Reliability findings (road-test, 2026-06-08)

| Model | As Pattern-B **drafter** | As Pattern-A **critiquer** |
|---|---|---|
| **`deepseek/deepseek-v4-pro`** (paid) | ✅ reliable, repo-grounded, rivals codex/claude, ~$0.03/run | ✅ best — original, on-context |
| `minimax/minimax-m2.1` (paid) | ✅ reliable but thinner | ✅ excellent |
| `openai/gpt-oss-120b:free` | ❌ generated a draft but **never wrote the file** (printed to stdout) | ✅ useful, $0 |
| `nvidia/nemotron-3-super-120b:free` | ❌ **botched a multi-call shell write, timed out** | ✅ useful, $0 |
| `qwen/qwen3-coder:free` | ❌❌ **OpenRouter gateway "high demand" — failed every attempt** | ❌ 429 |

**Rules of thumb:**
- **Drafting (agentic): paid only.** Free models cannot reliably complete the agentic write loop — gateway
  flakiness or weak tool-calling. `deepseek-v4-pro` is the pick.
- **Critique (single-shot): free is fine.** Use a free model as a bonus critiquer with retry-and-skip.
- **Treat the fallback like the agy fallback:** one retry, then skip and proceed with fewer models — never
  block the sprint on it.

## 7. Verify the setup (3 quick tests)

```bash
export OPENROUTER_API_KEY="$(op item get "Openrouter_DarkFactoryProjectAIKey" --fields credential --reveal)"
cd <your repo>
# 1. Smoke — connectivity/auth/wire_api:
codex --config model_provider=openrouter --config model=deepseek/deepseek-v4-pro exec \
  --dangerously-bypass-approvals-and-sandbox "Reply with exactly one word: PONG"
# 2. Grounding — ask a repo-specific question; confirm it cites REAL paths (no hallucination).
# 3. Write — ask it to write a small file to a known path; confirm the file appears on disk.
```

If all three pass, the provider is wired correctly and ready to use as the fallback drafter.
