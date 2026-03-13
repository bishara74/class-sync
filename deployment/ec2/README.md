# ClassSync — EC2 Deployment Guide

Deploy ClassSync on an AWS EC2 instance with Nginx as a reverse proxy and systemd for process management.

## Prerequisites

- **AWS account** with EC2 access
- **EC2 instance**: t2.micro (free tier eligible), Ubuntu 22.04 LTS AMI
- **Security group** with the following inbound rules:
  - SSH (port 22) — restricted to your IP
  - HTTP (port 80) — open to all
  - HTTPS (port 443) — open to all (for future SSL)
- **SSH key pair** for instance access

## Architecture

```
Internet → Nginx (port 80)
              ├── /api/*  → Spring Boot (port 8081) → PostgreSQL (port 5432)
              └── /*      → Angular static files (/opt/classsync/angular-frontend/dist/)
```

- **Nginx** serves the Angular frontend as static files and reverse-proxies API requests to the Spring Boot backend
- **Spring Boot** runs as a systemd service on port 8081
- **PostgreSQL** runs locally on the default port 5432

## Step-by-Step Deployment

### 1. Launch an EC2 Instance

1. Go to **AWS Console → EC2 → Launch Instance**
2. Select **Ubuntu Server 22.04 LTS** AMI
3. Choose **t2.micro** instance type (free tier eligible)
4. Configure a security group with SSH (22), HTTP (80), and HTTPS (443)
5. Create or select an SSH key pair
6. Launch the instance and note the **public IP address**

### 2. Connect to the Instance

```bash
ssh -i your-key.pem ubuntu@<ec2-public-ip>
```

### 3. Transfer and Run the Setup Script

Option A — Clone the repo first, then run setup:
```bash
sudo apt-get update && sudo apt-get install -y git
sudo mkdir -p /opt/classsync && sudo chown ubuntu:ubuntu /opt/classsync
git clone <your-repo-url> /opt/classsync
sudo /opt/classsync/deployment/ec2/setup.sh
```

Option B — Copy the script manually:
```bash
scp -i your-key.pem deployment/ec2/setup.sh ubuntu@<ec2-public-ip>:~/
ssh -i your-key.pem ubuntu@<ec2-public-ip> "sudo chmod +x ~/setup.sh && sudo ~/setup.sh"
```

### 4. Configure the Application

Change the default PostgreSQL password:
```bash
sudo -u postgres psql -c "ALTER USER classsync PASSWORD 'your-secure-password';"
```

### 5. Set Up Nginx

```bash
sudo cp /opt/classsync/deployment/ec2/nginx.conf /etc/nginx/sites-available/classsync
sudo ln -sf /etc/nginx/sites-available/classsync /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl restart nginx
```

### 6. Set Up the Systemd Service

```bash
sudo cp /opt/classsync/deployment/ec2/classsync.service /etc/systemd/system/
```

Edit the service file to set production values:
```bash
sudo nano /etc/systemd/system/classsync.service
```

Update these environment variables:
- `SPRING_DATASOURCE_PASSWORD` — your PostgreSQL password from step 4
- `JWT_SECRET` — a strong random secret (e.g., `openssl rand -base64 32`)

Then enable and start the service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable classsync
sudo systemctl start classsync
```

### 7. Run the First Deployment

```bash
cd /opt/classsync
chmod +x deployment/ec2/deploy.sh
./deployment/ec2/deploy.sh
```

### 8. Verify the Deployment

```bash
# Check the service is running
sudo systemctl status classsync

# Test the API
curl http://<ec2-public-ip>/api/auth/login

# Open in browser
# http://<ec2-public-ip>
```

## Useful Commands

| Command | Description |
|---------|-------------|
| `sudo journalctl -u classsync -f` | View application logs (live) |
| `sudo systemctl restart classsync` | Restart the backend |
| `sudo systemctl status classsync` | Check backend status |
| `sudo systemctl stop classsync` | Stop the backend |
| `sudo tail -f /var/log/nginx/classsync_access.log` | Nginx access logs |
| `sudo tail -f /var/log/nginx/classsync_error.log` | Nginx error logs |
| `sudo nginx -t` | Test Nginx configuration |
| `sudo systemctl restart nginx` | Restart Nginx |

## Updating the Application

```bash
cd /opt/classsync
./deployment/ec2/deploy.sh
```

This pulls the latest code, rebuilds both frontend and backend, and restarts the service.

## Security Hardening (Post-Deployment)

1. **Change default passwords** — update PostgreSQL password and JWT secret (see steps 4 and 6)
2. **Set up SSL with Let's Encrypt:**
   ```bash
   sudo apt install certbot python3-certbot-nginx
   sudo certbot --nginx -d yourdomain.com
   ```
3. **Restrict SSH access** — limit the security group SSH rule to your IP only
4. **Enable UFW firewall:**
   ```bash
   sudo ufw allow OpenSSH
   sudo ufw allow 'Nginx Full'
   sudo ufw enable
   ```
5. **Automated PostgreSQL backups:**
   ```bash
   # Add to crontab (daily backup at 2 AM)
   sudo crontab -e
   0 2 * * * pg_dump -U classsync classsync > /opt/classsync/backups/db-$(date +\%Y\%m\%d).sql
   ```
6. **Keep the system updated:**
   ```bash
   sudo apt-get update && sudo apt-get upgrade -y
   ```

## Troubleshooting

- **Backend won't start** — check logs: `sudo journalctl -u classsync -n 50`
- **502 Bad Gateway** — backend isn't running; check `sudo systemctl status classsync`
- **Frontend shows blank page** — verify the Angular dist path in nginx.conf matches the actual build output
- **Database connection refused** — ensure PostgreSQL is running: `sudo systemctl status postgresql`
- **Port conflict** — verify port 8081 is free: `sudo lsof -i :8081`
