# RAG Framework

A modular Retrieval-Augmented Generation (RAG) application built with Spring Boot and Spring AI. LLM providers, models, and vector stores are swappable via Maven modules and configuration, without changing application code.

The application itself lives in [`mainApp/`](./mainApp), a multi-module Maven project. This README covers building, configuring, and running it via Docker.

## Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- Java 24 and Maven, **only** if you intend to build outside Docker (not required for the standard setup below)

## Project structure

```
mainApp/
├── docker-compose.yml               # base stack: app + db
├── docker-compose.ollama.yml        # overlay: Ollama container + config
├── docker-compose.openai.yml        # overlay: OpenAI config (no container)
├── docker-compose.elasticsearch.yml # overlay: Elasticsearch + index init
├── Dockerfile
├── .env.example                     # copy to .env and fill in
├── pom.xml                          # parent POM, lists all modules
├── rag-app/                         # main application module (edit pom.xml here to select modules)
├── rag-core/
├── rag-starter-provider-*/          # LLM provider integrations
├── rag-starter-llm-*/               # specific LLM starters
├── rag-starter-vectorstore-*/       # vector store integrations
├── rag-starter-parser-*/            # document parsers
├── rag-integration-tests/
└── es-init/
    └── create-index.sh              # creates the Elasticsearch index/mapping on startup
```

## Quick start

All commands below are run from the `mainApp/` directory:

```bash
cd mainApp
```

### 1. Create your configuration files from the templates

```bash
cp .env.example .env
cp rag-app/src/main/resources/application.yaml.example rag-app/src/main/resources/application.yaml
```

Neither `.env` nor `application.yaml` is committed to the repository, since both can contain environment-specific values and credentials. You must create them locally before running the application.

Edit `.env` and fill in:
- `OLLAMA_CHAT_MODEL` / `OLLAMA_EMBED_MODEL` — if using the Ollama overlay
- `OPENAI_API_KEY` — if using the OpenAI overlay
- `ANTHROPIC_API_KEY` — used for the test judge, independent of the active LLM provider
- `COMPOSE_FILE` — optionally set this to the overlay combination you want (see below), so you don't need to pass `-f` flags every time. **On Windows, separate paths with `;`; on Linux/macOS, use `:`.**

`application.yaml` should already be wired to read its values from the same environment variables — you shouldn't need to hardcode secrets there.

### 2. Choose your module configuration

The active LLM provider and vector store are determined by which starter modules `rag-app/pom.xml` depends on. Check (or edit) the `<dependencies>` section of `rag-app/pom.xml` to confirm which are active, e.g.:

```xml
<dependency>
    <groupId>cz.cuni.mff.hanaf</groupId>
    <artifactId>rag-starter-llm-deepseek-ollama</artifactId>
</dependency>
<dependency>
    <groupId>cz.cuni.mff.hanaf</groupId>
    <artifactId>rag-starter-vectorstore-elasticsearch</artifactId>
</dependency>
```

Swapping providers means changing these dependencies and rebuilding.

### 3. Start the stack

Pick the overlay files matching your chosen modules. For the default Ollama + Elasticsearch setup:

```bash
docker compose -f docker-compose.yml -f docker-compose.ollama.yml -f docker-compose.elasticsearch.yml up --build
```

Or, if you set `COMPOSE_FILE` in `.env`:

```bash
docker compose up --build
```

## Configuration reference

| File | Purpose | Committed? |
|---|---|---|
| `.env` | Model names, API keys, active Compose overlay selection | No (see `.env.example`) |
| `application.yaml` | Spring configuration; reads most values from the environment | No (see `application.yaml.example`) |
| `rag-app/pom.xml` | Selects which LLM provider / vector store modules are compiled in | Yes |
| `docker-compose*.yml` | Infrastructure wiring per module choice | Yes |

## Troubleshooting

- **`no main manifest attribute, in app.jar`** — the Spring Boot Maven plugin didn't repackage the jar. Check `rag-app/pom.xml` for `spring-boot-maven-plugin` and confirm it isn't set to `<skip>true</skip>` for the build you're running.
- **`exec ...: no such file or directory` on a shell script** — usually Windows CRLF line endings in a `.sh` file bind-mounted into a Linux container. Convert to LF (a `.gitattributes` entry of `*.sh text eol=lf` prevents this going forward).
- **`ollama` container stuck `unhealthy`** — check what it actually has with `docker compose exec ollama ollama list`; the healthcheck only confirms the server responds, not that a specific model is present.
- **`relation "..." does not exist` errors despite the table existing** — often caused by re-running `data.sql` against an already-seeded database; see the note above.
