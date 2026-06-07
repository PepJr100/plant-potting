#!/usr/bin/env python3
"""PLANTPOTTING-0012 Phase 1 — stage license-clean iNaturalist candidates for visual vetting.

Downloads ORIGINAL-size JPEGs for a taxon under an accepted license set into a staging
dir inside the project tree (so the sandbox overlay persists them). Writes a meta JSONL
sidecar with everything the fixture-manifest.tsv row + license attribution needs.

Nothing here is accepted automatically — each staged image is then Read (visually vetted)
before being promoted into app/src/androidTest/assets/identify-fixtures/.

Usage: python3 stage_inat.py "<taxon name>" <slug> <cc0|cc-by|all> <max>
"""
import json, subprocess, sys, urllib.parse, os, re
try:
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
except Exception:
    pass

STAGE = os.path.join(os.path.dirname(__file__), "_staging")
os.makedirs(STAGE, exist_ok=True)

taxon, slug, lic_arg, maxn = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4])
licenses = ["cc0", "cc-by"] if lic_arg == "all" else [lic_arg]
research = (sys.argv[5] == "research") if len(sys.argv) > 5 else False

LIC_URL = {
    "cc0": "https://creativecommons.org/publicdomain/zero/1.0/",
    "cc-by": "https://creativecommons.org/licenses/by/4.0/",
}
LIC_LABEL = {"cc0": "CC0", "cc-by": "CC BY 4.0"}

def curl(url):
    r = subprocess.run(["curl", "-s", "--max-time", "40", url], capture_output=True)
    return r.stdout.decode("utf-8", errors="replace")

enc = urllib.parse.quote(taxon)
meta_path = os.path.join(STAGE, f"{slug}__meta.jsonl")
seen_users = {}
rows = []
for lic in licenses:
    q = (f"https://api.inaturalist.org/v1/observations?taxon_name={enc}"
         f"&photo_license={lic}&per_page=50&order_by=votes&order=desc")
    if research:
        q += "&quality_grade=research"
    data = json.loads(curl(q) or "{}")
    for o in data.get("results", []):
        tname = (o.get("taxon") or {}).get("name", "")
        user = o.get("user") or {}
        author = user.get("name") or user.get("login") or "unknown"
        login = user.get("login") or "unknown"
        for ph in o.get("photos", []):
            code = ph.get("license_code")
            if code not in ("cc0", "cc-by"):
                continue
            url = ph.get("url", "")
            orig = re.sub(r"/(square|small|medium|large)\.", "/original.", url)
            if not orig:
                continue
            rows.append({
                "obs_id": o.get("id"), "photo_id": ph.get("id"),
                "taxon": tname, "author": author, "login": login,
                "license_code": code, "license": LIC_LABEL[code],
                "license_url": LIC_URL[code],
                "source_url": f"https://www.inaturalist.org/observations/{o.get('id')}",
                "orig_url": orig,
            })

# de-dup by observation, prefer one photo per distinct user for independence
rows.sort(key=lambda r: (r["license_code"] != "cc0",))  # cc0 first
picked = []
per_user = {}
for r in rows:
    if per_user.get(r["login"], 0) >= 1:
        continue
    per_user[r["login"]] = per_user.get(r["login"], 0) + 1
    picked.append(r)
    if len(picked) >= maxn:
        break

with open(meta_path, "w", encoding="utf-8") as mf:
    for i, r in enumerate(picked, 1):
        fn = f"{slug}__cand{i:02d}.jpg"
        dst = os.path.join(STAGE, fn)
        subprocess.run(["curl", "-s", "--max-time", "60", "-o", dst, r["orig_url"]])
        size = os.path.getsize(dst) if os.path.exists(dst) else 0
        r["staged_file"] = fn
        r["bytes"] = size
        mf.write(json.dumps(r) + "\n")
        print(f"{fn}\t{size}B\t{r['license']}\t{r['author']}\t{r['taxon']}\t{r['source_url']}")

print(f"\nStaged {len(picked)} candidates for {taxon} -> {STAGE}")
