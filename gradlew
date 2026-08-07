#!/bin/sh

set -eu

APP_HOME=$(cd "${0%/*}" >/dev/null 2>&1 && pwd -P)
GRADLE_VERSION="9.5.0"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
DIST_SHA256="553c78f50dafcd54d65b9a444649057857469edf836431389695608536d6b746"
CACHE_ROOT="${GRADLE_USER_HOME:-${HOME}/.gradle}/wrapper/dists/onestep-gradle-${GRADLE_VERSION}"
DIST_ZIP="$CACHE_ROOT/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_BIN="$CACHE_ROOT/gradle-${GRADLE_VERSION}/bin/gradle"

verify_distribution() {
    if command -v sha256sum >/dev/null 2>&1; then
        printf '%s  %s\n' "$DIST_SHA256" "$DIST_ZIP" | sha256sum --check --status
    elif command -v shasum >/dev/null 2>&1; then
        [ "$(shasum -a 256 "$DIST_ZIP" | awk '{print $1}')" = "$DIST_SHA256" ]
    else
        echo "ERROR: sha256sum or shasum is required to verify Gradle." >&2
        return 1
    fi
}

if [ ! -x "$GRADLE_BIN" ]; then
    mkdir -p "$CACHE_ROOT"
    if [ ! -f "$DIST_ZIP" ] || ! verify_distribution; then
        TMP_ZIP="$DIST_ZIP.tmp"
        rm -f "$TMP_ZIP" "$DIST_ZIP"
        if command -v curl >/dev/null 2>&1; then
            curl --fail --location --show-error "$DIST_URL" --output "$TMP_ZIP"
        elif command -v wget >/dev/null 2>&1; then
            wget "$DIST_URL" -O "$TMP_ZIP"
        else
            echo "ERROR: curl or wget is required to download Gradle." >&2
            exit 1
        fi
        mv "$TMP_ZIP" "$DIST_ZIP"
    fi

    if ! verify_distribution; then
        rm -f "$DIST_ZIP"
        echo "ERROR: Gradle distribution checksum verification failed." >&2
        exit 1
    fi

    rm -rf "$CACHE_ROOT/gradle-${GRADLE_VERSION}"
    if ! command -v unzip >/dev/null 2>&1; then
        echo "ERROR: unzip is required to extract Gradle." >&2
        exit 1
    fi
    unzip -q "$DIST_ZIP" -d "$CACHE_ROOT"
fi

exec "$GRADLE_BIN" -p "$APP_HOME" "$@"
