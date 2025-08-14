# OpenLineage Insecure Wrapper

This project provides a wrapper JAR for OpenLineage that disables SSL certificate validation for the HTTP transport. This is **only intended for testing environments** where self-signed certificates are used and proper certificate management is not feasible.

## Security Warning

**WARNING**: This wrapper disables SSL certificate validation, which makes your connections vulnerable to man-in-the-middle attacks. Never use this in production environments!

## How it works

The wrapper overrides the standard `HttpTransportFactory` and `HttpTransport` classes from OpenLineage by:

1. Implementing a custom `InsecureTrustManager` that accepts all certificates without validation
2. Creating a custom `InsecureHttpTransport` that uses this trust manager
3. Overriding the `HttpTransportFactory` to create instances of our insecure transport
4. Using Java's service provider mechanism to replace the original factory

## Building

Build the JAR using Maven:

```bash
mvn clean package
```

This will create a JAR file in the `target` directory.

## Usage

### In AWS Glue

Upload the JAR to S3 and add it to your Glue job:

```
--extra-jars s3://your-bucket/path/to/openlineage-insecure-wrapper-1.0-SNAPSHOT.jar
```

Make sure this JAR appears before any other OpenLineage JARs in the classpath.

### In Spark

Add the JAR to your Spark submit command:

```bash
spark-submit --jars openlineage-insecure-wrapper-1.0-SNAPSHOT.jar ...
```

### Direct Usage in Code

You can also use the insecure transport directly in your code:

```java
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.InsecureHttpTransport;

// Create a client with insecure transport
OpenLineageClient client = OpenLineageClient.builder()
    .transport(InsecureHttpTransport.forUri("https://your-server:port"))
    .build();
```

## License

This project is licensed under the Apache License 2.0.
