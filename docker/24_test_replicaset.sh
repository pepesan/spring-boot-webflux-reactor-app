#!/bin/bash

echo "=== Estado del Replica Set ==="
docker exec -i mongo mongosh --eval "rs.status().ok" --quiet

echo ""
echo "=== Probando Change Stream: esperando evento (10s timeout) ==="
echo "Abre otra terminal y ejecuta:"
echo "  curl -s -N -H 'Accept: text/event-stream' http://localhost:8080/api/changestream/notificaciones/stream"
echo ""
echo "Insertando notificación de prueba en 3 segundos..."
sleep 3

docker exec -i mongo mongosh webflux --eval '
  db.notificaciones.insertOne({
    titulo: "Test Change Stream",
    mensaje: "Si ves esto en el SSE, funciona.",
    fecha: new Date()
  })
' --quiet

echo ""
echo "Notificación insertada. Deberías haber recibido el evento SSE en la otra terminal."
