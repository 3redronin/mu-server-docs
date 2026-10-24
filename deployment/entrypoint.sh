#!/bin/sh
set -eu
umask 077
exec java -XX:MaxRAMPercentage=65 -Dlogback.configurationFile=/app/logback-container.xml -jar /app/docs.jar "$@"
