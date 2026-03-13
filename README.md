# ClassSync - Attendance Management System

ClassSync is a modern, full-stack web application designed to streamline classroom attendance tracking. It replaces manual roll calls with a secure, time-sensitive digital check-in system. Teachers can generate temporary attendance codes, and students can check in securely using their institutional credentials.

## Features

* **Role-Based Dashboards:** Distinct user interfaces and capabilities for Teachers and Students.
* **Custom Authentication:** Secure login flow incorporating standard credentials and institutional Neptun Codes for student verification.
* **Dynamic Session Generation:** Teachers can create active class sessions with custom expiration timers (e.g., valid for 10 minutes).
* **Cryptographic Check-Ins:** Automatically generates unique 6-digit attendance codes that students use to prove their presence.
* **Real-Time Validation:** The backend verifies code validity, checks expiration times, and prevents duplicate check-ins.

## Tech Stack

**Frontend:**
* React.js
* Vite (Build Tool)
* Tailwind CSS (Styling)
* React Router (Navigation)
* Axios (HTTP Client)

**Backend:**
* Java 17
* Spring Boot
* Spring Data JPA
* RESTful API Architecture

**Database:**
* PostgreSQL

## Deployment

### Option 1: Local Development (Docker Compose)

```bash
docker-compose up
```

This starts PostgreSQL (port 5433), the Spring Boot backend (port 8081), and the Angular frontend (port 4200).

### Option 2: AWS EC2

Deploy to a single EC2 instance with Nginx reverse proxy and systemd service management.

```
Internet → Nginx (port 80)
              ├── /api/*  → Spring Boot (port 8081) → PostgreSQL (port 5432)
              └── /*      → Angular static files
```

See the full guide: [deployment/ec2/README.md](deployment/ec2/README.md)

## Prerequisites

To run this project locally, you will need:
* Node.js (v18 or higher)
* Java Development Kit (JDK 17 or higher)
* PostgreSQL (v16 or higher)
* Maven

