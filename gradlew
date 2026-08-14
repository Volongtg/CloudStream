#!/usr/bin/env sh
set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION="7.4.2"
DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
DIST_BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists"
DIST_DIR="${DIST_BASE}/gradle-${GRADLE_VERSION}-bin"
ZIP_PATH="${DIST_DIR}/gradle-${GRADLE_VERSION}-bin.zip"
INSTALL_DIR="${DIST_DIR}/gradle-${GRADLE_VERSION}"

mkdir -p "$DIST_DIR"

if [ ! -x "$INSTALL_DIR/bin/gradle" ]; then
    if [ ! -f "$ZIP_PATH" ]; then
        echo "Downloading Gradle ${GRADLE_VERSION}..."
        if command -v curl >/dev/null 2>&1; then
            curl -fL --retry 3 "$DIST_URL" -o "$ZIP_PATH"
        elif command -v wget >/dev/null 2>&1; then
            wget -O "$ZIP_PATH" "$DIST_URL"
        else
            echo "curl or wget is required." >&2
            exit 1
        fi
    fi

    rm -rf "$INSTALL_DIR"
    if command -v unzip >/dev/null 2>&1; then
        unzip -q "$ZIP_PATH" -d "$DIST_DIR"
    else
        echo "unzip is required." >&2
        exit 1
    fi
fi

exec "$INSTALL_DIR/bin/gradle" "$@"
