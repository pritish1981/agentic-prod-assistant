#!/bin/bash

echo "🔥 Running Smoke Test for Enterprise AI Agent..."

############################################
# Backend Health
############################################

echo "Checking backend health..."

STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)

if [ "$STATUS" != "200" ]; then
  echo "❌ Backend is NOT healthy!"
  exit 1
fi

echo "✅ Backend healthy."

############################################
# Elastic Health
############################################

echo "Checking Elasticsearch..."

curl -s http://localhost:9200/_cluster/health?pretty

############################################
# Chat Test
############################################

echo ""
echo "Sending sample query to agent..."

curl -X POST http://localhost:8080/api/chat \
-H "Content-Type: application/json" \
-d '{
  "message": "How do I resolve database connection exhaustion?",
  "userId": "smoke-test"
}'

echo ""
echo ""
echo "🎉 Smoke test completed!"
