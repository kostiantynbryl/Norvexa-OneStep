#!/bin/sh

set -eu

APP_HOME=$(cd "${0%/*}" >/dev/null 2>&1 && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-9.5.0-wrapper.jar"
WRAPPER_SHA256="497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7"

verify_wrapper() {
    if command -v sha256sum >/dev/null 2>&1; then
        printf '%s  %s\n' "$WRAPPER_SHA256" "$WRAPPER_JAR" | sha256sum --check --status
    elif command -v shasum >/dev/null 2>&1; then
        [ "$(shasum -a 256 "$WRAPPER_JAR" | awk '{print $1}')" = "$WRAPPER_SHA256" ]
    else
        echo "ERROR: sha256sum or shasum is required to verify Gradle Wrapper." >&2
        return 1
    fi
}

if [ ! -f "$WRAPPER_JAR" ] || ! verify_wrapper; then
    mkdir -p "$(dirname "$WRAPPER_JAR")"
    TMP_JAR="$WRAPPER_JAR.tmp"
    rm -f "$TMP_JAR"
    if command -v curl >/dev/null 2>&1; then
        curl --fail --location --silent --show-error "$WRAPPER_URL" --output "$TMP_JAR"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$WRAPPER_URL" -O "$TMP_JAR"
    else
        echo "ERROR: curl or wget is required to download Gradle Wrapper." >&2
        exit 1
    fi
    mv "$TMP_JAR" "$WRAPPER_JAR"
    if ! verify_wrapper; then
        rm -f "$WRAPPER_JAR"
        echo "ERROR: Gradle Wrapper checksum verification failed." >&2
        exit 1
    fi
fi

if [ -n "${JAVA_HOME:-}" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi

exec "$JAVACMD" -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
