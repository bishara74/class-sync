# Changelog

All notable changes to the ClassSync project will be documented in this file.

## [Unreleased]

### Added
- **Jenkins CI/CD Pipeline** (`Jenkinsfile`) with 4 stages:
  - **Checkout** — clones the repo and prints branch/build info
  - **Build** — compiles the Spring Boot backend JAR via Maven in Docker
  - **Test** — runs unit tests against a real PostgreSQL 15 container
  - **Build Docker Image** — builds and tags the backend Docker image
- Post-build cleanup: stops test database container and prunes dangling Docker images
- `.gitignore` entries for `angular-frontend/node_modules/` and `angular-frontend/dist/`
- **Ansible Playbooks** (`ansible/`) for automated EC2 provisioning and deployment:
  - `setup.yml` — provisions a fresh Ubuntu EC2 instance (Java, PostgreSQL, Nginx, Node.js)
  - `deploy.yml` — deploys the latest code (git pull, Maven build, Angular build, service restart)
  - 6 roles: `common`, `java`, `postgresql`, `nginx`, `nodejs`, `app`
  - Jinja2 templates for Nginx config and systemd service
  - Configurable variables in `group_vars/all.yml`

### Fixed
- `.gitignore` was missing entries for the `angular-frontend/` directory (only `frontend/` was covered)
