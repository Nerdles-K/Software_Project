#!/bin/bash

echo "=== VisiTalk — Starting All Services ==="

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

# 1. Ensure PostgreSQL is running
echo "[1/3] PostgreSQL..."
brew services start postgresql@17 2>/dev/null
sleep 1

# Verify DB exists
psql -U visitalk -d visitalk -c "SELECT 1" > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "  Creating database..."
  psql postgres -c "CREATE USER visitalk WITH PASSWORD 'visitalk123';" 2>/dev/null
  psql postgres -c "CREATE DATABASE visitalk OWNER visitalk;" 2>/dev/null
  psql -U visitalk -d visitalk -f "$PROJECT_ROOT/backend/src/main/resources/schema.sql" 2>/dev/null
fi
echo "  PostgreSQL ✓"

# 2. Kill stale processes and start fresh
echo "[2/3] Backend (Spring Boot on :8080)..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:3000 | xargs kill -9 2>/dev/null
cd "$PROJECT_ROOT/backend"
./gradlew bootRun > /dev/null 2>&1 &
BACKEND_PID=$!
echo "  Backend starting (PID $BACKEND_PID)..."

# 3. Start frontend
echo "[3/3] Frontend (Vite on :3000)..."
cd "$PROJECT_ROOT/frontend"
npm install --silent 2>/dev/null
npm run dev &
FRONTEND_PID=$!
echo "  Frontend starting (PID $FRONTEND_PID)..."

# Wait for backend to be ready
echo ""
echo "Waiting for backend..."
for i in $(seq 1 30); do
  curl -s http://localhost:8080/api/health > /dev/null 2>&1 && break
  sleep 2
done

echo ""
echo "==================================="
echo "  VisiTalk is starting up!"
echo "  Frontend: http://localhost:3000"
echo "  Backend:  http://localhost:8080"
echo "  Health:   http://localhost:8080/api/health"
echo ""
echo "  Test accounts:"
echo "    parent@test.com / password123"
echo "    child@test.com  / password123"
echo "==================================="
echo ""
echo "Press Ctrl+C to stop all services"

# Open browser
sleep 2
open http://localhost:3000

# Trap Ctrl+C to kill both
trap "echo 'Shutting down...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" INT TERM

wait
