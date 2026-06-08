#!/usr/bin/env python3
"""Promote vetted staged candidates into identify-fixtures/ and emit manifest TSV rows.

Reads <slug>__meta.jsonl from _staging, copies the chosen cand files to the fixtures dir
under the __NN convention, and prints the fixture-manifest.tsv rows to append.
"""
import json, os, shutil, sys, datetime
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

HERE = os.path.dirname(__file__)
STAGE = os.path.join(HERE, "_staging")
FIX = os.path.abspath(os.path.join(HERE, "..", "..", "..", "..",
                                   "app", "src", "androidTest", "assets", "identify-fixtures"))

# slug -> { cand_file_index : new_NN }
PROMOTIONS = {
    "pilea-peperomioides": {1: "01", 2: "02", 5: "03", 7: "04", 9: "05", 10: "06"},
    "epipremnum-aureum":   {2: "02", 3: "03", 4: "04", 8: "05", 12: "06"},
    # 8 previously-untested mapped species (one clean base photo each).
    "saintpaulia-ionantha":   {2: "01"},
    "chamaedorea-elegans":    {1: "01"},
    "beaucarnea-recurvata":   {2: "01"},
    "alocasia":               {3: "01"},
    "dracaena":               {2: "01"},
    "begonia":                {2: "01"},
    "ctenanthe":              {2: "01"},
    "schlumbergera-bridgesii":{2: "01"},
}
_UNTESTED = "PLANTPOTTING-0012 Phase 1 (iNaturalist CC0). New fixture — previously-untested mapped species."
NOTE = {
    "pilea-peperomioides": "PLANTPOTTING-0012 Phase 1 (iNaturalist CC0). New Pilea fixture (boundary pair); round peltate leaves.",
    "epipremnum-aureum":   "PLANTPOTTING-0012 Phase 1 (iNaturalist CC0). New pothos fixture (boundary pair).",
    "saintpaulia-ionantha": _UNTESTED, "chamaedorea-elegans": _UNTESTED,
    "beaucarnea-recurvata": _UNTESTED, "alocasia": _UNTESTED, "dracaena": _UNTESTED,
    "begonia": _UNTESTED, "ctenanthe": _UNTESTED, "schlumbergera-bridgesii": _UNTESTED,
}

def meta_for(slug):
    out = {}
    with open(os.path.join(STAGE, f"{slug}__meta.jsonl"), encoding="utf-8") as f:
        for line in f:
            r = json.loads(line)
            idx = int(r["staged_file"].split("cand")[1].split(".")[0])
            out[idx] = r
    return out

rows = []
for slug, promos in PROMOTIONS.items():
    meta = meta_for(slug)
    for cand_idx, nn in sorted(promos.items(), key=lambda kv: kv[1]):
        r = meta[cand_idx]
        src = os.path.join(STAGE, r["staged_file"])
        dstname = f"{slug}__{nn}.jpg"
        dst = os.path.join(FIX, dstname)
        shutil.copyfile(src, dst)
        rows.append("\t".join([
            dstname, slug, r["source_url"], r["author"], r["license"],
            r["license_url"], "2026-06-07", NOTE[slug],
        ]))
        print(f"copied {r['staged_file']} -> {dstname}  ({r['author']}, {r['license']})")

print("\n--- MANIFEST ROWS ---")
for row in rows:
    print(row)
with open(os.path.join(HERE, "_manifest_rows.tsv"), "w", encoding="utf-8") as f:
    f.write("\n".join(rows) + "\n")
