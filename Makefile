# =============================================================================
# ClassSync — Makefile
#
# Common commands for development, deployment, and operations.
# Run "make help" to see all available targets.
# =============================================================================

.DEFAULT_GOAL := help

# ---------------------------------------------------------------------------
# Docker Compose
# ---------------------------------------------------------------------------

.PHONY: up
up: ## Start all services with Docker Compose (build + detached)
	docker compose up --build -d

.PHONY: down
down: ## Stop all services and remove volumes
	docker compose down -v

.PHONY: logs
logs: ## Follow backend container logs
	docker compose logs -f backend

# ---------------------------------------------------------------------------
# Backend (Maven)
# ---------------------------------------------------------------------------

.PHONY: build
build: ## Build the Spring Boot backend JAR (skip tests)
	cd backend && mvn clean package -DskipTests

.PHONY: test
test: ## Run backend tests
	cd backend && mvn test

# ---------------------------------------------------------------------------
# Operational Scripts
# ---------------------------------------------------------------------------

.PHONY: healthcheck
healthcheck: ## Run health check against API and frontend
	./scripts/healthcheck.sh

.PHONY: monitor
monitor: ## Monitor service logs for errors
	./scripts/log-monitor.sh

.PHONY: restart
restart: ## Health-check and auto-restart if service is down
	./scripts/service-restart.sh

# ---------------------------------------------------------------------------
# Help
# ---------------------------------------------------------------------------

.PHONY: help
help: ## Show this help message
	@echo ""
	@echo "ClassSync — Available Make Targets"
	@echo "==================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'
	@echo ""
