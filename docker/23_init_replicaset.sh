#!/bin/bash

echo "Inicializando Replica Set rs0..."
docker exec -i mongo mongosh --eval "rs.initiate()"
