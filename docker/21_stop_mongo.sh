#!/bin/bash

echo "Parando contenedor mongo..."
docker compose stop mongo
docker compose rm -f mongo
echo "Contenedor parado y eliminado."
