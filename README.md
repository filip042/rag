# RAG Framework

A modular Retrieval-Augmented Generation (RAG) application built with Spring Boot and Spring AI. LLM providers, models, and vector stores are swappable via Maven modules and configuration, without changing application code.

The application itself lives in [`mainApp/`](./mainApp), a multi-module Maven project. This README covers building, configuring, and running it via Docker.

## Prerequisites

- [Docker](https://www.docker.com/) and Docker Compose
- Java 24 and Maven, **only** if you intend to build outside Docker (not required for the standard setup below)

## Project structure

```
mainApp/
├── docker-compose.yml               # base stack: app + db + docs generation
├── docker-compose.ollama.yml        # overlay: Ollama container + config
├── docker-compose.openai.yml        # overlay: OpenAI config (no container)
├── docker-compose.elasticsearch.yml # overlay: Elasticsearch + index init
├── Dockerfile
├── .env_template                     # copy to .env and fill in
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

Create your configuration files from the templates. Neither `.env` nor `application.yaml` is committed to the repository, since both can contain environment-specific values and credentials — this step is required, but the defaults work as-is, so no editing is needed to get started:

```bash
cp .env_template .env
cp rag-app/src/main/resources/application.yaml_template rag-app/src/main/resources/application.yaml
```

Start the stack (default Ollama + Elasticsearch setup):

```bash
docker compose -f docker-compose.yml -f docker-compose.ollama.yml -f docker-compose.elasticsearch.yml up --build
```

On the first start, Ollama will download the configured models, which are several GB in size — expect this to take a few minutes depending on your connection. The stack is ready once the logs show the application has started.

## Try it out

When the log shows the app has started, open **http://localhost:8080** in a browser.

Log in with the seeded demo account — username `user`, password `user`.

Open the pre-loaded **Book Club** project, which contains indexed Wikipedia articles about classic fantasy literature. Ask in the chat:

> Who wrote The Hobbit?

You should get an answer naming **J. R. R. Tolkien**. If instead you get "There isn't enough information to answer that question", the Elasticsearch index is empty. Check `docker logs mainapp-es-init-1` for seeding errors.

## Configuration reference

If you want to modify the default configuration, edit `.env` and fill in:
- `LLM_PROVIDER` / `VECTORSTORE_PROVIDER` - which provider is used for LLMs and the vector store. Must correspond to the selected docker-compose files and starter modules.
  - `LLM_PROVIDER` currently supports `ollama` and `openai`
  - `VECTORSTORE_PROVIDER` currently supports `elasticsearch`
- `OLLAMA_CHAT_MODEL` / `OLLAMA_EMBED_MODEL` — if using the Ollama overlay
- `OPENAI_API_KEY` — if using the OpenAI overlay
- `ANTHROPIC_API_KEY` — used for the test judge, independent of the active LLM provider
- `LLM_USERNAME` / `LLM_PASSWORD` - if required by the LLM provider
- `COMPOSE_FILE` — optionally set this to the overlay combination you want (see below), so you don't need to pass `-f` flags every time. **On Windows, separate paths with `;`; on Linux/macOS, use `:`.**

`application.yaml` should already be wired to read its values from the same environment variables — you shouldn't need to hardcode secrets there.

### Swapping LLM providers / vector stores

In adition to the values set in .env, the active LLM provider and vector store are determined by which starter modules `rag-app/pom.xml` depends on. Check (or edit) the `<dependencies>` section of `rag-app/pom.xml` to confirm which are active, e.g.:

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

Swapping providers means changing these dependencies, the .env values, and rebuilding. Then pick the overlay files matching your chosen modules, e.g.:

```bash
docker compose -f docker-compose.yml -f docker-compose.ollama.yml -f docker-compose.elasticsearch.yml up --build
```

Or, if you set `COMPOSE_FILE` in `.env`:

```bash
docker compose up --build
```

### Configuration files

| File | Purpose | Committed?                          |
|---|---|-------------------------------------|
| `.env` | Model names, API keys, active Compose overlay selection | No (see `.env_template`)             |
| `application.yaml` | Spring configuration; reads most values from the environment | No (see `application.yaml_template`) |
| `rag-app/pom.xml` | Selects which LLM provider / vector store modules are compiled in | Yes                                 |
| `docker-compose*.yml` | Infrastructure wiring per module choice | Yes                                 |

## Generating API documentation (Javadoc)

API documentation can be generated by running the following command:

```bash
docker compose --profile docs run --rm docs
```

Run the server:

```bash
docker compose --profile docs up docs-server
```

Then open **http://localhost:8081**.
