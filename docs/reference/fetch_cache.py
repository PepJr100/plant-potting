#!/usr/bin/env python3
"""
fetch_cache.py — downloads all URLs from docs/reference/urls.md into
docs/reference/cache/.

Safe to re-run: already-OK'd URLs are skipped via status.log.
Uses 10 concurrent threads.

Requirements:
    pip install requests

Usage (from the repo root or anywhere):
    python docs/reference/fetch_cache.py
"""

import os
import re
import sys
import threading
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

try:
    import requests
except ImportError:
    sys.exit("Missing dependency — run:  pip install requests")

# ── paths ──────────────────────────────────────────────────────────────────────
SCRIPT_DIR   = Path(__file__).parent
URLS_MD      = SCRIPT_DIR / "urls.md"
CACHE_DIR    = SCRIPT_DIR / "cache"
STATUS_LOG   = CACHE_DIR / "status.log"

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/124.0.0.0 Safari/537.36"
    ),
    "Accept": "text/html,application/xhtml+xml,application/xhtml+xml,application/pdf,*/*;q=0.8",
    "Accept-Language": "en-GB,en-US;q=0.9,en;q=0.8",
    "Accept-Encoding": "gzip, deflate, br",
    "Referer": "https://www.google.com/",
    "Sec-Ch-Ua": '"Google Chrome";v="124", "Chromium";v="124", "Not-A.Brand";v="99"',
    "Sec-Ch-Ua-Mobile": "?0",
    "Sec-Ch-Ua-Platform": '"macOS"',
    "Sec-Fetch-Dest": "document",
    "Sec-Fetch-Mode": "navigate",
    "Sec-Fetch-Site": "cross-site",
    "Sec-Fetch-User": "?1",
    "Upgrade-Insecure-Requests": "1",
    "Cache-Control": "max-age=0",
}

# Sites that require Cloudflare JS challenge — skip and report for manual save
CLOUDFLARE_BLOCKED = {
    "www.researchgate.net",
}

TIMEOUT   = 30   # seconds per request
WORKERS   = 5    # lower parallelism helps avoid rate-limiting

_log_lock = threading.Lock()

# ── helpers ────────────────────────────────────────────────────────────────────

def url_to_filename(url: str) -> str:
    """Deterministic filename from URL — matches what the bash agents produced."""
    parsed = urllib.parse.urlparse(url)
    path   = parsed.path.rstrip("/")
    ext    = os.path.splitext(path)[1].lower()
    if ext not in {".pdf", ".html", ".htm", ".md", ".txt"}:
        ext = ".html"
    slug = re.sub(r"[^a-zA-Z0-9_-]", "_", parsed.netloc + path)[:120]
    return slug + ext


def already_done(url: str) -> bool:
    if not STATUS_LOG.exists():
        return False
    text = STATUS_LOG.read_text(encoding="utf-8", errors="replace")
    return f"OK {url}" in text


def append_status(line: str) -> None:
    with _log_lock:
        with STATUS_LOG.open("a", encoding="utf-8") as f:
            f.write(line + "\n")


def extract_urls(md_path: Path) -> list[str]:
    """Pull every https?:// URL from the markdown table rows."""
    urls = []
    pattern = re.compile(r"https?://[^\s|)\]\"<>\\]+")
    for line in md_path.read_text(encoding="utf-8").splitlines():
        # Only table data rows (start with |)
        if line.strip().startswith("|"):
            for m in pattern.finditer(line):
                url = m.group(0).rstrip(".,;)")
                if url not in urls:
                    urls.append(url)
    return urls


# ── download worker ────────────────────────────────────────────────────────────

def download(url: str) -> tuple[str, str]:
    """Returns (status, message)."""
    if already_done(url):
        return "SKIP", url

    parsed = urllib.parse.urlparse(url)
    if parsed.netloc in CLOUDFLARE_BLOCKED:
        append_status(f"SKIP_MANUAL {url}")
        return "MANUAL", url

    fname   = url_to_filename(url)
    outfile = CACHE_DIR / fname

    session = requests.Session()
    session.headers.update(HEADERS)

    try:
        resp = session.get(url, timeout=TIMEOUT, allow_redirects=True)
        resp.raise_for_status()

        content = resp.content
        if not content:
            append_status(f"FAIL_EMPTY {url}")
            return "FAIL_EMPTY", url

        outfile.write_bytes(content)
        append_status(f"OK {url} {fname}")
        size_kb = len(content) // 1024
        return "OK", f"{url}  →  {fname}  ({size_kb} KB)"

    except requests.exceptions.HTTPError as e:
        code = e.response.status_code
        # For MDPI, try the /pdf endpoint as a fallback
        if code == 403 and "mdpi.com" in url and not url.endswith("/pdf"):
            try:
                pdf_url = url.rstrip("/") + "/pdf"
                pdf_fname = url_to_filename(pdf_url).replace(".html", ".pdf")
                pdf_resp = session.get(pdf_url, timeout=TIMEOUT, allow_redirects=True)
                pdf_resp.raise_for_status()
                if pdf_resp.content:
                    pdf_out = CACHE_DIR / pdf_fname
                    pdf_out.write_bytes(pdf_resp.content)
                    append_status(f"OK {url} {pdf_fname}  [via /pdf fallback]")
                    size_kb = len(pdf_resp.content) // 1024
                    return "OK", f"{url}  →  {pdf_fname}  ({size_kb} KB) [PDF fallback]"
            except Exception:
                pass  # fall through to normal FAIL
        append_status(f"FAIL {url}  [{code}]")
        return "FAIL", f"{url}  [{code}]"
    except Exception as e:
        append_status(f"FAIL {url}  [{type(e).__name__}]")
        return "FAIL", f"{url}  [{type(e).__name__}: {e}]"


# ── main ───────────────────────────────────────────────────────────────────────

def main() -> None:
    CACHE_DIR.mkdir(parents=True, exist_ok=True)
    if not STATUS_LOG.exists():
        STATUS_LOG.touch()

    if not URLS_MD.exists():
        sys.exit(f"Cannot find {URLS_MD}")

    urls = extract_urls(URLS_MD)
    print(f"Found {len(urls)} URLs in manifest.")

    pending = [u for u in urls if not already_done(u)]
    skipped = len(urls) - len(pending)
    if skipped:
        print(f"Skipping {skipped} already-downloaded URLs.")
    print(f"Downloading {len(pending)} URLs with {WORKERS} workers…\n")

    ok = fail = 0
    with ThreadPoolExecutor(max_workers=WORKERS) as pool:
        futures = {pool.submit(download, u): u for u in pending}
        for future in as_completed(futures):
            status, msg = future.result()
            if status == "OK":
                ok += 1
                print(f"  ✓  {msg}")
            elif status == "SKIP":
                pass
            elif status == "MANUAL":
                print(f"  ⚠  MANUAL SAVE NEEDED: {msg}")
            else:
                fail += 1
                print(f"  ✗  {msg}")

    print(f"\nDone — {ok} downloaded, {fail} failed, {skipped} skipped.")
    print(f"Cache: {CACHE_DIR}")
    print(f"Log:   {STATUS_LOG}")


if __name__ == "__main__":
    main()
