# HTTP Insecure Transport

This library provides a transport layer that integrates OpenLineage with HTTP endpoints while disabling SSL certificate verification. This is useful for development environments, testing with self-signed certificates, or when working with internal CA certificates that aren't in the trust store.

## ⚠️ Security Warning

**This transport disables SSL certificate verification and should only be used in development environments or trusted networks. Do not use this in production environments as it makes the connection vulnerable to man-in-the-middle attacks.**

## Getting Started

### Adding the Dependency

To use this transport in your project, you need to include the following dependency in your build configuration. This is particularly important for environments like `Spark`, where this transport must be on the classpath for lineage events to be emitted correctly.

**Maven:**

```xml
<dependency>
    <groupId>io.openlineage</groupId>
    <artifactId>transports-http-insecure</artifactId>
    <version>YOUR_VERSION_HERE</version>
</dependency>
```

### Configuration

- `type` - string, must be `"http_insecure"`. Required.
- `url` - string, HTTP endpoint URL. Required.
- `endpoint` - string, API endpoint path. Optional, default: "/api/v1/lineage"
- `timeoutInMillis` - integer, HTTP timeout in milliseconds. Optional, default: 5000
- `headers` - map of HTTP headers. Optional.
- `compression` - string, compression algorithm. Optional, values: "gzip"

### Behavior

- Events are serialized to JSON, and then dispatched to the HTTP endpoint.
- SSL certificate verification is **disabled**.
- Console logging is enabled to help with debugging in AWS Glue environments.
