#!/bin/bash

echo "🚀 Starting Enterprise Production AI Agent Locally..."

# Stop old containers
echo "🧹 Cleaning old containers..."
docker compose down

# Start infrastructure
echo "📦 Starting Elastic + Redis..."
docker compose up -d elasticsearch redis

# Wait for Elastic
echo "⏳ Waiting for Elasticsearch to become healthy..."

until curl -s http://localhost:9200 >/dev/null; do
  sleep 5
  echo "Waiting for Elastic..."
done

echo "✅ Elasticsearch is up!"

############################################
# Start Backend
############################################

echo "☕ Starting Spring Boot Agent..."

cd backend/agent-service || exit

./mvnw clean install -DskipTests

./mvnw spring-boot:run &

BACKEND_PID=$!

echo "Backend running with PID $BACKEND_PID"

############################################
# Start Frontend
############################################

echo "💻 Starting React Widget..."

cd ../../frontend/react-chat-widget || exit

npm install
npm start &

FRONTEND_PID=$!

echo "Frontend running with PID $FRONTEND_PID"

echo ""
echo "🎉 SYSTEM READY!"
echo "Frontend: http://localhost:3000"
echo "Backend:  http://localhost:8080"
echo ""
echo "Press CTRL+C to stop everything."

wait
