# Makefile for Sales Management System Docker Operations
# Provides convenient commands for building, testing, and deploying Docker images

# Configuration
IMAGE_NAME := sales-management-backend
IMAGE_TAG := render
FULL_IMAGE := $(IMAGE_NAME):$(IMAGE_TAG)
CONTAINER_NAME := sales-management-test
TEST_PORT := 8083

# Colors for output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[1;33m
RED := \033[0;31m
NC := \033[0m # No Color

# Default target
.PHONY: help
help: ## Show this help message
	@echo "Sales Management System - Docker Operations"
	@echo "==========================================="
	@echo ""
	@echo "Available commands:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(BLUE)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "Examples:"
	@echo "  make build          # Build Docker image"
	@echo "  make test           # Test Docker image"
	@echo "  make run            # Run container locally"
	@echo "  make deploy         # Build, test, and prepare for deployment"

.PHONY: clean
clean: ## Clean up Docker resources
	@echo "$(YELLOW)Cleaning up Docker resources...$(NC)"
	@docker stop $(CONTAINER_NAME) 2>/dev/null || true
	@docker rm $(CONTAINER_NAME) 2>/dev/null || true
	@docker rmi $(FULL_IMAGE) 2>/dev/null || true
	@echo "$(GREEN)Cleanup completed$(NC)"

.PHONY: build
build: clean ## Build Docker image for Render.com
	@echo "$(BLUE)Building Docker image: $(FULL_IMAGE)$(NC)"
	@mvn clean package -DskipTests -q
	@docker build -t $(FULL_IMAGE) .
	@docker tag $(FULL_IMAGE) $(IMAGE_NAME):latest
	@echo "$(GREEN)Docker image built successfully$(NC)"
	@docker images $(IMAGE_NAME)

.PHONY: test
test: ## Test Docker image
	@echo "$(BLUE)Testing Docker image: $(FULL_IMAGE)$(NC)"
	@if [ ! -f .env.render ]; then \
		echo "$(YELLOW)Creating .env.render from template...$(NC)"; \
		cp .env.render.template .env.render; \
		echo "$(YELLOW)Please edit .env.render with your values$(NC)"; \
	fi
	@docker run -d --name $(CONTAINER_NAME) -p $(TEST_PORT):8081 --env-file .env.render $(FULL_IMAGE)
	@echo "$(BLUE)Waiting for application to start...$(NC)"
	@sleep 30
	@if curl -f http://localhost:$(TEST_PORT)/actuator/health >/dev/null 2>&1; then \
		echo "$(GREEN)Health check passed$(NC)"; \
	else \
		echo "$(RED)Health check failed$(NC)"; \
		docker logs $(CONTAINER_NAME); \
		docker stop $(CONTAINER_NAME); \
		docker rm $(CONTAINER_NAME); \
		exit 1; \
	fi
	@docker stop $(CONTAINER_NAME)
	@docker rm $(CONTAINER_NAME)
	@echo "$(GREEN)Docker image test completed successfully$(NC)"

.PHONY: run
run: ## Run container locally with environment variables
	@echo "$(BLUE)Running container locally: $(CONTAINER_NAME)$(NC)"
	@if [ ! -f .env.render ]; then \
		echo "$(YELLOW)Creating .env.render from template...$(NC)"; \
		cp .env.render.template .env.render; \
		echo "$(YELLOW)Please edit .env.render with your values$(NC)"; \
	fi
	@docker stop $(CONTAINER_NAME) 2>/dev/null || true
	@docker rm $(CONTAINER_NAME) 2>/dev/null || true
	@docker run -d --name $(CONTAINER_NAME) -p 8081:8081 --env-file .env.render $(FULL_IMAGE)
	@echo "$(GREEN)Container started successfully$(NC)"
	@echo "Application URL: http://localhost:8081"
	@echo "Health endpoint: http://localhost:8081/actuator/health"
	@echo "Admin interface: http://localhost:8081/admin/"
	@echo ""
	@echo "To stop: make stop"
	@echo "To view logs: make logs"

.PHONY: stop
stop: ## Stop running container
	@echo "$(BLUE)Stopping container: $(CONTAINER_NAME)$(NC)"
	@docker stop $(CONTAINER_NAME) 2>/dev/null || true
	@docker rm $(CONTAINER_NAME) 2>/dev/null || true
	@echo "$(GREEN)Container stopped$(NC)"

.PHONY: logs
logs: ## View container logs
	@echo "$(BLUE)Container logs:$(NC)"
	@docker logs -f $(CONTAINER_NAME)

.PHONY: shell
shell: ## Open shell in running container
	@echo "$(BLUE)Opening shell in container: $(CONTAINER_NAME)$(NC)"
	@docker exec -it $(CONTAINER_NAME) /bin/bash

.PHONY: health
health: ## Check container health
	@echo "$(BLUE)Checking container health...$(NC)"
	@if curl -f http://localhost:8081/actuator/health 2>/dev/null; then \
		echo "$(GREEN)Container is healthy$(NC)"; \
	else \
		echo "$(RED)Container health check failed$(NC)"; \
	fi

.PHONY: stats
stats: ## Show container resource usage
	@echo "$(BLUE)Container resource usage:$(NC)"
	@docker stats $(CONTAINER_NAME) --no-stream

.PHONY: push-dockerhub
push-dockerhub: ## Push image to Docker Hub (requires login)
	@echo "$(BLUE)Pushing to Docker Hub...$(NC)"
	@read -p "Enter Docker Hub username: " username; \
	docker tag $(FULL_IMAGE) $$username/$(IMAGE_NAME):$(IMAGE_TAG); \
	docker tag $(FULL_IMAGE) $$username/$(IMAGE_NAME):latest; \
	docker push $$username/$(IMAGE_NAME):$(IMAGE_TAG); \
	docker push $$username/$(IMAGE_NAME):latest; \
	echo "$(GREEN)Pushed to Docker Hub: $$username/$(IMAGE_NAME)$(NC)"

.PHONY: push-ghcr
push-ghcr: ## Push image to GitHub Container Registry (requires login)
	@echo "$(BLUE)Pushing to GitHub Container Registry...$(NC)"
	@read -p "Enter GitHub username: " username; \
	docker tag $(FULL_IMAGE) ghcr.io/$$username/$(IMAGE_NAME):$(IMAGE_TAG); \
	docker tag $(FULL_IMAGE) ghcr.io/$$username/$(IMAGE_NAME):latest; \
	docker push ghcr.io/$$username/$(IMAGE_NAME):$(IMAGE_TAG); \
	docker push ghcr.io/$$username/$(IMAGE_NAME):latest; \
	echo "$(GREEN)Pushed to GHCR: ghcr.io/$$username/$(IMAGE_NAME)$(NC)"

.PHONY: deploy
deploy: build test ## Build and test image for deployment
	@echo "$(GREEN)Docker image ready for deployment!$(NC)"
	@echo ""
	@echo "Next steps:"
	@echo "1. Push to registry: make push-dockerhub or make push-ghcr"
	@echo "2. Deploy to Render.com using the pushed image"
	@echo "3. Configure environment variables in Render.com dashboard"
	@echo ""
	@echo "Image details:"
	@docker images $(IMAGE_NAME)

.PHONY: render-test
render-test: ## Test with Render.com configuration
	@echo "$(BLUE)Testing Render.com configuration...$(NC)"
	@if [ -f docker-compose.render.yml ]; then \
		docker-compose -f docker-compose.render.yml --env-file .env.render up -d; \
		echo "$(BLUE)Waiting for services to start...$(NC)"; \
		sleep 30; \
		if curl -f http://localhost:8081/actuator/health >/dev/null 2>&1; then \
			echo "$(GREEN)Render.com configuration test passed$(NC)"; \
		else \
			echo "$(RED)Render.com configuration test failed$(NC)"; \
		fi; \
		docker-compose -f docker-compose.render.yml down; \
	else \
		echo "$(RED)docker-compose.render.yml not found$(NC)"; \
	fi

.PHONY: env-check
env-check: ## Check environment variables
	@echo "$(BLUE)Checking environment variables...$(NC)"
	@if [ -f .env.render ]; then \
		echo "$(GREEN).env.render file found$(NC)"; \
		echo "$(BLUE)Environment variables:$(NC)"; \
		grep -v '^#' .env.render | grep -v '^$$' | while read line; do \
			key=$$(echo $$line | cut -d'=' -f1); \
			echo "  ✓ $$key"; \
		done; \
	else \
		echo "$(YELLOW).env.render file not found$(NC)"; \
		echo "$(BLUE)Creating from template...$(NC)"; \
		cp .env.render.template .env.render; \
		echo "$(YELLOW)Please edit .env.render with your values$(NC)"; \
	fi

.PHONY: size
size: ## Show image size information
	@echo "$(BLUE)Docker image size information:$(NC)"
	@docker images $(IMAGE_NAME) --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

.PHONY: info
info: ## Show system information
	@echo "$(BLUE)System Information:$(NC)"
	@echo "Docker version: $$(docker --version)"
	@echo "Docker Compose version: $$(docker-compose --version 2>/dev/null || echo 'Not installed')"
	@echo "Java version: $$(java -version 2>&1 | head -1)"
	@echo "Maven version: $$(mvn --version 2>/dev/null | head -1 || echo 'Not installed')"
	@echo ""
	@echo "$(BLUE)Image Information:$(NC)"
	@if docker image inspect $(FULL_IMAGE) >/dev/null 2>&1; then \
		echo "Image exists: $(GREEN)✓$(NC)"; \
		docker images $(IMAGE_NAME); \
	else \
		echo "Image exists: $(RED)✗$(NC)"; \
		echo "Run 'make build' to create the image"; \
	fi

# Development targets
.PHONY: dev-build
dev-build: ## Build for development (with tests)
	@echo "$(BLUE)Building for development...$(NC)"
	@mvn clean package
	@docker build -t $(IMAGE_NAME):dev .

.PHONY: dev-run
dev-run: ## Run in development mode
	@echo "$(BLUE)Running in development mode...$(NC)"
	@docker run -d --name $(CONTAINER_NAME)-dev -p 8081:8081 \
		-e SPRING_PROFILES_ACTIVE=docker \
		-e LOG_LEVEL=DEBUG \
		$(IMAGE_NAME):dev

# Maintenance targets
.PHONY: prune
prune: ## Remove unused Docker resources
	@echo "$(YELLOW)Removing unused Docker resources...$(NC)"
	@docker system prune -f
	@echo "$(GREEN)Docker cleanup completed$(NC)"

.PHONY: reset
reset: clean prune ## Complete reset (remove all images and containers)
	@echo "$(YELLOW)Performing complete reset...$(NC)"
	@docker rmi $(IMAGE_NAME):latest 2>/dev/null || true
	@docker rmi $(IMAGE_NAME):dev 2>/dev/null || true
	@echo "$(GREEN)Reset completed$(NC)"
