#!/bin/bash
# =============================================================================
# ClassSync — Health Check Script
#
# Checks if the ClassSync API and frontend are alive and responding.
#
# Environment variables:
#   API_URL       — Backend URL (default: http://localhost:8081)
#   FRONTEND_URL  — Frontend URL (default: http://localhost:4200)
#   TIMEOUT       — Curl timeout in seconds (default: 10)
#
# Usage:
#   ./scripts/healthcheck.sh
#   API_URL=http://13.60.9.90:8081 FRONTEND_URL=http://13.60.9.90 ./scripts/healthcheck.sh
# =============================================================================

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
API_URL="${API_URL:-http://localhost:8081}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:4200}"
TIMEOUT="${TIMEOUT:-10}"

# ---------------------------------------------------------------------------
# Colors and logging
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log()   { echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] $1"; }
ok()    { log "${GREEN}[PASS]${NC} $1"; }
fail()  { log "${RED}[FAIL]${NC} $1"; }
warn()  { log "${YELLOW}[WARN]${NC} $1"; }

# Track overall result
PASSED=0
FAILED=0

# ---------------------------------------------------------------------------
# Check 1: Backend API
# ---------------------------------------------------------------------------
log "Checking backend API at ${API_URL}/api/auth/login ..."

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time "${TIMEOUT}" "${API_URL}/api/auth/login" 2>/dev/null || echo "000")

if [ "${HTTP_CODE}" != "000" ]; then
    ok "Backend API responded with HTTP ${HTTP_CODE}"
    PASSED=$((PASSED + 1))
else
    fail "Backend API is unreachable (no response within ${TIMEOUT}s)"
    FAILED=$((FAILED + 1))
fi

# ---------------------------------------------------------------------------
# Check 2: Frontend
# ---------------------------------------------------------------------------
log "Checking frontend at ${FRONTEND_URL} ..."

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time "${TIMEOUT}" "${FRONTEND_URL}" 2>/dev/null || echo "000")

if [ "${HTTP_CODE}" = "200" ]; then
    ok "Frontend responded with HTTP 200"
    PASSED=$((PASSED + 1))
else
    fail "Frontend check failed (HTTP ${HTTP_CODE})"
    FAILED=$((FAILED + 1))
fi

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
log "Health check complete: ${GREEN}${PASSED} passed${NC}, ${RED}${FAILED} failed${NC}"

if [ "${FAILED}" -gt 0 ]; then
    fail "One or more health checks failed"
    exit 1
fi

ok "All health checks passed"
exit 0
