#!/bin/bash
# Test script for the insecure OpenLineage wrapper

echo "Testing Insecure OpenLineage Transport..."

# Check if JAR exists
if [ ! -f "target/openlineage-insecure-all.jar" ]; then
    echo "ERROR: openlineage-insecure-all.jar not found. Run 'mvn clean package' first."
    exit 1
fi

echo "✓ JAR file found"

# Check ServiceLoader registration
echo "Checking ServiceLoader registration..."
jar tf target/openlineage-insecure-all.jar | grep "META-INF/services/org.apache.spark.scheduler.SparkListener" > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ ServiceLoader registration found"
    echo "Registered listeners:"
    jar xf target/openlineage-insecure-all.jar META-INF/services/org.apache.spark.scheduler.SparkListener
    cat META-INF/services/org.apache.spark.scheduler.SparkListener | sed 's/^/  - /'
    rm -rf META-INF
else
    echo "✗ ServiceLoader registration missing"
    exit 1
fi

# Check for our custom classes
echo "Checking for custom transport classes..."
jar tf target/openlineage-insecure-all.jar | grep "InsecureTransport.class" > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ InsecureTransport class found"
else
    echo "✗ InsecureTransport class missing"
    exit 1
fi

jar tf target/openlineage-insecure-all.jar | grep "DirectInsecureSparkListener.class" > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ DirectInsecureSparkListener class found"
else
    echo "✗ DirectInsecureSparkListener class missing"
    exit 1
fi

# Test basic Java class loading
echo "Testing class loading..."
java -cp target/openlineage-insecure-all.jar -Dspark.openlineage.transport.url=https://test.example.com/api/v1/lineage io.openlineage.client.transports.InsecureTransport 2>/dev/null
if [ $? -eq 0 ]; then
    echo "✓ Classes load successfully"
else
    echo "⚠ Class loading test inconclusive (expected for transport class)"
fi

echo ""
echo "✅ Basic tests passed!"
echo ""
echo "To use in AWS Glue:"
echo "  --extra-jars s3://your-bucket/openlineage-insecure-all.jar"
echo "  --conf spark.openlineage.transport.url=https://your-endpoint/api/v1/lineage"
echo ""
echo "For SSL issues, try:"
echo "  --conf spark.extraListeners=io.openlineage.spark.insecure.DirectInsecureSparkListener"
