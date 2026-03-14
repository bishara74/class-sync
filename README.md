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
* Nginx reverse proxy
* AWS EC2 deployment (systemd + Nginx)

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Login with email + password (+ optional neptunCode) | No |
| POST | `/api/attendance/sessions` | Create attendance session | JWT |
| POST | `/api/attendance/check-in` | Student check-in with 6-digit code | JWT |

## Deployment

### Option 1: Local Development (Docker Compose)

```bash
docker-compose up
```

This starts PostgreSQL (port 5433), the Spring Boot backend (port 8081), and the Angular frontend (port 4200).

**Test users seeded automatically:**
* Teacher: `teacher@school.edu` / `pass123`
* Student: `student@school.edu` / `pass123` / neptun `ABC123`

### Option 2: AWS EC2

Deploy to a single EC2 instance with Nginx reverse proxy and systemd service management.

```
Internet → Nginx (port 80)
              ├── /api/*  → Spring Boot (port 8081) → PostgreSQL (port 5432)
              └── /*      → Angular static files
```

See the full guide: [deployment/ec2/README.md](deployment/ec2/README.md)

## E2E Tests (Selenium)

12 tests across 4 test classes using headless Chrome + Selenium WebDriver:

* **LoginE2ETest** (5 tests) — login page load, teacher/student login, invalid login, empty fields
* **TeacherFlowE2ETest** (3 tests) — dashboard load, session creation, code format
* **StudentFlowE2ETest** (3 tests) — dashboard load, check-in, invalid code
* **FullFlowE2ETest** (1 test) — end-to-end: teacher creates session → student checks in → teacher verifies

```bash
mvn test -Dtest="com.hodali.classsync.e2e.**"
```

## Prerequisites

To run this project locally, you will need:
* Node.js (v18 or higher)
* Java Development Kit (JDK 17 or higher)
* PostgreSQL (v15 or higher)
* Maven
