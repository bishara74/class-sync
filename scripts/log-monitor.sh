#!/bin/bash
# =============================================================================
# ClassSync — Log Monitor Script
#
# Monitors ClassSync service logs and reports errors, warnings, and exceptions.
#
# Environment variables:
#   LOG_LINES — Number of recent log lines to analyze (default: 100)
#
# Usage:
#   ./scripts/log-monitor.sh
#   LOG_LINES=500 ./scripts/log-monitor.sh
# =============================================================================

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
LOG_LINES="${LOG_LINES:-100}"

# ---------------------------------------------------------------------------
# Colors and logging
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log()   { echo -e "[$(date '+%Y-%m-%d %H:%M:%S')] $1"; }
ok()    { log "${GREEN}[OK]${NC} $1"; }
fail()  { log "${RED}[ERROR]${NC} $1"; }
warn()  { log "${YELLOW}[WARN]${NC} $1"; }

# ---------------------------------------------------------------------------
# Fetch logs from journalctl
# ---------------------------------------------------------------------------
log "Analyzing last ${LOG_LINES} lines from classsync service logs..."

LOGS=$(sudo journalctl -u classsync --no-pager -n "${LOG_LINES}" 2>/dev/null || echo "")

if [ -z "${LOGS}" ]; then
    warn "No logs found for classsync service. Is the service running?"
    exit 0
fi

# ---------------------------------------------------------------------------
# Count occurrences of errors, warnings, and exceptions
# ---------------------------------------------------------------------------
TOTAL_LINES=$(echo "${LOGS}" | wc -l)
ERROR_COUNT=$(echo "${LOGS}" | grep -ci "ERROR" || true)
WARN_COUNT=$(echo "${LOGS}" | grep -ci "WARN" || true)
EXCEPTION_COUNT=$(echo "${LOGS}" | grep -ci "Exception" || true)

# ---------------------------------------------------------------------------
# Print summary
# ---------------------------------------------------------------------------
echo ""
log "========== Log Analysis Summary =========="
log "Total lines analyzed:  ${TOTAL_LINES}"
log "Errors:                ${RED}${ERROR_COUNT}${NC}"
log "Warnings:              ${YELLOW}${WARN_COUNT}${NC}"
log "Exceptions:            ${RED}${EXCEPTION_COUNT}${NC}"
echo ""

# ---------------------------------------------------------------------------
# If errors found, print the last 5 error lines
# ---------------------------------------------------------------------------
if [ "${ERROR_COUNT}" -gt 0 ]; then
    fail "Errors detected in service logs!"
    echo ""
    log "Last 5 error lines:"
    echo "---"
    echo "${LOGS}" | grep -i "ERROR" | tail -5
    echo "---"
    echo ""
    log "Run ${YELLOW}sudo journalctl -u classsync -f${NC} for live logs"
    exit 1
fi

ok "No errors found in the last ${LOG_LINES} log lines"
exit 0
