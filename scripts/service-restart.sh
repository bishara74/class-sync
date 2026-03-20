#!/bin/bash
# =============================================================================
# ClassSync — Service Auto-Restart Script
#
# Checks if the ClassSync API is healthy and restarts the systemd service
# if it's down. Retries up to MAX_RETRIES times before reporting critical failure.
#
# Environment variables:
#   API_URL      — Backend health endpoint (default: http://localhost:8081)
#   MAX_RETRIES  — Maximum restart attempts (default: 3)
#   RETRY_DELAY  — Seconds between retries (default: 10)
#
# Usage:
#   ./scripts/service-restart.sh
#   API_URL=http://localhost:8081 MAX_RETRIES=5 ./scripts/service-restart.sh
#
# Cron example (check every 5 minutes):
#   */5 * * * * /opt/classsync/scripts/service-restart.sh >> /var/log/classsync-watchdog.log 2>&1
# =============================================================================

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
API_URL="${API_URL:-http://localhost:8081}"
MAX_RETRIES="${MAX_RETRIES:-3}"
RETRY_DELAY="${RETRY_DELAY:-10}"

# ---------------------------------------------------------------------------
# Colors and logging
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log()   { echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] $1"; }
ok()    { log "${GREEN}[OK]${NC} $1"; }
fail()  { log "${RED}[CRITICAL]${NC} $1"; }
warn()  { log "${YELLOW}[WARN]${NC} $1"; }

# ---------------------------------------------------------------------------
# Health check function
# Returns 0 if the API responds, 1 otherwise
# ---------------------------------------------------------------------------
check_health() {
    local http_code
    http_code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "${API_URL}/api/auth/login" 2>/dev/null || echo "000")

    if [ "${http_code}" != "000" ]; then
        return 0
    else
        return 1
    fi
}

# ---------------------------------------------------------------------------
# Initial health check
# ---------------------------------------------------------------------------
log "Checking ClassSync API at ${API_URL} ..."

if check_health; then
    ok "ClassSync API is healthy — no action needed"
    exit 0
fi

# ---------------------------------------------------------------------------
# API is down — attempt restart with retries
# ---------------------------------------------------------------------------
warn "ClassSync API is not responding. Starting restart procedure..."

for attempt in $(seq 1 "${MAX_RETRIES}"); do
    log "Attempt ${attempt}/${MAX_RETRIES}: Restarting classsync service..."

    # Restart the systemd service
    sudo systemctl restart classsync

    # Wait for the service to come up
    log "Waiting ${RETRY_DELAY}s for service to start..."
    sleep "${RETRY_DELAY}"

    # Check health again
    if check_health; then
        ok "ClassSync API is back online after restart (attempt ${attempt})"
        exit 0
    fi

    warn "Service still not responding after attempt ${attempt}"
done

# ---------------------------------------------------------------------------
# All retries exhausted
# ---------------------------------------------------------------------------
echo ""
fail "ClassSync API failed to recover after ${MAX_RETRIES} restart attempts!"
fail "Manual intervention required."
echo ""
log "Diagnostics:"
log "  Service status: sudo systemctl status classsync"
log "  Service logs:   sudo journalctl -u classsync -n 50 --no-pager"
log "  Check port:     sudo ss -tlnp | grep 8081"
exit 1
