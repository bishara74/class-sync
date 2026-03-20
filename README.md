# ClassSync - Attendance Management System

ClassSync is a modern, full-stack web application designed to streamline classroom attendance tracking. It replaces manual roll calls with a secure, time-sensitive digital check-in system. Teachers can generate temporary attendance codes, and students can check in securely using their institutional credentials.

## Features

* **Role-Based Dashboards:** Distinct user interfaces and capabilities for Teachers and Students.
* **Custom Authentication:** Secure login with JWT tokens, BCrypt password hashing, and institutional Neptun Code verification for students.
* **Dynamic Session Generation:** Teachers can create active class sessions with custom expiration timers (e.g., valid for 10 minutes).
* **Cryptographic Check-Ins:** Automatically generates unique 6-digit attendance codes that students use to prove their presence.
* **Real-Time Validation:** The backend verifies code validity, checks expiration times, and prevents duplicate check-ins.

## Tech Stack

**Frontend (primary):**
* Angular 21 + TypeScript
* Tailwind CSS (Styling)
* HTTP Interceptor for JWT auth

**Frontend (legacy):**
* React 19 + Vite + Axios (in `frontend/` directory, unused)

**Backend:**
* Java 17
* Spring Boot 3.5.11
* Spring Data JPA
* RESTful API Architecture
* JWT authentication (jjwt 0.12.6)
* BCrypt password hashing

**Database:**
* PostgreSQL 15

**DevOps:**
* Docker + Docker Compose
* GitLab CI/CD
* Jenkins CI/CD Pipeline
* Ansible playbooks for automated provisioning
* Kubernetes manifests (Minikube-ready)
* Nginx reverse proxy
* AWS EC2 deployment (systemd + Nginx)

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Login with email + password (+ optional neptunCode) | No |
| POST | `/api/attendance/sessions` | Create attendance session | JWT |
| POST | `/api/attendance/check-in` | Student check-in with 6-digit code | JWT |

## Live Demo

Deployed on AWS EC2: [http://13.60.9.90/](http://13.60.9.90/)

## Local Development

```bash
docker-compose up
```

This starts PostgreSQL (port 5433), the Spring Boot backend (port 8081), and the Angular frontend (port 4200).

**Test users seeded automatically:**
* Teacher: `teacher@school.edu` / `pass123`
* Student: `student@school.edu` / `pass123` / neptun `ABC123`

## E2E Tests (Selenium)

12 tests across 4 test classes using headless Chrome + Selenium WebDriver:

* **LoginE2ETest** (5 tests) — login page load, teacher/student login, invalid login, empty fields
* **TeacherFlowE2ETest** (3 tests) — dashboard load, session creation, code format
* **StudentFlowE2ETest** (3 tests) — dashboard load, check-in, invalid code
* **FullFlowE2ETest** (1 test) — end-to-end: teacher creates session → student checks in → teacher verifies

```bash
mvn test -Dtest="com.hodali.classsync.e2e.**"
```

## CI/CD

### GitLab CI/CD (`.gitlab-ci.yml`)
Three jobs across build and test stages:
* **backend-build** — compiles Spring Boot JAR with Maven
* **backend-test** — runs tests against a PostgreSQL 15 service container
* **angular-build** — builds the Angular frontend for production

### Jenkins Pipeline (`Jenkinsfile`)
Four-stage declarative pipeline:
1. **Checkout** — clones the repo, prints branch and build number
2. **Build** — compiles the Spring Boot backend JAR (Maven in Docker)
3. **Test** — runs unit tests against a PostgreSQL 15 container (Selenium E2E tests excluded)
4. **Build Docker Image** — builds and tags `classsync-backend:{build_number}` and `classsync-backend:latest`

Post-build: automatically cleans up test containers and prunes dangling Docker images.

### Ansible Playbooks (`ansible/`)

Automated alternative to the manual `setup.sh` and `deploy.sh` scripts.

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

Roles: `common` (apt + git), `java` (OpenJDK 17), `postgresql` (PostgreSQL 15 + DB/user), `nginx` (reverse proxy via Jinja2 template), `nodejs` (Node.js 20), `app` (clone, build, deploy).

Configuration: edit `group_vars/all.yml` to set database credentials, repo URL, ports, etc.

### Kubernetes (`k8s/`)

Deploy to a local Minikube cluster:

```bash
docker build -t classsync-backend:latest ./backend
minikube image load classsync-backend:latest

kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/database/
kubectl apply -f k8s/backend/
kubectl get pods -n classsync
```

Includes: backend Deployment (2 replicas with health probes and resource limits), PostgreSQL StatefulSet (1Gi persistent storage), ConfigMaps, Secrets, and ClusterIP Services. See [`k8s/README.md`](k8s/README.md) for full details.

### Operational Scripts (`scripts/`)

| Script | Purpose | Usage |
|--------|---------|-------|
| `healthcheck.sh` | Check if API and frontend are alive | `./scripts/healthcheck.sh` |
| `log-monitor.sh` | Analyze service logs for errors | `./scripts/log-monitor.sh` |
| `service-restart.sh` | Auto-restart service if down | `./scripts/service-restart.sh` |

All scripts support environment variable overrides (e.g., `API_URL`, `LOG_LINES`, `MAX_RETRIES`). See script headers for details.

**Cron example** (auto-restart every 5 minutes):
```bash
*/5 * * * * /opt/classsync/scripts/service-restart.sh >> /var/log/classsync-watchdog.log 2>&1
```

### Makefile

```bash
make help          # Show all targets
make up            # Start services with Docker Compose
make down          # Stop services and remove volumes
make build         # Build backend JAR
make test          # Run backend tests
make logs          # Follow backend container logs
make healthcheck   # Run health check
make monitor       # Monitor service logs
make restart       # Auto-restart if service is down
```

## Prerequisites

To run this project locally, you will need:
* Node.js (v18 or higher)
* Java Development Kit (JDK 17 or higher)
* PostgreSQL (v15 or higher)
* Maven
