#!/bin/sh
# Gradle wrapper script
export JAVA_HOME="${JAVA_HOME:-/home/user/jdks/jdk-21.0.2}"
GRADLE_BIN="/home/user/.gradle/wrapper/dists/gradle-9.1.0-bin/a7zz1zpvyl3jaouarz82m4yky/gradle-9.1.0/bin/gradle"
exec "$GRADLE_BIN" --no-daemon -p "$(dirname "$0")" "$@"
