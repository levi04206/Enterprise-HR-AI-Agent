from pathlib import Path
import json
import sys
import urllib.request


sys.stdout.reconfigure(encoding="utf-8", errors="replace")
OUT = Path("target/template-repo")
OUT.mkdir(parents=True, exist_ok=True)


def read_url(url):
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "codex-resume-formatter",
            "Accept": "application/vnd.github+json",
        },
    )
    with urllib.request.urlopen(req, timeout=20) as resp:
        return resp.read().decode("utf-8", errors="replace")


def main():
    api = "https://api.github.com/repos/daoge668/template/contents"
    try:
        data = json.loads(read_url(api))
    except Exception as exc:
        print(f"FETCH_FAILED: {exc}")
        return
    print("# ROOT")
    for item in data:
        print(f"{item.get('type')}: {item.get('name')} {item.get('download_url') or ''}")
        if item.get("type") == "file" and item.get("download_url"):
            name = item["name"].lower()
            if name.endswith((".md", ".txt", ".html", ".css", ".tex")):
                try:
                    content = read_url(item["download_url"])
                    path = OUT / item["name"]
                    path.write_text(content, encoding="utf-8")
                    print(f"SAVED: {path}")
                    print(content[:3000])
                except Exception as exc:
                    print(f"FILE_FAILED {item['name']}: {exc}")


if __name__ == "__main__":
    main()
