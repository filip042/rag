@echo off
rem Starts the full stack (default Ollama + Elasticsearch setup) and reports
rem in the console when the application is ready to use.
cd /d "%~dp0"

docker compose -f docker-compose.yml -f docker-compose.ollama.yml -f docker-compose.elasticsearch.yml up -d --build --wait %*

if errorlevel 1 (
    echo.
    echo Startup FAILED. To diagnose, run:
    echo   docker compose ps          ^(which service is not healthy^)
    echo   docker compose logs app    ^(why; replace 'app' with the failing service^)
    exit /b 1
)

echo.
echo ==========================================================
echo   Application is ready -- open http://localhost:8080/user/
echo   Stop the stack with: docker compose down
echo ==========================================================
