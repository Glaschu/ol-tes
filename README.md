# OpenLineage HTTP Insecure Transport

A custom transport implementation for OpenLineage that allows HTTP connections with disabled SSL certificate verification. This is useful for development environments and testing scenarios.

## ⚠️ Security Warning

**This transport disables SSL certificate verification and should ONLY be used in development environments or trusted networks. Never use this in production as it makes connections vulnerable to man-in-the-middle attacks.**

## Features

- HTTP transport with disabled SSL certificate verification
- Support for GZIP compression
- Custom headers support
- Detailed console logging for debugging (especially useful in AWS Glue)
- Compatible with OpenLineage client framework

## Usage

### Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.openlineage</groupId>
    <artifactId>transports-http-insecure</artifactId>
    <version>1.21.0</version>
</dependency>
```

### Gradle Dependency

Add to your `build.gradle`:

```gradle
implementation 'io.openlineage:transports-http-insecure:1.21.0'
```

### Configuration

#### YAML Configuration

```yaml
transport:
  type: http_insecure
  url: https://your-lineage-endpoint.com
  endpoint: /api/v1/lineage  # optional, default is /api/v1/lineage
  timeoutInMillis: 10000     # optional, default is 5000
  compression: gzip          # optional
  headers:                   # optional
    Authorization: Bearer your-token
    X-Custom-Header: value
```

#### Spark Configuration

```properties
spark.openlineage.transport.type=http_insecure
spark.openlineage.transport.url=https://your-lineage-endpoint.com
spark.openlineage.transport.timeoutInMillis=10000
spark.openlineage.transport.compression=gzip
spark.openlineage.transport.headers.Authorization=Bearer your-token
```

#### Java Code Configuration

```java
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.httpinsecure.HttpInsecureConfig;
import io.openlineage.client.transports.httpinsecure.HttpInsecureTransport;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

// Create configuration
HttpInsecureConfig config = new HttpInsecureConfig();
config.setUrl(URI.create("https://your-lineage-endpoint.com"));
config.setTimeoutInMillis(10000);
config.setCompression(HttpInsecureConfig.Compression.GZIP);

// Optional: Add custom headers
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer your-token");
headers.put("X-Custom-Header", "value");
config.setHeaders(headers);

// Create transport and client
HttpInsecureTransport transport = new HttpInsecureTransport(config);
OpenLineageClient client = new OpenLineageClient(transport);

// Use the client to emit events
// client.emit(runEvent);
```

## AWS Glue Usage

This transport is particularly useful for AWS Glue jobs where you need to send lineage data to endpoints with self-signed certificates or internal CAs.

### Example AWS Glue Job Configuration

```python
# In your Glue job parameters or script
import sys
from awsglue.utils import getResolvedOptions

# Add the JAR to your Glue job's extra JARs
# s3://your-bucket/jars/transports-http-insecure-1.21.0.jar

# Configure via system properties or spark configuration
spark.conf.set("spark.openlineage.transport.type", "http_insecure")
spark.conf.set("spark.openlineage.transport.url", "https://your-internal-lineage-server.com")
```

## Console Logging

The transport includes extensive console logging to help with debugging, especially in environments like AWS Glue where standard logging might be limited:

```
=== HttpInsecureTransport initialized ===
Target URI: https://your-endpoint.com/api/v1/lineage
Headers: {Authorization=Bearer token}
Compression: GZIP
SSL Certificate Verification: DISABLED
===============================================

=== HttpInsecureTransport SSL Context ===
SSL Context: TLS
Hostname Verification: DISABLED
Trust All Certificates: ENABLED
=========================================

=== HttpInsecureTransport.emit() called ===
Event JSON length: 1234
Target URI: https://your-endpoint.com/api/v1/lineage
Sending HTTP POST request...
HTTP Response Code: 200
HTTP Response Reason: OK
Event successfully sent!
=========================================
```

## Building from Source

```bash
cd client/java
./gradlew build
```

## Testing

```bash
cd client/java
./gradlew test
```

## Security Considerations

1. **Development Only**: This transport should only be used in development environments
2. **Network Security**: Ensure you're using this only in trusted networks
3. **Alternative Solutions**: Consider proper certificate management instead of disabling verification
4. **Monitoring**: Monitor network traffic when using this transport

## License

Apache License 2.0 - see LICENSE file for details.
