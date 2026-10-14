#!/usr/bin/env bash
# Builds the JAR (if Maven is installed) and creates SpaceOps.zip next to this project folder.
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PARENT_DIR="$(dirname "$PROJECT_DIR")"
NAME="$(basename "$PROJECT_DIR")"
ZIP_FILE="$PARENT_DIR/SpaceOps.zip"

cd "$PROJECT_DIR"

if command -v mvn >/dev/null 2>&1; then
    echo "Building JAR..."
    mvn -B clean package || echo "Maven build failed - ZIP will contain source only. Run 'mvn clean package' to build the JAR."
else
    echo "Maven not found - ZIP will contain source only. Run 'mvn clean package' to build the JAR."
fi

cd "$PARENT_DIR"
rm -f "$ZIP_FILE"
zip -r "$ZIP_FILE" "$NAME" -x "$NAME/target/*"

if [ -f "$NAME/target/spaceops.jar" ]; then
    zip "$ZIP_FILE" "$NAME/target/spaceops.jar"
    echo "JAR included in ZIP."
fi

echo "Created: $ZIP_FILE"
