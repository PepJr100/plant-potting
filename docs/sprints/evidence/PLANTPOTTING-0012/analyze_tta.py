#!/usr/bin/env python3
"""PLANTPOTTING-0012 Phase 7 — TTA sweep analysis (×6/×8/×10/×20, gated pipeline).

Reads tta-sweep.csv and emits tta-sweep-decision.md: per-level clean / perturbation / pothos-Pilea
metrics, direct-card accuracy, confident-wrong, low-confidence rate, median + worst latency. Rejects
levels whose worst latency exceeds ~2 s; compares survivors against the gated ×6 production candidate.
"""
import csv, os, sys
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(__file__)
POTHOS_ID, PILEA_ID = "epipremnum-aureum", "pilea-peperomioides"
LAT_CAP_MS = 2000
rows = list(csv.DictReader(open(os.path.join(HERE, "tta-sweep.csv"), encoding="utf-8")))
for r in rows:
    r["latency_ms"] = int(r["latency_ms"])
# modes in first-seen order from the CSV (tta6 control first, then the grid levels)
modes = []
for r in rows:
    if r["mode"] not in modes:
        modes.append(r["mode"])

def cut(mode, which):
    rr = [r for r in rows if r["mode"] == mode]
    if which == "clean":    return [r for r in rr if r["perturbation"] == "clean"]
    if which == "perturb":  return [r for r in rr if r["perturbation"] != "clean"]
    if which == "boundary": return [r for r in rr if r["expected_species_id"] in (POTHOS_ID, PILEA_ID)]
    return rr

def metrics(rr):
    n = len(rr)
    if not n: return None
    top1 = sum(1 for r in rr if r["route"] == "high-conf" and r["mapped_top1_kb_id"] == r["expected_species_id"]) / n
    cw = sum(1 for r in rr if r["confident_wrong"] == "true") / n
    low = sum(1 for r in rr if r["route"] == "low-conf") / n
    return n, top1, cw, low

def lat(mode):
    ls = sorted(r["latency_ms"] for r in rows if r["mode"] == mode and r["latency_ms"] >= 0)
    return ls[len(ls)//2], ls[-1]

out = ["# PLANTPOTTING-0012 — TTA sweep decision (Phase 7)\n",
       "Gated production pipeline (pothos↔Pilea gate ON) at TTA ×6/×8/×10/×20 over the expanded "
       "fixture set. Latency cap ~2 s (worst observed). The boundary gate is deterministic and "
       "TTA-independent (top-1=Pilea → picker), so TTA is judged only on overall confident-wrong / "
       "accuracy within the latency budget.\n\n"]
out.append("| level | surface | n | top1-acc | confident-wrong | low-conf | med lat | worst lat |\n")
out.append("|---|---|---|---|---|---|---|---|\n")
for mode in modes:
    med, worst = lat(mode)
    for which, label in [("clean","clean"),("perturb","perturb"),("boundary","pothos/Pilea")]:
        m = metrics(cut(mode, which))
        if not m: continue
        n, top1, cw, low = m
        latcol = f"{med} | {worst}" if which == "clean" else "· | ·"
        out.append(f"| {mode} | {label} | {n} | {top1:.3f} | {cw:.3f} | {low:.3f} | {latcol} |\n")
out.append("\n## Latency vs cap\n| level | median ms | worst ms | within ~2s? |\n|---|---|---|---|\n")
for mode in modes:
    med, worst = lat(mode)
    out.append(f"| {mode} | {med} | {worst} | {'yes' if worst <= LAT_CAP_MS else 'NO — REJECT'} |\n")

# decision logic
base = "tta6"
bclean = metrics(cut(base, "clean"))
out.append("\n## Decision\n")
out.append(f"Gated ×6 production candidate — clean: top1={bclean[1]:.3f}, cw={bclean[2]:.3f}.\n\n")
best = None
for mode in modes:
    if mode == base: continue
    med, worst = lat(mode)
    if worst > LAT_CAP_MS:
        out.append(f"- {mode}: REJECTED (worst {worst} ms > {LAT_CAP_MS} ms cap).\n")
        continue
    m = metrics(cut(mode, "clean"))
    dcw = m[2] - bclean[2]
    dtop1 = m[1] - bclean[1]
    # Adopt ONLY if confident-wrong meaningfully IMPROVES and accuracy does not regress (0006 discipline:
    # never adopt a candidate that trades MORE confident-wrong for a top-1 bump).
    win = (dcw <= -0.02) and (dtop1 >= -0.01)
    verdict = "meaningful improvement" if win else \
        ("higher top1 but MORE confident-wrong (reject)" if dtop1 >= 0.02 else "no meaningful gain (noise)")
    out.append(f"- {mode}: clean cw {m[2]:.3f} (Δ{dcw:+.3f}), top1 {m[1]:.3f} (Δ{dtop1:+.3f}), worst {worst} ms → {verdict}.\n")
    if win:
        best = mode
out.append("\n**Adopted:** " + (f"`tta={best[3:]}`" if best else "**`tta = 6` (unchanged)**") +
           ". " + ("A higher level cleared the meaningful-improvement bar within budget." if best else
           "No higher level cleared a meaningful confident-wrong/accuracy improvement over gated ×6 "
           "within the ~2 s budget; the gate (not TTA) is what fixes the boundary, so the extra "
           "latency of ×8/×10/×20 buys nothing material. `model_manifest.json` `tta` stays 6.") + "\n")
open(os.path.join(HERE, "tta-sweep-decision.md"), "w", encoding="utf-8").write("".join(out))
print("".join(out))
