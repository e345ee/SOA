import argparse
import json
from pathlib import Path

import yaml

root = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser()
parser.add_argument("--check", action="store_true")
args = parser.parse_args()
specs = {}
for name in ("ticket", "booking"):
    document = yaml.safe_load((root / "api" / (name + ".yaml")).read_text())
    specs[name] = json.loads(json.dumps(document, default=str))

generated = {
    root / "frontend/assets/contracts.json": json.dumps(specs, ensure_ascii=False, separators=(",", ":")),
}

outdated = []
for path, content in generated.items():
    if args.check:
        if not path.exists() or json.loads(path.read_text()) != json.loads(content):
            outdated.append(str(path.relative_to(root)))
    else:
        path.write_text(content + "\n")

if outdated:
    parser.exit(1, "Outdated contracts: " + ", ".join(outdated) + "\n")
print("Contract copies are synchronized." if args.check else "Contract copies updated from api/*.yaml.")
