#!/usr/bin/env sh
set -eu

VER="7.4.2"
DIST="$HOME/.gradle/gradle-bootstrap"
ZIP="$DIST/gradle-$VER-bin.zip"
ROOT="$DIST"
DIR="$ROOT/gradle-$VER"
URL="https://services.gradle.org/distributions/gradle-$VER-bin.zip"

mkdir -p "$DIST"

if [ ! -x "$DIR/bin/gradle" ]; then
    rm -f "$ZIP"
    echo "Downloading Gradle $VER..."
    if command -v curl >/dev/null 2>&1; then
        curl -fL --retry 3 "$URL" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$URL" -O "$ZIP"
    else
        echo "curl or wget is required" >&2
        exit 1
    fi

    rm -rf "$DIR"
    unzip -q "$ZIP" -d "$ROOT"
fi

exec "$DIR/bin/gradle" "$@"
