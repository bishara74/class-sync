#!/bin/bash
# =============================================================================
# ClassSync — One-Time EC2 Instance Setup Script
# Target: Ubuntu 22.04 LTS (t2.micro or similar)
#
# This script installs all dependencies needed to run ClassSync:
#   - Java 17 (OpenJDK)
#   - PostgreSQL 15
#   - Nginx (reverse proxy)
#   - Node.js 20 (for Angular frontend build)
#   - Git
#
# Usage:
#   chmod +x setup.sh
#   sudo ./setup.sh
#
# The script is idempotent — safe to run multiple times.
# =============================================================================

set -euo pipefail

# Must run as root
if [ "$EUID" -ne 0 ]; then
    echo "ERROR: Please run as root (sudo ./setup.sh)"
    exit 1
fi

APP_DIR="/opt/classsync"
DB_NAME="classsync"
DB_USER="classsync"
DB_PASS="changeme"  # CHANGE THIS after setup

echo "============================================="
echo "  ClassSync — EC2 Instance Setup"
echo "============================================="
echo ""

# 1. Update system packages

echo "[1/9] Updating system packages..."
apt-get update -y
apt-get upgrade -y


# 2. Install Java 17
# 
echo "[2/9] Installing Java 17..."
if java -version 2>&1 | grep -q "17"; then
    echo "  Java 17 already installed, skipping."
else
    apt-get install -y openjdk-17-jdk
fi
java -version


# 3. Install PostgreSQL 15
echo "[3/9] Installing PostgreSQL 15..."
if command -v psql &> /dev/null; then
    echo "  PostgreSQL already installed, skipping package install."
else
    # Add PostgreSQL APT repository for version 15
    apt-get install -y wget gnupg2
    echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
        > /etc/apt/sources.list.d/pgdg.list
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
    apt-get update -y
    apt-get install -y postgresql-15
fi

# Ensure PostgreSQL is running
systemctl enable postgresql
systemctl start postgresql


# 4. Create database and user
echo "[4/9] Configuring PostgreSQL database..."
# Create user if it doesn't exist
if sudo -u postgres psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='${DB_USER}'" | grep -q 1; then
    echo "  Database user '${DB_USER}' already exists, skipping."
else
    sudo -u postgres psql -c "CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASS}';"
    echo "  Created database user '${DB_USER}'."
fi

# Create database if it doesn't exist
if sudo -u postgres psql -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" | grep -q 1; then
    echo "  Database '${DB_NAME}' already exists, skipping."
else
    sudo -u postgres psql -c "CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};"
    echo "  Created database '${DB_NAME}'."
fi

# Grant privileges
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};"


# 5. Install Nginx
echo "[5/9] Installing Nginx..."
if command -v nginx &> /dev/null; then
    echo "  Nginx already installed, skipping."
else
    apt-get install -y nginx
fi
systemctl enable nginx


# 6. Install Node.js 20 via NodeSource
echo "[6/9] Installing Node.js 20..."
if command -v node &> /dev/null && node -v | grep -q "v20"; then
    echo "  Node.js 20 already installed, skipping."
else
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
    apt-get install -y nodejs
fi
node -v
npm -v


# 7. Install Git

echo "[7/9] Installing Git..."
if command -v git &> /dev/null; then
    echo "  Git already installed, skipping."
else
    apt-get install -y git
fi


# 8. Create application directory
echo "[8/9] Creating application directory at ${APP_DIR}..."
if [ -d "${APP_DIR}" ]; then
    echo "  Directory ${APP_DIR} already exists, skipping."
else
    mkdir -p "${APP_DIR}"
    chown ubuntu:ubuntu "${APP_DIR}"
    echo "  Created ${APP_DIR} (owned by ubuntu user)."
fi


# 9. Clone the repository
echo "[9/9] Cloning repository..."
if [ -d "${APP_DIR}/.git" ]; then
    echo "  Repository already cloned at ${APP_DIR}, skipping."
    echo "  To update, run: cd ${APP_DIR} && git pull"
else
    echo "  NOTE: Clone your repository into ${APP_DIR}:"
    echo "    git clone <your-repo-url> ${APP_DIR}"
    echo "  Or copy the project files manually."
fi

echo ""
echo 
echo "  Setup Complete!"
echo 
echo ""
echo "Next steps:"
echo "  1. Clone your repo into ${APP_DIR} (if not already done)"
echo "  2. Change the PostgreSQL password:"
echo "     sudo -u postgres psql -c \"ALTER USER ${DB_USER} PASSWORD 'your-secure-password';\""
echo "  3. Copy Nginx config:"
echo "     sudo cp ${APP_DIR}/deployment/ec2/nginx.conf /etc/nginx/sites-available/classsync"
echo "     sudo ln -sf /etc/nginx/sites-available/classsync /etc/nginx/sites-enabled/"
echo "     sudo rm -f /etc/nginx/sites-enabled/default"
echo "     sudo nginx -t && sudo systemctl restart nginx"
echo "  4. Copy systemd service:"
echo "     sudo cp ${APP_DIR}/deployment/ec2/classsync.service /etc/systemd/system/"
echo "     sudo systemctl daemon-reload"
echo "     sudo systemctl enable classsync"
echo "  5. Update passwords/secrets in /etc/systemd/system/classsync.service"
echo "  6. Run the deploy script:"
echo "     cd ${APP_DIR} && ./deployment/ec2/deploy.sh"
echo ""
