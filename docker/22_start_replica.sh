#!/bin/bash

echo "Arrancando mongo con Replica Set (compose.replica.yaml)..."
docker compose -f compose.replica.yaml up -d
echo "Esperando a que mongo esté listo..."
sleep 5
echo "Mongo arrancado."
