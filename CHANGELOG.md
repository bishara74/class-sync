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

### Fixed
- `.gitignore` was missing entries for the `angular-frontend/` directory (only `frontend/` was covered)
