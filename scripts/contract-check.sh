#!/usr/bin/env sh
# Поднимает сервис текущей ветки, гоняет неизменный набор контрактных тестов и останавливает сервис.
# Использование: scripts/contract-check.sh
set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
cd "$PROJECT_DIR"
PORT=${PORT:-8080}
LOG=build/contract-app.log

stop_app() {
    pids=$(lsof -ti "tcp:$PORT" -sTCP:LISTEN 2>/dev/null || true)
    [ -n "$pids" ] && kill $pids 2>/dev/null || true
    [ -n "${BOOT_PID:-}" ] && kill "$BOOT_PID" 2>/dev/null || true
}
trap stop_app EXIT

docker compose up -d --wait
mkdir -p build
echo "Starting service on :$PORT (log: $LOG)"
PORT=$PORT ./gw bootRun --console=plain > "$LOG" 2>&1 &
BOOT_PID=$!

for _ in $(seq 1 90); do
    if curl -fs "http://localhost:$PORT/actuator/health" > /dev/null 2>&1; then
        break
    fi
    if ! kill -0 "$BOOT_PID" 2>/dev/null; then
        echo "Service failed to start, see $LOG"
        tail -40 "$LOG"
        exit 1
    fi
    sleep 2
done

curl -fs "http://localhost:$PORT/actuator/health" > /dev/null || { echo "Service is not healthy"; tail -40 "$LOG"; exit 1; }

TARGET_BASE_URL="http://localhost:$PORT" ./gw contractTest --console=plain
