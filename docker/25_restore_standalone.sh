#!/bin/bash

echo "Parando contenedor mongo (replica)..."
docker compose -f compose.replica.yaml stop mongo
docker compose -f compose.replica.yaml rm -f mongo

echo "Arrancando mongo en modo standalone (compose.yaml original)..."
docker compose up -d

echo "Mongo restaurado a standalone."
