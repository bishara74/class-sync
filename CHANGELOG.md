# Changelog

All notable changes to the ClassSync project will be documented in this file.

## [Unreleased]

### Added
- **Jenkins CI/CD Pipeline** (`Jenkinsfile`) with 5 stages:
  - **Checkout** — clones the repo and prints branch/build info
  - **Build** — compiles the Spring Boot backend JAR via Maven in Docker
  - **Test** — runs unit tests against a real PostgreSQL 15 container
  - **Build Docker Image** — builds and tags the backend Docker image
  - **E2E Tests** — starts full stack via Docker Compose, runs 12 Selenium E2E tests with headless Chrome in a Maven container
- Post-build cleanup: stops test database container, tears down Docker Compose stack, and prunes dangling Docker images
- `.gitignore` entries for `angular-frontend/node_modules/` and `angular-frontend/dist/`
- **Ansible Playbooks** (`ansible/`) for automated EC2 provisioning and deployment:
  - `setup.yml` — provisions a fresh Ubuntu EC2 instance (Java, PostgreSQL, Nginx, Node.js)
  - `deploy.yml` — deploys the latest code (git pull, Maven build, Angular build, service restart)
  - 6 roles: `common`, `java`, `postgresql`, `nginx`, `nodejs`, `app`
  - Jinja2 templates for Nginx config and systemd service
  - Configurable variables in `group_vars/all.yml`
- **Kubernetes Manifests** (`k8s/`) for container orchestration:
  - Namespace, Deployment (2 replicas), StatefulSet, Services, ConfigMaps, Secrets
  - PostgreSQL StatefulSet with 1Gi persistent storage
  - Backend with health probes, resource limits, and env from ConfigMap/Secret
  - Designed for Minikube local testing
- **Operational Shell Scripts** (`scripts/`):
  - `healthcheck.sh` — checks API and frontend liveness with colored output
  - `log-monitor.sh` — analyzes journalctl logs for errors, warnings, and exceptions
  - `service-restart.sh` — auto-restarts the systemd service with retries (cron-ready)
- **Makefile** with targets: `up`, `down`, `build`, `test`, `logs`, `healthcheck`, `monitor`, `restart`, `help`
- **Comprehensive README** covering all DevOps tooling, project structure, architecture, and usage

### Fixed
- `.gitignore` was missing entries for the `angular-frontend/` directory (only `frontend/` was covered)
