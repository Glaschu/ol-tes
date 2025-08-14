# OpenLineage Insecure Wrapper

A drop-in replacement for `openlineage-spark` that bypasses SSL certificate validation for testing environments.

**⚠️ WARNING: This wrapper disables SSL certificate verification. Use only in development/testing environments. DO NOT use in production.**

## What This Does

- Provides a single JAR that acts as a drop-in replacement for `openlineage-spark`
- Automatically bypasses SSL certificate validation for all HTTP connections
- Uses a custom insecure transport that bypasses Apache HTTP Client's shaded SSL implementation
- Auto-registers as a SparkListener via ServiceLoader mechanism
- Includes all OpenLineage dependencies in a single shaded JAR

## Built JAR

The final JAR is: `target/openlineage-insecure-all.jar` (~23MB)

## Usage

### AWS Glue (Recommended)

Simply add the JAR and configure OpenLineage via Spark configuration:

```bash
--extra-jars s3://your-bucket/openlineage-insecure-all.jar
--conf spark.openlineage.transport.url=https://your-openlineage-endpoint/api/v1/lineage
--conf spark.openlineage.transport.headers.api-key=your-api-key
```

### Manual Spark Configuration

```bash
spark-submit \
  --jars openlineage-insecure-all.jar \
  --conf spark.openlineage.transport.url=https://your-openlineage-endpoint/api/v1/lineage \
  --conf spark.openlineage.transport.headers.api-key=your-api-key \
  your-spark-app.py
```

### Explicit Listener Configuration (Alternative)

If auto-discovery doesn't work:

```bash
--conf spark.extraListeners=io.openlineage.spark.insecure.InsecureOpenLineageUnifiedListener
```

## How It Works

1. **Static SSL Bypass**: When the `InsecureOpenLineageUnifiedListener` class loads, it immediately installs a global insecure SSL context that trusts all certificates and hostnames.

2. **Custom Transport**: Creates an `InsecureTransport` that uses `HttpURLConnection` with disabled SSL verification, completely bypassing OpenLineage's shaded Apache HTTP client.

3. **Client Injection**: Uses reflection to inject the custom insecure client into the standard `OpenLineageSparkListener`.

4. **ServiceLoader Auto-Registration**: Automatically registers as a SparkListener without requiring explicit configuration.

## Configuration

The wrapper reads the same configuration as standard OpenLineage:

- `spark.openlineage.transport.url` - The OpenLineage endpoint URL
- `spark.openlineage.transport.headers.api-key` - API key for authentication
- Other standard OpenLineage configurations

## Components

### Core Classes

- `InsecureOpenLineageUnifiedListener` - Main SparkListener that combines SSL bypass and OpenLineage event emission
- `InsecureTransport` - Custom transport using HttpURLConnection with disabled SSL verification
- `InsecureConfig` - Configuration class for the custom transport
- `EarlySSLBootstrap` - Global SSL context bypass utilities
- `InsecureOpenLineageSpark` - Utility methods for SSL bypass

### SSL Bypass Strategy

The wrapper uses multiple strategies to ensure SSL bypass:

1. **Global SSL Context**: Installs a permissive SSL context that trusts all certificates
2. **Custom Transport**: Uses `HttpURLConnection` instead of Apache HTTP client
3. **JVM Properties**: Sets system properties to disable various SSL checks
4. **Static Initialization**: Applies SSL bypass immediately when classes load

## Building

```bash
mvn clean package
```

This creates:
- `target/openlineage-insecure-wrapper-1.0-SNAPSHOT.jar` - Basic JAR without dependencies
- `target/openlineage-insecure-all.jar` - Shaded JAR with all dependencies (use this one)

## Testing

To test that SSL bypass is working:

1. Configure a self-signed or invalid SSL endpoint
2. Run your Spark job with the wrapper
3. Check logs for successful OpenLineage event emission without SSL errors

## Troubleshooting

### SSL Errors Still Occurring

If you still see SSL certificate errors:

1. Verify you're using the shaded JAR (`openlineage-insecure-all.jar`)
2. Check that the URL is correct and accessible
3. Ensure no other OpenLineage JARs are on the classpath that might override the custom transport

### Events Not Being Sent

1. Check Spark logs for `InsecureOpenLineageUnifiedListener` initialization messages
2. Verify the `spark.openlineage.transport.url` configuration is set
3. Enable debug logging: `--conf spark.sql.adaptive.enabled=false --conf spark.serializer=org.apache.spark.serializer.KryoSerializer`

### Version Conflicts

If you encounter dependency conflicts:

1. Ensure no other OpenLineage JARs are on the classpath
2. Use `--conf spark.sql.extensions=` to disable any SQL extensions that might conflict
3. Check Maven dependency tree for conflicts: `mvn dependency:tree`

## Security Notice

This wrapper is designed for development and testing environments where SSL certificate validation needs to be bypassed. It:

- Disables ALL SSL certificate validation globally
- Trusts all certificates and hostnames
- Should NEVER be used in production environments
- May affect other SSL connections in the same JVM

## License

This project is for testing purposes only. Use at your own risk.
