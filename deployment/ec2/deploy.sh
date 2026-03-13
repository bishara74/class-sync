#!/bin/bash
# ClassSync — Deployment Script
#
# Builds and deploys the latest version of ClassSync on an EC2 instance.
# Safe to run multiple times (idempotent).
#
# Usage:
#   cd /opt/classsync
#   ./deployment/ec2/deploy.sh
#
# Prerequisites:
#   - setup.sh has been run
#   - Repository is cloned at /opt/classsync
#   - Nginx and systemd service are configured
# 

set -euo pipefail

APP_DIR="/opt/classsync"
BACKEND_DIR="${APP_DIR}/backend"
FRONTEND_DIR="${APP_DIR}/angular-frontend"
NGINX_SERVE_DIR="${FRONTEND_DIR}/dist/angular-frontend/browser"

echo 
echo "  ClassSync — Deploying Latest Version"
echo 
echo ""


# 1. Pull latest code from git
echo "[1/5] Pulling latest code..."
cd "${APP_DIR}"
git pull

# 2. Build Spring Boot backend
echo "[2/5] Building Spring Boot backend..."
cd "${BACKEND_DIR}"

# Use Maven wrapper if available, fall back to system Maven
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    ./mvnw clean package -DskipTests
else
    mvn clean package -DskipTests
fi

echo "  Backend build complete."

# 3. Build Angular frontend
echo "[3/5] Building Angular frontend..."
cd "${FRONTEND_DIR}"

# Install dependencies
npm install

# Build for production
npx ng build --configuration=production

echo "  Frontend build complete."


# 4. Verify Angular dist exists for Nginx

echo "[4/5] Verifying frontend build output..."
if [ -d "${NGINX_SERVE_DIR}" ]; then
    echo "  Frontend files ready at ${NGINX_SERVE_DIR}"
else
    echo "  WARNING: Expected directory not found: ${NGINX_SERVE_DIR}"
    echo "  Check Angular build output and update nginx.conf root path if needed."
fi

# Reload Nginx to pick up any new static files
sudo nginx -t && sudo systemctl reload nginx
echo "  Nginx reloaded."


# 5. Restart the Spring Boot service
echo "[5/5] Restarting ClassSync service..."
sudo systemctl daemon-reload
sudo systemctl restart classsync

# Wait a moment for the service to start
sleep 3

echo "  Deployment Complete!"
echo "============================================="
echo ""
echo "Service status:"
sudo systemctl status classsync --no-pager -l
echo ""
echo "Useful commands:"
echo "  View logs:     sudo journalctl -u classsync -f"
echo "  Restart app:   sudo systemctl restart classsync"
echo "  Check status:  sudo systemctl status classsync"
echo "  Nginx logs:    sudo tail -f /var/log/nginx/classsync_access.log"
echo ""
