#!/bin/bash
cd "$(dirname "$0")"

# On macOS, prefer Liberica JDK 21 (required for JavaFX)
if [ -f "/Library/Java/JavaVirtualMachines/liberica-jdk-21.jdk/Contents/Home/bin/java" ]; then
    export JAVA_HOME="/Library/Java/JavaVirtualMachines/liberica-jdk-21.jdk/Contents/Home"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# Try system Maven first, then bundled tools/ Maven
if command -v mvn &>/dev/null; then
    mvn javafx:run
elif [ -f "tools/apache-maven-3.9.9/bin/mvn" ]; then
    ./tools/apache-maven-3.9.9/bin/mvn javafx:run
else
    echo "ERROR: Maven not found."
    echo "Install Maven from https://maven.apache.org or add it to PATH."
    exit 1
fi
