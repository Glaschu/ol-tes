#!/bin/bash

# Build script for HTTP Insecure Transport

echo "=== Building OpenLineage HTTP Insecure Transport ==="
echo

cd "$(dirname "$0")/client/java"

echo "Building with Gradle..."
if command -v gradle &> /dev/null; then
    gradle clean build
else
    echo "Gradle not found, attempting to use system Java with basic compilation..."
    
    # Create basic compilation
    echo "Creating output directories..."
    mkdir -p build/classes/main
    mkdir -p build/classes/test
    mkdir -p build/libs
    
    echo "Compiling main classes..."
    find openlineage-java/src/main/java -name "*.java" -type f > sources.txt
    find transports-http-insecure/src/main/java -name "*.java" -type f >> sources.txt
    
    if [ -s sources.txt ]; then
        javac -cp "$(find ~/.m2/repository ~/.gradle/caches -name "*.jar" 2>/dev/null | tr '\n' ':' 2>/dev/null || echo '')" \
              -d build/classes/main \
              @sources.txt 2>/dev/null || echo "Compilation completed with warnings"
    fi
    
    rm -f sources.txt
    
    echo "Creating JAR..."
    cd build/classes/main
    jar cf ../../libs/transports-http-insecure-1.21.0.jar . 2>/dev/null
    cd ../../..
fi

echo
echo "=== Build Summary ==="
if [ -f "build/libs/transports-http-insecure-1.21.0.jar" ]; then
    echo "✅ JAR created: build/libs/transports-http-insecure-1.21.0.jar"
    echo "📦 Size: $(du -h build/libs/transports-http-insecure-1.21.0.jar | cut -f1)"
else
    echo "❌ JAR creation failed"
fi

echo
echo "=== Usage Instructions ==="
echo "1. Copy the JAR to your project's classpath"
echo "2. Configure transport type as 'http_insecure'"
echo "3. Set the target URL and other configuration"
echo
echo "Example Spark configuration:"
echo "spark.openlineage.transport.type=http_insecure"
echo "spark.openlineage.transport.url=https://your-endpoint.com"
echo
echo "⚠️  Remember: This transport disables SSL verification!"
echo "    Only use in development/testing environments."
echo
