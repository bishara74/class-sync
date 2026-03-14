## ClassSync — Project Summary

**ClassSync** is a full-stack classroom attendance tracking app. Teachers create time-limited attendance sessions with auto-generated 6-digit codes; students enter the code to check in and get marked PRESENT or LATE based on timing.

### Tech Stack
- **Backend:** Java 17 + Spring Boot 3.5.11, Spring Data JPA, Maven
- **Frontend (primary):** Angular 21 + TypeScript + Tailwind CSS
- **Frontend (legacy):** React 19 + Vite + Axios (in `frontend/` directory)
- **Database:** PostgreSQL 15
- **DevOps:** Docker + Docker Compose, GitLab CI/CD, Nginx reverse proxy, AWS EC2 deployment (systemd + Nginx)

### Key Structure
```
class-sync/
├── backend/                  # Spring Boot REST API (port 8081)
│   └── src/main/java/com/hodali/classsync/
│       ├── controller/       # AuthController, AttendanceController
│       ├── service/          # AttendanceService, JwtService
│       ├── config/           # JwtAuthFilter, FilterConfig, CorsConfig
│       ├── model/            # User, AttendanceSession, AttendanceRecord + enums
│       ├── dto/              # LoginRequest, LoginResponse, CreateSessionRequest, CheckInRequest
│       └── repository/       # JPA repositories
│   └── src/test/java/com/hodali/classsync/e2e/  # Selenium E2E tests
├── angular-frontend/         # Angular 21 app (port 4200 dev / 80 Docker)
│   └── src/app/
│       ├── pages/            # login, teacher-dashboard, student-dashboard
│       ├── services/         # auth.service, attendance.service, auth.interceptor
│       ├── models/           # TypeScript interfaces matching backend DTOs
│       └── guards/           # authGuard + roleGuard
├── frontend/                 # Legacy React frontend
├── deployment/
│   └── ec2/                  # AWS EC2 deployment configs
│       ├── setup.sh          # One-time instance setup script
│       ├── deploy.sh         # Build & deploy script
│       ├── nginx.conf        # Nginx reverse proxy config
│       ├── classsync.service # Systemd service file
│       └── README.md         # EC2 deployment guide
├── docker-compose.yml        # PostgreSQL + backend + angular-frontend
└── .gitlab-ci.yml            # build + test stages
```

### API Endpoints
- **POST /api/auth/login** — email + password + optional neptunCode → `{ token, user }` (JWT)
- **POST /api/attendance/sessions** — teacher creates session (courseName, validForMinutes) → gets 6-digit code *(requires JWT)*
- **POST /api/attendance/check-in** — student submits code → PRESENT/LATE status *(requires JWT)*

### Database Models
- **User** — id, name, email, role (TEACHER/STUDENT), password, neptunCode
- **AttendanceSession** — id, teacher (FK), courseName, generatedCode, expirationTime
- **AttendanceRecord** — id, student (FK), session (FK), checkInTime, status (PRESENT/LATE/ABSENT)

### Auth & Security
- ✅ Passwords hashed with BCrypt (`spring-security-crypto`)
- ✅ JWT authentication (jjwt 0.12.6) — tokens returned on login, 24h expiration
- ✅ `JwtAuthFilter` protects `/api/attendance/**` endpoints — returns 401 for missing/invalid tokens
- ✅ Angular stores JWT in localStorage, sends via HTTP interceptor (`auth.interceptor.ts`)
- ✅ CORS restricted to `localhost:4200`, `localhost:5173`, `localhost:3000`
- Role-based route guards on frontend (`authGuard` + `roleGuard`)

### CI/CD
- Maven compiler plugin pinned to 3.13.0 (fixes `asm:9.8` resolution failure in CI)

### Dev Setup
- `docker-compose up` spins up everything (DB on port 5433, backend on 8081, frontend on 4200)
- Auto-seeds test users: teacher `teacher@school.edu`/`pass123`, student `student@school.edu`/`pass123`/neptun `ABC123`
- Hibernate `ddl-auto: update` auto-generates schema
- **Note:** If upgrading from plaintext passwords, drop the `users` table so seed data re-runs with hashed passwords

### E2E Tests (Selenium)
- **12 tests** across 4 test classes using headless Chrome + Selenium WebDriver
- `LoginE2ETest` (5 tests) — login page load, teacher/student login success, invalid login error, empty field prevention
- `TeacherFlowE2ETest` (3 tests) — dashboard load, session creation, generated code format
- `StudentFlowE2ETest` (3 tests) — dashboard load, successful check-in, invalid code error
- `FullFlowE2ETest` (1 test) — end-to-end: teacher creates session → student checks in → teacher verifies
- **Shared Chrome instance** via JUnit 5 `TestSuiteExtension` (single browser reused across all test classes)
- **Login bypass**: tests that need authentication use direct API calls via `executeAsyncScript` + `fetch()` to avoid Angular form binding timing issues; only `LoginE2ETest` exercises the actual login UI
- Run with: `mvn test -Dtest="com.hodali.classsync.e2e.**"`

### Known Gaps
1. ~~Passwords not hashed~~ ✅ Fixed — BCrypt
2. ~~No JWT/session tokens~~ ✅ Fixed — JWT auth
3. ~~No backend authorization checks~~ ✅ Fixed — JwtAuthFilter
4. ~~CORS wide open~~ ✅ Fixed — restricted origins
5. React frontend is legacy/unused but still in repo
