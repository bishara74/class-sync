# ClassSync — Attendance Management System

ClassSync is a modern, full-stack web application designed to streamline classroom attendance tracking. It replaces manual roll calls with a secure, time-sensitive digital check-in system. Teachers can generate temporary attendance codes, and students can check in securely using their institutional credentials.

**Live Demo:** [http://13.60.9.90](http://13.60.9.90)

## Features

* **Role-Based Dashboards** — distinct interfaces for Teachers and Students
* **Custom Authentication** — JWT tokens, BCrypt password hashing, Neptun Code verification for students
* **Dynamic Session Generation** — teachers create sessions with custom expiration timers
* **Cryptographic Check-Ins** — unique 6-digit attendance codes for student verification
* **Real-Time Validation** — code validity checks, expiration enforcement, duplicate prevention

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Frontend** | Angular 21, TypeScript, Tailwind CSS, HTTP Interceptor (JWT) |
| **Frontend (legacy)** | React 19, Vite, Axios (`frontend/` — unused) |
| **Backend** | Java 17, Spring Boot 3.5.11, Spring Data JPA, JWT (jjwt 0.12.6), BCrypt |
| **Database** | PostgreSQL 15 |
| **Containerization** | Docker, Docker Compose, Kubernetes (Minikube) |
| **CI/CD** | GitLab CI/CD, Jenkins |
| **Infrastructure** | Ansible, AWS EC2, Nginx, systemd |
| **Testing** | JUnit 5, Selenium WebDriver, headless Chrome |
| **Operations** | Shell scripts (Bash), Makefile |

## Architecture

```
Internet → Nginx (port 80)
             ├── /api/*  → Spring Boot backend (port 8081)
             └── /*      → Angular static files

┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│   Angular    │────▶│  Spring Boot  │────▶│  PostgreSQL   │
│  (port 4200) │     │  (port 8081)  │     │  (port 5432)  │
└─────────────┘     └──────────────┘     └──────────────┘
```

## Quick Start

```bash
docker-compose up
```

This starts PostgreSQL (port 5433), the Spring Boot backend (port 8081), and the Angular frontend (port 4200).

**Test users seeded automatically:**

| Role | Email | Password | Neptun Code |
|------|-------|----------|-------------|
| Teacher | `teacher@school.edu` | `pass123` | — |
| Student | `student@school.edu` | `pass123` | `ABC123` |

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Login with email + password (+ optional neptunCode) | No |
| POST | `/api/attendance/sessions` | Create attendance session | JWT |
| POST | `/api/attendance/check-in` | Student check-in with 6-digit code | JWT |

## Testing

### Unit Tests

```bash
cd backend && mvn test -Dtest="com.hodali.classsync.service.**"
```

### Selenium E2E Tests

12 tests across 4 test classes using headless Chrome + Selenium WebDriver:

| Test Class | Tests | Coverage |
|------------|-------|----------|
| **LoginE2ETest** | 5 | Login page load, teacher/student login, invalid login, empty fields |
| **TeacherFlowE2ETest** | 3 | Dashboard load, session creation, code format |
| **StudentFlowE2ETest** | 3 | Dashboard load, check-in, invalid code |
| **FullFlowE2ETest** | 1 | End-to-end: teacher creates session → student checks in → teacher verifies |

```bash
cd backend && mvn test -Dtest="com.hodali.classsync.e2e.**"
```

## CI/CD

### GitLab CI/CD (`.gitlab-ci.yml`)

Three jobs across build and test stages:

| Job | Stage | Description |
|-----|-------|-------------|
| `backend-build` | build | Compiles Spring Boot JAR with Maven |
| `backend-test` | test | Runs tests against a PostgreSQL 15 service container |
| `angular-build` | build | Builds the Angular frontend for production |

### Jenkins Pipeline (`Jenkinsfile`)

Four-stage declarative pipeline designed to run with Docker:

| Stage | Description |
|-------|-------------|
| **Checkout** | Clones the repo, prints branch name and build number |
| **Build** | Compiles the Spring Boot JAR using `maven:3.9-eclipse-temurin-17` in Docker |
| **Test** | Spins up a PostgreSQL 15 container, runs unit tests (Selenium excluded) |
| **Build Docker Image** | Builds `classsync-backend:{build_number}` and tags as `latest` |

Post-build: automatically cleans up test containers and prunes dangling Docker images.

## Ansible

Ansible playbooks in `ansible/` automate EC2 provisioning and deployment as an alternative to the manual shell scripts.

### Roles

| Role | Description |
|------|-------------|
| `common` | System update, Git installation |
| `java` | OpenJDK 17 |
| `postgresql` | PostgreSQL 15, database and user creation |
| `nginx` | Nginx with Jinja2-templated reverse proxy config |
| `nodejs` | Node.js 20 via NodeSource |
| `app` | Clone repo, Maven build, Angular build, systemd service deployment |

### Usage

**Provision a fresh EC2 instance:**
```bash
cd ansible
ansible-playbook -i inventory.ini playbooks/setup.yml
```

**Deploy the latest code:**
```bash
cd ansible
ansible-playbook -i inventory.ini playbooks/deploy.yml
```

Configuration: edit `group_vars/all.yml` to set database credentials, repo URL, ports, and other variables.

## Kubernetes

Kubernetes manifests in `k8s/` for deploying the backend and PostgreSQL, designed for local Minikube testing.

| Manifest | Description |
|----------|-------------|
| `namespace.yml` | Creates the `classsync` namespace |
| `backend/deployment.yml` | 2 replicas, resource limits (512Mi/500m CPU), health probes |
| `backend/service.yml` | ClusterIP service on port 8081 |
| `backend/configmap.yml` | Datasource URL, username, server port |
| `backend/secret.yml` | Database password, JWT secret (base64) |
| `database/statefulset.yml` | PostgreSQL 15 with 1Gi persistent storage |
| `database/service.yml` | ClusterIP service on port 5432 |
| `database/configmap.yml` | Database name, user |
| `database/secret.yml` | Database password (base64) |

### Deploy to Minikube

```bash
# Build and load the Docker image
docker build -t classsync-backend:latest ./backend
minikube image load classsync-backend:latest

# Apply manifests
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/database/
kubectl apply -f k8s/backend/

# Verify
kubectl get pods -n classsync

# Access the API
kubectl port-forward -n classsync svc/backend-service 8081:8081
```

See [`k8s/README.md`](k8s/README.md) for full details.

## Operational Scripts

Shell scripts in `scripts/` for monitoring and automation. All use colored output, timestamps, and configurable environment variables.

| Script | Purpose | Key Env Vars |
|--------|---------|-------------|
| `healthcheck.sh` | Check if API and frontend are alive | `API_URL`, `FRONTEND_URL`, `TIMEOUT` |
| `log-monitor.sh` | Analyze journalctl logs for errors/warnings | `LOG_LINES` |
| `service-restart.sh` | Auto-restart systemd service with retries | `API_URL`, `MAX_RETRIES`, `RETRY_DELAY` |

**Cron example** (auto-restart watchdog every 5 minutes):
```bash
*/5 * * * * /opt/classsync/scripts/service-restart.sh >> /var/log/classsync-watchdog.log 2>&1
```

## Makefile

```bash
make help          # Show all available targets
make up            # Start services with Docker Compose
make down          # Stop services and remove volumes
make build         # Build backend JAR (skip tests)
make test          # Run backend tests
make logs          # Follow backend container logs
make healthcheck   # Run health check script
make monitor       # Monitor service logs for errors
make restart       # Auto-restart if service is down
```

## AWS EC2 Deployment

Manual deployment scripts in `deployment/ec2/` for Ubuntu 22.04 instances:

| File | Description |
|------|-------------|
| `setup.sh` | One-time provisioning: installs Java 17, PostgreSQL 15, Nginx, Node.js 20, Git |
| `deploy.sh` | Pulls latest code, builds backend + frontend, restarts services |
| `nginx.conf` | Nginx reverse proxy configuration (API + SPA routing) |
| `classsync.service` | systemd service file for the Spring Boot backend |

```bash
# First-time setup
sudo ./deployment/ec2/setup.sh

# Deploy updates
./deployment/ec2/deploy.sh
```

See [`deployment/ec2/README.md`](deployment/ec2/README.md) for full instructions.

## Project Structure

```
class-sync/
├── angular-frontend/          # Angular 21 frontend (primary)
│   ├── src/
│   ├── Dockerfile
│   └── angular.json
├── backend/                   # Spring Boot 3.5.11 backend
│   ├── src/
│   │   ├── main/java/com/hodali/classsync/
│   │   │   ├── controller/    # REST controllers
│   │   │   ├── service/       # Business logic
│   │   │   ├── repository/    # JPA repositories
│   │   │   ├── model/         # Entity classes
│   │   │   ├── dto/           # Request/response DTOs
│   │   │   └── config/        # JWT, CORS, filters
│   │   └── test/java/com/hodali/classsync/
│   │       ├── service/       # Unit tests
│   │       └── e2e/           # Selenium E2E tests (12 tests)
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                  # React 19 frontend (legacy, unused)
├── ansible/                   # Ansible playbooks + roles
│   ├── playbooks/
│   │   ├── setup.yml          # Provision EC2 instance
│   │   └── deploy.yml         # Deploy latest code
│   ├── roles/                 # common, java, postgresql, nginx, nodejs, app
│   ├── group_vars/all.yml     # Configuration variables
│   └── inventory.ini          # EC2 host inventory
├── k8s/                       # Kubernetes manifests
│   ├── namespace.yml
│   ├── backend/               # Deployment, Service, ConfigMap, Secret
│   └── database/              # StatefulSet, Service, ConfigMap, Secret
├── deployment/ec2/            # EC2 deployment scripts
│   ├── setup.sh               # One-time instance provisioning
│   ├── deploy.sh              # Code deployment
│   ├── nginx.conf             # Nginx reverse proxy config
│   └── classsync.service      # systemd service file
├── scripts/                   # Operational scripts
│   ├── healthcheck.sh         # API + frontend liveness check
│   ├── log-monitor.sh         # Log analysis for errors
│   └── service-restart.sh     # Auto-restart with retries
├── docker-compose.yml         # Local development environment
├── Jenkinsfile                # Jenkins CI/CD pipeline
├── .gitlab-ci.yml             # GitLab CI/CD pipeline
├── Makefile                   # Common commands
├── CHANGELOG.md               # Project changelog
└── README.md
```

## Prerequisites

To run this project locally, you will need:
* Docker + Docker Compose (recommended)
* Node.js v18+ (for frontend development)
* Java Development Kit (JDK 17+)
* Maven
* PostgreSQL v15+ (if running without Docker)
