# LLM Gateway

A scalable, multi-provider **LLM Gateway** that presents a single, unified API in front of OpenAI, Google Gemini, Anthropic Claude, and NVIDIA-hosted models. The gateway adds **intelligent routing**, **cost optimization**, **prompt caching**, **circuit-breaker resilience**, **usage & cost accounting**, and **authentication / rate limiting** on top of the underlying providers — the same architectural role an API gateway plays for microservices, applied to Large Language Models.

> Capstone project for the M.Sc. Computer Science program — Scaler Neovarsity / Woolf.
> **Project title:** *Design and Implementation of a Scalable Multi-Provider LLM Gateway with Intelligent Routing and Cost Optimization.*

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [1. Start infrastructure (PostgreSQL + Redis)](#1-start-infrastructure-postgresql--redis)
  - [2. Configure provider API keys](#2-configure-provider-api-keys)
  - [3. Build and run](#3-build-and-run)
- [Configuration](#configuration)
- [Authentication](#authentication)
- [API Reference](#api-reference)
- [Command-Line Interface (CLI)](#command-line-interface-cli)
- [Routing Strategies](#routing-strategies)
- [Data Model](#data-model)
- [Project Structure](#project-structure)
- [Resilience & Health Scoring](#resilience--health-scoring)
- [Rate Limiting](#rate-limiting)
- [License](#license)

---

## Features

- **Unified completions API** — one endpoint for every provider; responses are normalized into a single shape.
- **Intelligent routing** — pick a model automatically by `CHEAPEST`, `FASTEST`, or `CAPABILITY_BASED` strategy, weighted by a live health score.
- **Cost optimization** — route inexpensive workloads to inexpensive models and serve repeated prompts from cache, reducing blended cost dramatically.
- **Prompt caching** — Redis-backed, SHA-256-keyed cache (10-minute TTL) shared by streaming and non-streaming paths.
- **Multi-model comparison** — run one prompt across several models in parallel.
- **Streaming** — token-by-token responses over Server-Sent Events, including a parallel multi-model stream.
- **Resilience** — Resilience4j circuit breaker with a safe global fallback (never cached or billed).
- **Health scoring** — per-model success-rate, latency, and timeout metrics feeding a 0–100 score.
- **Usage & cost tracking** — every billable request logged with token counts and computed cost, attributed to a user and API key.
- **Security** — dual JWT + API-key authentication, BCrypt hashing, per-key and per-IP rate limits.

---

## Architecture

Requests flow through an ordered chain of security/edge filters, into thin REST controllers, down to a small set of core services that resolve providers through a registry and call the external LLM APIs. PostgreSQL holds durable state; Redis holds the prompt cache and rate-limit counters.

```
Client
  │  (Bearer JWT or sk_live_ API key)
  ▼
[ RateLimitFilter ] → [ ApiKeyAuthFilter ] → [ JwtAuthFilter ]
  ▼
REST Controllers ── /v1/chat/completions · /route · /compare · /stream · /admin/models
  ▼
RoutingOrchestrator ─► IntelligentRoutingEngine (Cost / Latency / Capability)
  ▼
LlmExecutionService  ──► PromptCacheService (Redis)
  │                   ──► UsageService + CostCalculator (PostgreSQL)
  ▼
AdvancedModelRouter ─► ResilientProviderExecutor (@CircuitBreaker)
  ▼
ProviderRegistry ─► OpenAi / Gemini / Claude / Nvidia Provider
  ▼
External LLM Provider APIs
```

A full architecture diagram, class diagram, ER schema, request-flow sequence, and deployment diagram are included in the project report (`LLM_Gateway_Project_Report.docx`).

---

## Technology Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security, JJWT (JWT), BCrypt |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL 15 |
| Cache / counters | Redis 7 |
| Reactive / streaming | Spring WebFlux, Server-Sent Events |
| Resilience | Resilience4j (circuit breaker) |
| Build | Maven (wrapper included) |
| Local infra | Docker Compose |

---

## Getting Started

### Prerequisites

- JDK 17+
- Docker & Docker Compose (for PostgreSQL and Redis)
- API keys for the providers you intend to enable

### 1. Start infrastructure (PostgreSQL + Redis)

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 15** on `localhost:5432` (db `llm_gateway`, user `llm_user`, password `llm_password`)
- **Redis 7** on `localhost:6379`

### 2. Configure provider API keys

The application reads provider keys from environment variables (see `src/main/resources/application.yaml`):

```bash
export OPENAI_API_KEY="sk-..."
export GEMINI_API_KEY="..."
export ANTHROPIC_API_KEY="sk-ant-..."
export NVIDIA_API_KEY="nvapi-..."
```

You only need keys for the providers you keep `enabled: true`.

### 3. Build and run

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
# or
java -jar target/llm-gateway-0.0.1-SNAPSHOT.jar
```

The service starts on **http://localhost:8080**. On first launch the schema is created by Hibernate and the model catalogue is seeded idempotently from `data.sql`.

---

## Configuration

Key settings live in `src/main/resources/application.yaml`:

| Setting | Default | Notes |
|---------|---------|-------|
| `server.port` | `8080` | HTTP port |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/llm_gateway` | PostgreSQL connection |
| `spring.redis.host` / `port` | `localhost` / `6379` | Redis connection |
| `providers.configs.<name>.enabled` | `true` | Toggle a provider on/off |
| `resilience4j.circuitbreaker.instances.llmProvider` | see below | Circuit-breaker tuning |

**Circuit breaker (`llmProvider`):** `failureRateThreshold: 50`, `minimumNumberOfCalls: 5`, `slidingWindowSize: 10`, `waitDurationInOpenState: 10s`.

> **Note:** The JWT signing secret and provider keys shown in the local profile are for development only. In production, inject them from a secrets manager and never commit real secrets.

---

## Authentication

Two mechanisms are supported, both via the `Authorization: Bearer <token>` header:

1. **JWT** — obtained from `/api/auth/login`; short-lived (15 min) access token, renewable with a refresh token.
2. **API key** — generated at `/api/keys`; format `sk_live_...`. Only a BCrypt hash + 12-char prefix is stored; the raw key is shown once.

Public endpoints (`/api/auth/**`) require no authentication; all others do.

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"secret123"}'

# Login → returns a JWT access token + refresh token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"secret123"}'

# Generate an API key (use the JWT from login)
curl -X POST http://localhost:8080/api/keys \
  -H "Authorization: Bearer <JWT>"
```

---

## API Reference

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/auth/register` | Create a user account |
| POST | `/api/auth/login` | Authenticate; returns JWT + refresh token |
| POST | `/api/auth/refresh` | Exchange a refresh token for a new access token |
| POST | `/api/auth/logout` | Invalidate the active refresh token |
| POST | `/api/keys` | Generate a new API key |
| POST | `/v1/chat/completions` | Unified chat completion for a named model |
| POST | `/v1/chat/route` | Intelligent routing by strategy |
| POST | `/v1/chat/compare` | Parallel multi-model comparison |
| POST | `/v1/chat/stream` | Single-model SSE stream |
| POST | `/v1/chat/stream/parallel` | Parallel multi-model SSE stream |
| POST / GET | `/admin/models` | Register / list model metadata |

### Examples

**Chat completion**

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Authorization: Bearer sk_live_..." \
  -H "Content-Type: application/json" \
  -d '{"model":"gpt-4o-mini","prompt":"Explain circuit breakers in one paragraph."}'
```

**Intelligent routing (cheapest model)**

```bash
curl -X POST http://localhost:8080/v1/chat/route \
  -H "Authorization: Bearer sk_live_..." \
  -H "Content-Type: application/json" \
  -d '{
        "routing": { "strategy": "CHEAPEST", "requestType": "CHAT" },
        "messages": [ { "role": "user", "content": "Summarize this text..." } ]
      }'
```

**Streaming (Server-Sent Events)**

```bash
curl -N -X POST http://localhost:8080/v1/chat/stream \
  -H "Authorization: Bearer sk_live_..." \
  -H "Content-Type: application/json" \
  -d '{"model":"claude-haiku-4-5","prompt":"Write a haiku about gateways."}'
```

---

## Command-Line Interface (CLI)

A companion command-line client, **[multi-llm-cli](https://github.com/wasim-h-sheikh/multi-llm-cli)**, provides convenient terminal access to every major gateway endpoint. It is a Node.js tool built with `commander`, `axios`, and `dotenv`, and installs an `llm` command.

### Install

```bash
git clone https://github.com/wasim-h-sheikh/multi-llm-cli.git
cd multi-llm-cli
npm install
npm link        # optional: exposes the `llm` command globally
```

### Configure

Create a `.env` file pointing at your gateway and an API key:

```bash
API_URL=http://localhost:8080
API_KEY=sk_live_...
```

The CLI sends the key as `Authorization: Bearer <API_KEY>` on every request.

### Commands

| Command | Maps to | Description |
|---------|---------|-------------|
| `llm chat` | `POST /v1/chat/completions` | Single completion against a named model |
| `llm compare` | `POST /v1/chat/compare` | Run one prompt across several models |
| `llm route` | `POST /v1/chat/route` | Intelligent routing by strategy |
| `llm stream` | `POST /v1/chat/stream` | Stream a single model's response (SSE) |
| `llm multi-stream` | `POST /v1/chat/stream/parallel` | Stream the same prompt to several models in parallel |
| `llm models:create` | `POST /admin/models` | Register a new model in the catalogue |
| `llm models:list` | `GET /admin/models` | List the model catalogue |
| `llm models:disable` | `PATCH /admin/models/{id}/disable` | Disable a model |

### Examples

```bash
# Single completion
llm chat --model gpt-4o-mini --prompt "Explain circuit breakers in one paragraph."

# Compare several models on the same prompt
llm compare --models "gpt-4o-mini,gemini-2.5-flash,claude-haiku-4-5" \
  --prompt "Give me a one-line tagline for an LLM gateway."

# Intelligent routing (cheapest model for a chat task)
llm route --strategy CHEAPEST --requestType CHAT \
  --prompt "Summarize the benefits of an API gateway."

# Stream a single model
llm stream --provider anthropic --model claude-haiku-4-5 \
  --prompt "Write a haiku about gateways."

# Parallel multi-model stream (provider:model pairs)
llm multi-stream \
  --models "openai:gpt-4o-mini,google:gemini-2.5-flash,anthropic:claude-haiku-4-5" \
  --prompt "Describe load balancing in two sentences."

# Administer the model catalogue
llm models:list
llm models:create --name gpt-4o-mini --provider OPENAI \
  --providerBeanName openAiProvider \
  --inputCost 0.00015 --outputCost 0.00060 \
  --latency 600 --contextWindow 128000
```

---

## Routing Strategies

| Strategy | Component | Score (lower is better) |
|----------|-----------|--------------------------|
| `CHEAPEST` | `CostBasedRouter` | `inputCostPer1k × 100000 − healthScore × 5` |
| `FASTEST` | `LatencyBasedRouter` | `avgLatencyMs − healthScore × 5` |
| `CAPABILITY_BASED` | `CapabilityRouter` | filter by capability, then `avgLatencyMs − healthScore × 5` |

Each decision returns a human-readable reason (e.g. `"Cost optimized routing (cost=0.000150 health=100.00)"`) so routing is auditable.

**Seeded models** (`data.sql`):

| Model | Provider | Input $/1K | Output $/1K | Latency | Context |
|-------|----------|-----------|------------|---------|---------|
| `gpt-4o-mini` | OpenAI | 0.00015 | 0.00060 | 600 ms | 128K |
| `gemini-2.5-flash` | Google | 0.00030 | 0.00250 | 500 ms | 1,048,576 |
| `claude-haiku-4-5` | Anthropic | 0.00100 | 0.00500 | 400 ms | 200K |

---

## Data Model

| Table | Responsibility |
|-------|----------------|
| `users` | Account identity, credentials (BCrypt), role |
| `api_key` | Hashed, revocable programmatic credentials linked to a user |
| `refresh_token` | Long-lived tokens enabling short-lived JWT renewal |
| `model_metadata` | Catalogue of routable models: pricing, latency, capabilities, health metrics |
| `model_capabilities` | Set of capabilities per model (CHAT, CODING, REASONING, VISION, …) |
| `usage_logs` | Append-only ledger of billable requests with token counts and cost |

---

## Project Structure

```
src/main/java/com/ohan/llmgateway/
├── auth/          # registration, login, JWT, refresh tokens
├── apikey/        # API key generation, storage, authentication filter
├── security/      # token resolver, principal, security config helpers
├── ratelimit/     # Redis token-bucket rate limiting
├── chat/          # /v1/chat/completions controller + DTOs
├── routing/       # intelligent routing engine, strategies, orchestrator
├── router/        # AdvancedModelRouter (ModelRouter interface)
├── execution/     # LlmExecutionService (central cache + usage path)
├── compare/       # parallel multi-model comparison
├── streaming/     # WebFlux SSE streaming + parallel streaming
├── cache/         # Redis prompt cache
├── usage/         # usage logging + cost calculation
├── health/        # per-model health scoring
├── resilience/    # circuit breaker wrapper + fallback
├── provider/      # LlmProvider interface, registry, provider adapters
├── model/         # model metadata entity, enums, admin controller
├── config/        # security config, global exception handling
└── common/        # error codes, exceptions, request-id filter
src/main/resources/
├── application.yaml
└── data.sql       # idempotent model seeding
docker-compose.yml # PostgreSQL + Redis
```

---

## Resilience & Health Scoring

Every provider call runs through `ResilientProviderExecutor` (`@CircuitBreaker`). When the failure rate crosses 50% over a sliding window of 10 calls, the breaker opens for 10 seconds and a fallback response is returned — **fallback responses are never cached or billed**.

After each call the model's health score is recomputed:

```
healthScore = successRate
            − (timeoutCount   × 2.0)     # timeout penalty
            − (failedRequests × 1.5)     # failure penalty
            − (avgLatencyMs   / 1000.0)  # latency penalty
            clamped to [0, 100]
```

Routers add a small bonus proportional to this score, so traffic is steered toward historically reliable models automatically.

---

## Rate Limiting

Limits are enforced in Redis so they hold across all instances:

| Scope | Limit | Key pattern |
|-------|-------|-------------|
| Per IP address | 100 / minute | `rate_limit:ip:<ip>` |
| Per API key | 20 / minute | `rate_limit:apikey:<prefix>` |

Exceeding a limit returns **HTTP 429**.

---

## License

Proprietary and confidential. © 2026 Wasim Sheikh. Unauthorized copying of any file in this repository, via any medium, is strictly prohibited.
