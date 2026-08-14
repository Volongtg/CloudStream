#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
VER=7.4.2
DIST="$HOME/.gradle/gradle-bootstrap"
ZIP="$DIST/gradle-$VER-bin.zip"
DIR="$DIST/gradle-$VER"
URL="https://services.gradle.org/distributions/gradle-$VER-bin.zip"
mkdir -p "$DIST"
if [ ! -x "$DIR/bin/gradle" ]; then
  mkdir -p "$DIR.parent"
  if [ ! -f "$ZIP" ]; then
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL --retry 3 "$URL" -o "$ZIP"
    else
      wget -q "$URL" -O "$ZIP"
    fi
  fi
  rm -rf "$DIR"
  unzip -q "$ZIP" -d "$DIST"
  mv "$DIST/gradle-$VER" "$DIR"
fi
exec "$DIR/bin/gradle" "$@"
