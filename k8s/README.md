# ClassSync — Kubernetes Manifests

Kubernetes manifests for deploying the ClassSync backend and PostgreSQL database. Designed for local testing with Minikube.

## Directory Structure

| File | Description |
|------|-------------|
| `namespace.yml` | Creates the `classsync` namespace |
| `backend/configmap.yml` | Backend environment variables (datasource URL, username, port) |
| `backend/secret.yml` | Backend secrets (database password, JWT secret) — **change in production** |
| `backend/deployment.yml` | Backend deployment (2 replicas, resource limits, health probes) |
| `backend/service.yml` | ClusterIP service exposing the backend on port 8081 |
| `database/configmap.yml` | PostgreSQL config (database name, user) |
| `database/secret.yml` | PostgreSQL password — **change in production** |
| `database/statefulset.yml` | PostgreSQL StatefulSet with 1Gi persistent storage |
| `database/service.yml` | ClusterIP service exposing PostgreSQL on port 5432 |

## Quick Start (Minikube)

### 1. Start Minikube

```bash
minikube start
```

### 2. Build and load the backend Docker image

```bash
docker build -t classsync-backend:latest ./backend
minikube image load classsync-backend:latest
```

### 3. Apply the manifests

```bash
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/database/
kubectl apply -f k8s/backend/
```

### 4. Verify pods are running

```bash
kubectl get pods -n classsync
```

### 5. Access the backend (port-forward)

```bash
kubectl port-forward -n classsync svc/backend-service 8081:8081
```

The API is now available at `http://localhost:8081`.

## Notes

- Secrets use base64-encoded placeholder values. Replace them with real credentials in production (consider using Sealed Secrets or an external secrets manager).
- The backend uses `imagePullPolicy: IfNotPresent` so it picks up the locally loaded image in Minikube.
- PostgreSQL data is persisted via a 1Gi PersistentVolumeClaim.
