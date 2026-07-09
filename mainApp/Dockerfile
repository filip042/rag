# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /app

# Copy parent POM + every module's POM first (not source yet).
# This lets Docker cache the dependency-resolution layer separately from
# your actual source changes -- as long as no pom.xml changes, this whole
# block is a cache hit on rebuild.
COPY pom.xml .
COPY rag-core/pom.xml rag-core/
COPY rag-starter-provider-ollama/pom.xml rag-starter-provider-ollama/
COPY rag-starter-llm-deepseek-ollama/pom.xml rag-starter-llm-deepseek-ollama/
COPY rag-starter-llm-qwen3-ollama/pom.xml rag-starter-llm-qwen3-ollama/
COPY rag-starter-provider-openai/pom.xml rag-starter-provider-openai/
COPY rag-starter-llm-gpt4o-openai/pom.xml rag-starter-llm-gpt4o-openai/
COPY rag-starter-vectorstore-elasticsearch/pom.xml rag-starter-vectorstore-elasticsearch/
COPY rag-starter-parser-tika/pom.xml rag-starter-parser-tika/
COPY rag-starter-parser-markdown/pom.xml rag-starter-parser-markdown/
COPY rag-app/pom.xml rag-app/
COPY rag-integration-tests/pom.xml rag-integration-tests/

RUN mvn dependency:go-offline -B

# Now copy actual sources for every module.
COPY rag-core/src rag-core/src
COPY rag-starter-provider-ollama/src rag-starter-provider-ollama/src
COPY rag-starter-llm-deepseek-ollama/src rag-starter-llm-deepseek-ollama/src
COPY rag-starter-llm-qwen3-ollama/src rag-starter-llm-qwen3-ollama/src
COPY rag-starter-provider-openai/src rag-starter-provider-openai/src
COPY rag-starter-llm-gpt4o-openai/src rag-starter-llm-gpt4o-openai/src
COPY rag-starter-vectorstore-elasticsearch/src rag-starter-vectorstore-elasticsearch/src
COPY rag-starter-parser-tika/src rag-starter-parser-tika/src
COPY rag-starter-parser-markdown/src rag-starter-parser-markdown/src
COPY rag-app/src rag-app/src
COPY rag-integration-tests/src rag-integration-tests/src

# Build only rag-app and whatever modules it depends on (-am = "also make").
# Skips rag-integration-tests entirely since nothing depends on it.
# -Pdocker activates repackaging (spring-boot-maven-plugin is skipped by
# default in rag-app/pom.xml, presumably for local/test builds).
RUN mvn package -pl rag-app -am -DskipTests -B -Pdocker

# ---- Runtime stage ----
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=build /app/rag-app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]