#!/bin/sh
set -e

curl -s -X PUT "http://elasticsearch:9200/rag_idx" \
  -H 'Content-Type: application/json' \
  -d '{
    "mappings": {
      "properties": {
        "content": {
          "type": "text",
          "fields": {
            "keyword": { "type": "keyword", "ignore_above": 256 }
          }
        },
        "embedding": {
          "type": "dense_vector",
          "dims": 1024,
          "index": true,
          "similarity": "cosine",
          "index_options": {
            "type": "int8_hnsw",
            "m": 16,
            "ef_construction": 100
          }
        },
        "id": {
          "type": "text",
          "fields": {
            "keyword": { "type": "keyword", "ignore_above": 256 }
          }
        },
        "metadata": {
          "properties": {
            "chunk_index": { "type": "long" },
            "fileId": {
              "type": "text",
              "fields": {
                "keyword": { "type": "keyword", "ignore_above": 256 }
              }
            },
            "lastReadTime": { "type": "long" },
            "parent_document_id": {
              "type": "text",
              "fields": {
                "keyword": { "type": "keyword", "ignore_above": 256 }
              }
            },
            "project": { "type": "long" },
            "source": {
              "type": "text",
              "fields": {
                "keyword": { "type": "keyword", "ignore_above": 256 }
              }
            },
            "total_chunks": { "type": "long" }
          }
        }
      }
    }
  }'

echo "Elasticsearch index 'rag_idx' created (or already existed)."

# Bulk-load real exported data, if present (see data.ndjson, produced via
# `elasticdump --input=http://localhost:9200/rag_idx --output=es-init/data.ndjson --type=data`)
#
# elasticdump's export format wraps each doc as {"_id":..., "_source": {...}},
# one per line -- that's NOT the action/source pair format the _bulk API
# needs, so we convert it with jq first.
if [ -f /scripts/data.ndjson ]; then
  echo "Converting data.ndjson to bulk format..."
  jq -c '{"index": {"_index": "rag_idx", "_id": ._id}}, ._source' \
    /scripts/data.ndjson > /tmp/data-bulk.ndjson

  echo "Loading into rag_idx..."
  curl -s -X POST "http://elasticsearch:9200/_bulk" \
    -H 'Content-Type: application/x-ndjson' \
    --data-binary @/tmp/data-bulk.ndjson
  echo "Bulk load complete."
else
  echo "No data.ndjson found, skipping bulk load."
fi