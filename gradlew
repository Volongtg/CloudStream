#!/usr/bin/env sh
set -eu
VER="7.4.2"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/gradle-bootstrap"
ZIP="$BASE/gradle-$VER-bin.zip"
DIST="$BASE/gradle-$VER"
URL="https://services.gradle.org/distributions/gradle-$VER-bin.zip"

mkdir -p "$BASE"
if [ ! -x "$DIST/bin/gradle" ]; then
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Gradle $VER..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL --retry 3 "$URL" -o "$ZIP"
    else
      wget -q "$URL" -O "$ZIP"
    fi
  fi
  rm -rf "$DIST"
  unzip -q "$ZIP" -d "$BASE"
fi

exec "$DIST/bin/gradle" "$@"
