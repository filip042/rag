#!/bin/sh
# Starts the full stack (default Ollama + Elasticsearch setup) and reports
# in the console when the application is ready to use.
cd "$(dirname "$0")" || exit 1

docker compose -f docker-compose.yml -f docker-compose.ollama.yml -f docker-compose.elasticsearch.yml up -d --build --wait "$@"
status=$?

if [ $status -ne 0 ]; then
    echo ""
    echo "Startup FAILED. To diagnose, run:"
    echo "  docker compose ps          # which service is not healthy"
    echo "  docker compose logs app    # why (replace 'app' with the failing service)"
    exit $status
fi

echo ""
echo "=========================================================="
echo "  Application is ready -- open http://localhost:8080"
echo "  Stop the stack with: docker compose down"
echo "=========================================================="
