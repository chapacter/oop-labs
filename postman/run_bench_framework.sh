#!/usr/bin/env bash
# bench-scripts/run_bench_framework.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
POSTMAN="$ROOT/postman"
OUT_DIR="$POSTMAN"
COLLECTION="$POSTMAN/collection-lab5.json"
ENV="$POSTMAN/env-framework.postman_env.json"
ITERATIONS=${ITERATIONS:-20}
REPORT_JSON="$OUT_DIR/details_framework.json"
AGG_CSV="$OUT_DIR/aggregates_framework.csv"
DETAILS_CSV="$OUT_DIR/details_framework.csv"

echo "Running newman for framework with auth tests..."
newman run "$COLLECTION" -e "$ENV" --iteration-count $ITERATIONS \
  --reporters cli,json --reporter-json-export "$REPORT_JSON"

echo "Converting JSON -> CSV & aggregates..."
node "$ROOT/bench-scripts/newman-to-csv.js" --input "$REPORT_JSON" --out-details "$DETAILS_CSV" --out-agg "$AGG_CSV"

echo "Done. Files:"
ls -lh "$OUT_DIR" | sed -n '1,200p'
