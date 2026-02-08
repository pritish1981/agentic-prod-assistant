#!/bin/bash

echo "📚 Indexing documents into Elasticsearch..."

ELASTIC_URL="http://localhost:9200"
INDEX_NAME="prod-incidents"

############################################
# Delete Existing Index (Clean Reindex)
############################################

echo "Deleting existing index (if any)..."

curl -X DELETE "$ELASTIC_URL/$INDEX_NAME" \
-H "Content-Type: application/json"

echo ""
echo "✅ Existing index deleted (or did not exist)."

############################################
# Create Index Mapping
############################################

echo "Creating index..."

curl -X PUT "$ELASTIC_URL/$INDEX_NAME" \
-H "Content-Type: application/json" \
-d @infrastructure/elastic/index-mapping.json

echo ""
echo "✅ Index created."

############################################
# Bulk Index Incidents
############################################

echo "Bulk indexing incidents..."

python - "datasets/incidents.json" <<'PY' | while IFS= read -r doc; do
import json
import sys

path = sys.argv[1]
with open(path, encoding="utf-8") as handle:
    data = json.load(handle)

for item in data:
    print(json.dumps(item, separators=(",", ":")))
PY
  curl -X POST "$ELASTIC_URL/$INDEX_NAME/_doc" \
  -H "Content-Type: application/json" \
  -d "$doc"
done

echo ""
echo "✅ Incidents indexed."

############################################
# Optional: Runbooks
############################################

echo "Indexing runbooks..."

python - "datasets/runbooks.json" <<'PY' | while IFS= read -r doc; do
import json
import sys

path = sys.argv[1]
with open(path, encoding="utf-8") as handle:
    data = json.load(handle)

for item in data:
    print(json.dumps(item, separators=(",", ":")))
PY
  curl -X POST "$ELASTIC_URL/$INDEX_NAME/_doc" \
  -H "Content-Type: application/json" \
  -d "$doc"
done

echo ""
echo "✅ Runbooks indexed."

############################################
# Optional: FAQs
############################################

echo "Indexing FAQs..."

python - "datasets/prod-faqs.json" <<'PY' | while IFS= read -r doc; do
import json
import sys

path = sys.argv[1]
with open(path, encoding="utf-8") as handle:
    data = json.load(handle)

for item in data:
    print(json.dumps(item, separators=(",", ":")))
PY
  curl -X POST "$ELASTIC_URL/$INDEX_NAME/_doc" \
  -H "Content-Type: application/json" \
  -d "$doc"
done

echo ""
echo "🎉 ALL DOCUMENTS INDEXED SUCCESSFULLY!"
