#!/bin/bash

attempts=0
while [ $attempts -lt 30 ]; do
    if curl -s http://lila:9663 >/dev/null; then
        break
    fi
    echo "⌛ Waiting for lila to start..."
    sleep 1
    attempts=$((attempts + 1))
done

java \
    --add-exports chariot/chariot.internal.yayson=testchariot \
    --add-exports chariot/chariot.internal=testchariot \
    -p out/modules \
    -m testchariot it
