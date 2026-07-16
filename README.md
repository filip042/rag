# RAG Framework

A modular Retrieval-Augmented Generation (RAG) application built with Spring Boot and Spring AI. LLM providers, models, and vector stores are swappable via Maven modules and configuration, without changing application code.

The application itself lives in [`mainApp/`](./mainApp), a multi-module Maven project. This README covers building, configuring, and running it with Docker.

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

Create your configuration files from the templates. Neither `.env` nor `application.yaml` is committed to the repository, since both can contain environment-specific values and credentials. The default values work as-is, so no editing is required:

```bash
cp .env_template .env
cp rag-app/src/main/resources/application.yaml_template rag-app/src/main/resources/application.yaml
```

Start the stack (default Ollama + Elasticsearch setup):

```bash
docker compose -f docker-compose.yml -f docker-compose.ollama.yml -f docker-compose.elasticsearch.yml up --build
```

The first run on the default configuration can take up to an hour depending on internet speed, due to requiring the download and setup of Ollama and Elasticsearch. The application is ready once these lines appear in the logs:

```
app-1            | 2026-07-16T11:36:45.670Z  INFO 1 --- [mainApp] [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
app-1            | 2026-07-16T11:36:45.705Z  INFO 1 --- [mainApp] [           main] c.c.m.hanaf.mainapp.MainAppApplication   : Started MainAppApplication in 21.684 seconds (process running for 26.052)
```

## Try it out

When the log shows the app has started, open **http://localhost:8080** in a browser.

Log in with the seeded account: username `user`, password `user`.

Open the pre-loaded **Book Club** project, which contains indexed Wikipedia articles about classic fantasy literature. Ask in the chat:

> Who wrote The Hobbit?

You should get an answer naming **J. R. R. Tolkien**. If instead you get "There isn't enough information to answer that question", the Elasticsearch index is empty. Check `docker logs mainapp-es-init-1` for seeding errors.

## Configuration reference

If you want to modify the default configuration, edit `.env` and fill in:
- `LLM_PROVIDER` / `VECTORSTORE_PROVIDER` - which provider is used for LLMs and the vector store. Must correspond to the selected docker-compose files and starter modules.
  - `LLM_PROVIDER` currently supports `ollama` and `openai`
  - `VECTORSTORE_PROVIDER` currently supports `elasticsearch`
- `LLM_CHAT_MODEL` / `LLM_EMBED_MODEL` — Which LLM is used
  - `LLM_CHAT_MODEL` currently supports deepseek-r1 and qwen3 family models for Ollama, and GPT-4o models for OpenAI
  - `LLM_EMBED_MODEL` currently supports any embedding model supported by the implemented providers, provided the correct values are set in `application.yaml`
- `OPENAI_API_KEY` — if using the OpenAI overlay
- `ANTHROPIC_API_KEY` — used for the test judge, independent of the active LLM provider
- `LLM_USERNAME` / `LLM_PASSWORD` - if required by the LLM provider
- `COMPOSE_FILE` — optionally set this to the overlay combination you want (see below), so you don't need to pass `-f` flags every time. **On Windows, separate paths with `;`; on Linux/macOS, use `:`.**

`application.yaml` is wired to read these values from the `.env` file.

### Swapping LLM providers / vector stores

In addition to the values set in .env, the LLM provider and vector store are determined by which starter modules `rag-app/pom.xml` depends on. The selected models can be checked in the `<dependencies>` section of `rag-app/pom.xml`, e.g.:

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

Swapping providers means changing these dependencies, the .env values, and rebuilding. Then pick the overlay files matching the chosen modules, e.g.:

```bash
docker compose -f docker-compose.yml -f docker-compose.ollama.yml -f docker-compose.elasticsearch.yml up --build
```

Or, if `COMPOSE_FILE` is set in `.env`:

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

The generated documentation can then be viewed on **http://localhost:8081**.
