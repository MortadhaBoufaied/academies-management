"""
adminplatform/merge.py â€“ CSV data-merge logic.

Strategy:
  1. Load global_data.csv  (set by super_admin â€“ baseline Q&A for all academies)
  2. Load academy/{slug}/data.csv  (set by academy_admin â€“ academy-specific Q&A)
  3. Merge: rows sharing the same Intent_ID â†’ academy row wins.
            rows with no Intent_ID or unique IDs â†’ all included.
  4. Write merged result to academies/{slug}/merged_data.csv
  5. Optionally copy to the Django chatbot's data directory.
"""
import csv
import io
import shutil
from pathlib import Path

DATA_ROOT = Path(__file__).resolve().parent.parent / "data"
GLOBAL_CSV = DATA_ROOT / "global" / "data.csv"
ACADEMIES_DIR = DATA_ROOT / "academies"

# Path to the Django chatbot's data folder (for file-system deployment)
_CHATBOT_DATA = (
    Path(__file__).resolve().parent.parent.parent
    / "chatbot" / "chatbot" / "apps" / "chat" / "training_models" / "data"
)


# â”€â”€ CSV parsing 

def _detect_delimiter(text: str) -> str:
    first_line = text.splitlines()[0] if text.strip() else ""
    return ";" if first_line.count(";") > first_line.count(",") else ","


def _read_rows(path: Path) -> list[dict]:
    if not path.exists() or not path.stat().st_size:
        return []
    text = path.read_text(encoding="utf-8-sig")
    if not text.strip():
        return []
    delimiter = _detect_delimiter(text)
    reader = csv.DictReader(io.StringIO(text), delimiter=delimiter)
    return [{k.strip(): (v or "").strip() for k, v in row.items()} for row in reader]


def _write_rows(path: Path, rows: list[dict]) -> None:
    if not rows:
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    headers = list(rows[0].keys())
    with open(path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=headers, delimiter=";", extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


# â”€â”€ Merge logic 

def _build_merged_rows(slug: str) -> list[dict]:
    global_rows = _read_rows(GLOBAL_CSV)
    academy_rows = _read_rows(ACADEMIES_DIR / slug / "data.csv")

    # Index global rows by Intent_ID (only those that have one)
    global_by_intent: dict[str, dict] = {
        row["Intent_ID"]: row
        for row in global_rows
        if row.get("Intent_ID")
    }

    # Academy rows override matching global intents
    overridden_intents: set[str] = set()
    merged: list[dict] = []

    for row in academy_rows:
        iid = row.get("Intent_ID", "").strip()
        if iid and iid in global_by_intent:
            overridden_intents.add(iid)
        merged.append(row)

    # Append global rows not overridden
    for row in global_rows:
        iid = row.get("Intent_ID", "").strip()
        if not iid or iid not in overridden_intents:
            merged.append(row)

    return merged


def merge_and_write(slug: str) -> Path:
    """Produce merged_data.csv for an academy and return its path."""
    rows = _build_merged_rows(slug)
    out = ACADEMIES_DIR / slug / "merged_data.csv"
    _write_rows(out, rows)

    # Best-effort copy to Django chatbot data dir
    if _CHATBOT_DATA.exists():
        try:
            dest = _CHATBOT_DATA / f"{slug}_data.csv"
            shutil.copy2(str(out), str(dest))
        except OSError:
            pass

    return out


# â”€â”€ Statistics 

def get_global_row_count() -> int:
    return len(_read_rows(GLOBAL_CSV))


def get_academy_row_count(slug: str) -> int:
    return len(_read_rows(ACADEMIES_DIR / slug / "data.csv"))


def get_merged_row_count(slug: str) -> int:
    return len(_build_merged_rows(slug))


# â”€â”€ Save helpers 

def save_global_csv(contents: bytes) -> int:
    GLOBAL_CSV.parent.mkdir(parents=True, exist_ok=True)
    GLOBAL_CSV.write_bytes(contents)
    return get_global_row_count()


def save_academy_csv(slug: str, contents: bytes) -> int:
    dest = ACADEMIES_DIR / slug / "data.csv"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_bytes(contents)
    return get_academy_row_count(slug)


def get_global_csv_exists() -> bool:
    return GLOBAL_CSV.exists() and GLOBAL_CSV.stat().st_size > 0


def get_academy_csv_exists(slug: str) -> bool:
    p = ACADEMIES_DIR / slug / "data.csv"
    return p.exists() and p.stat().st_size > 0


{'='*80}
