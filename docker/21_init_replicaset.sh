#!/bin/bash

docker exec -i mongo mongosh --eval "rs.initiate()"
