# Auto-detect docker compose command
DOCKER_COMPOSE := $(shell docker compose version > /dev/null 2>&1 && echo "docker compose" || echo "docker-compose")
NEXUS_URL = http://localhost:8081
INFRA_SERVICES ?= nexus keycloak person-postgres prometheus grafana tempo loki

.PHONY: all up start stop clean logs rebuild infra infra-logs infra-stop publish-artifacts build-artifacts build-api clean-build-artifacts clean-build-api

all: up build-artifacts publish-artifacts build-api start

ifeq ($(OS),Windows_NT)
WAIT_CMD = powershell -Command "while ($$true) { \
		try { \
			Invoke-WebRequest -UseBasicParsing -Uri $(NEXUS_URL)/service/rest/v1/status -ErrorAction Stop; \
			break \
		} \
		catch { \
			Write-Host 'Nexus not ready, sleeping...'; \
			Start-Sleep -Seconds 5 \
		} \
	}"
else
WAIT_CMD = until curl -sf $(NEXUS_URL)/service/rest/v1/status; do \
echo 'Nexus not ready, sleeping...'; sleep 5; \
done
endif

up:
	$(DOCKER_COMPOSE) up -d nexus
	@echo "Waiting for Nexus to be healthy..."
	@$(WAIT_CMD)
	@echo "Nexus is healthy!"

build-artifacts:
	@$(DOCKER_COMPOSE) build persons-api

publish-artifacts:
	@echo "Publishing artifacts to Nexus..."
	@docker run --rm \
		--network host \
		-v $(PWD)/person-service:/home/gradle/project \
		-v gradle-cache:/home/gradle/.gradle \
		-w /home/gradle/project \
		-e NEXUS_URL=http://localhost:8081/repository/maven-snapshots \
		-e NEXUS_USERNAME=admin \
		-e NEXUS_PASSWORD=admin \
		gradle:8.14.2-jdk24 \
		bash -c "gradle --no-daemon build -x test && gradle --no-daemon publish --info"

build-api:
	@echo "Building api service (depends on person-api from Nexus)..."
	@$(DOCKER_COMPOSE) build api

start:
	$(DOCKER_COMPOSE) up -d

stop:
	$(DOCKER_COMPOSE) down

clean: stop
	$(DOCKER_COMPOSE) rm -f
	docker volume rm $$(docker volume ls -qf dangling=true) 2>/dev/null || true
	rm -rf ./person-service/build ./api/build

logs:
	$(DOCKER_COMPOSE) logs -f --tail=200

infra:
	@echo "Starting infrastructure services: $(INFRA_SERVICES)"
	$(DOCKER_COMPOSE) up -d $(INFRA_SERVICES)
	@echo "Waiting for Nexus...";
	@$(WAIT_CMD)
	@echo "Waiting for Keycloak...";
	@$(WAIT_CMD)
	@echo "Infrastructure is ready."

infra-logs:
	$(DOCKER_COMPOSE) logs -f --tail=200 $(INFRA_SERVICES)

infra-stop:
	$(DOCKER_COMPOSE) stop $(INFRA_SERVICES)

clean-build-artifacts:
	@echo "Building persons-api from scratch (no cache)..."
	@$(DOCKER_COMPOSE) build persons-api --no-cache

clean-build-api:
	@echo "Building api from scratch (no cache)..."
	@$(DOCKER_COMPOSE) build api --no-cache

rebuild: clean all