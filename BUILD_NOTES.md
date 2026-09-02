# How the shipped jar was actually built

The jar you were given was **not** produced by Gradle. It was compiled directly with
`javac` against the real Minecraft 26.2 / Fabric Loader 0.19.3 / Fabric API 0.158.0
classes, then packaged and boot-tested on a real 26.2 Fabric dedicated server.

`build.sh` reproduces exactly that. `build.gradle` is provided for future editing in a
normal Loom workspace - the Loom plugin version there is the one thing that was not
verifiable offline, so bump it to whatever release supports 26.2 in your setup.

The mod targets **Java 21 bytecode** deliberately. It runs fine on the Java 25 the
server uses, and staying at 21 keeps the class files readable by more tooling.

Classpath needed by `build.sh` (all of these are already in a normal .minecraft install):

* the Minecraft 26.2 jar
* fabric-loader 0.19.3, sponge-mixin, asm
* fabric-api 0.158.0+26.2 (its nested jars extracted)
* gson, guava, authlib, brigadier, datafixerupper, netty, fastutil, slf4j, joml, jspecify
