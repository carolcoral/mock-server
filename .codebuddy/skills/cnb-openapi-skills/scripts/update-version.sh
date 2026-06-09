#!/usr/bin/env bash
set -euo pipefail

VERSION_URL="https://cnb.cool/api/version"
README="README.md"
PACKAGE_JSON="package.json"

VERSION=$(curl -s "$VERSION_URL" | grep -o '"version":"[^"]*"' | head -1 | cut -d'"' -f4 | cut -d'-' -f1)

if [ -z "$VERSION" ]; then
  echo "Warning: Failed to fetch version from $VERSION_URL"
  exit 1
fi

sed -i '' "s/version-[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*/version-$VERSION/g" "$README"
echo "Updated $README version to $VERSION"

sed -i '' "s/\"version\": *\"[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\"/\"version\": \"$VERSION\"/g" "$PACKAGE_JSON"
sed -i '' "s/\"cnb-openapi-skills\": *\"\^[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\"/\"cnb-openapi-skills\": \"^$VERSION\"/g" "$PACKAGE_JSON"
echo "Updated $PACKAGE_JSON version to $VERSION"
