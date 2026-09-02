#!/usr/bin/env bash
# Offline build: point CP at a directory of Minecraft 26.2 + Fabric jars.
set -euo pipefail
CP_DIR="${1:?usage: build.sh <dir-with-mc-and-fabric-jars>}"
OUT=build/classes
CP="$(ls "$CP_DIR"/*.jar | tr '\n' ':')"

rm -rf "$OUT" build/stage build/libs
mkdir -p "$OUT" build/stage build/libs

find src/main/java -name '*.java' > build/sources.txt
javac -nowarn -proc:none -encoding UTF-8 --release 21 -cp "$CP" -d "$OUT" @build/sources.txt

cp -r "$OUT"/* build/stage/
cp src/main/resources/* build/stage/
(cd build/stage && zip -q -r -X ../libs/protected-leads-1.1.0+mc26.2.jar .)
echo "built build/libs/protected-leads-1.1.0+mc26.2.jar"
