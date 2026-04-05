SHELL := /bin/bash

MVN := ./mvnw
DOCKER_COMPOSE := docker compose

.PHONY: help up down restart logs \
	run-order-dev run-order-sqs run-inventory run-invoice-rabbit run-invoice-sqs run-notification \
	test-order test-inventory test-invoice test-notification \
	build-order build-inventory build-invoice build-notification

help:
	@echo "Available targets:"
	@echo "  make up                  Start infrastructure containers"
	@echo "  make down                Stop infrastructure containers"
	@echo "  make restart             Recreate infrastructure containers"
	@echo "  make logs                Follow infrastructure logs"
	@echo "  make run-order-dev       Run order-api with dev profile"
	@echo "  make run-order-sqs       Run order-api with sqs profile"
	@echo "  make run-inventory       Run inventory-service"
	@echo "  make run-invoice-rabbit  Run invoice-woker with rabbit profile"
	@echo "  make run-invoice-sqs     Run invoice-woker with sqs profile"
	@echo "  make run-notification    Run notification-service"
	@echo "  make test-order          Run order-api tests"
	@echo "  make test-inventory      Run inventory-service tests"
	@echo "  make test-invoice        Run invoice-woker tests"
	@echo "  make test-notification   Run notification-service tests"
	@echo "  make build-order         Build order-api"
	@echo "  make build-inventory     Build inventory-service"
	@echo "  make build-invoice       Build invoice-woker"
	@echo "  make build-notification  Build notification-service"

up:
	$(DOCKER_COMPOSE) up -d postgres kafka rabbitmq localstack

down:
	$(DOCKER_COMPOSE) down

restart:
	$(DOCKER_COMPOSE) down
	$(DOCKER_COMPOSE) up -d postgres kafka rabbitmq localstack

logs:
	$(DOCKER_COMPOSE) logs -f

run-order-dev:
	cd order-api && $(MVN) spring-boot:run

run-order-sqs:
	cd order-api && $(MVN) spring-boot:run -Dspring-boot.run.profiles=sqs

run-inventory:
	cd inventory-service && $(MVN) spring-boot:run

run-invoice-rabbit:
	cd invoice-woker && $(MVN) spring-boot:run -Dspring-boot.run.profiles=rabbit

run-invoice-sqs:
	cd invoice-woker && $(MVN) spring-boot:run -Dspring-boot.run.profiles=sqs

run-notification:
	cd notification-service && $(MVN) spring-boot:run

test-order:
	cd order-api && $(MVN) test

test-inventory:
	cd inventory-service && $(MVN) test

test-invoice:
	cd invoice-woker && $(MVN) test

test-notification:
	cd notification-service && $(MVN) test

build-order:
	cd order-api && $(MVN) -q -DskipTests compile

build-inventory:
	cd inventory-service && $(MVN) -q -DskipTests compile

build-invoice:
	cd invoice-woker && $(MVN) -q -DskipTests compile

build-notification:
	cd notification-service && $(MVN) -q -DskipTests compile
