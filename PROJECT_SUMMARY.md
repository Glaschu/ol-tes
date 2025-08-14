# OpenLineage HTTP Insecure Transport - Project Summary

## 🎯 Project Overview

This project provides a custom OpenLineage transport implementation that allows HTTP connections with **disabled SSL certificate verification**. It's specifically designed for development environments, testing scenarios, and situations where you need to connect to endpoints with self-signed certificates or internal CA certificates.

## ⚠️ Security Notice

**This transport disables SSL certificate verification and should ONLY be used in development environments or trusted networks. Never use this in production as it makes connections vulnerable to man-in-the-middle attacks.**

## 📋 What Was Created

### ✅ Core Transport Implementation
- **HttpInsecureConfig.java** - Configuration class with URL, timeout, headers, compression settings
- **HttpInsecureTransport.java** - Main transport with disabled SSL verification and extensive logging
- **HttpInsecureTransportBuilder.java** - Builder pattern implementation for service discovery
- Service registration for automatic discovery by OpenLineage framework

### ✅ Key Features Implemented
1. **SSL Certificate Verification Disabled** - Uses custom TrustManager that accepts all certificates
2. **Hostname Verification Disabled** - Uses NoopHostnameVerifier 
3. **Extensive Console Logging** - Perfect for debugging in AWS Glue environments
4. **GZIP Compression Support** - Optional compression for request bodies
5. **Custom Headers Support** - For authentication and custom headers
6. **Configurable Timeouts** - Adjustable connection and read timeouts

### ✅ Testing & Examples
- Comprehensive unit tests with Mockito
- Example usage class demonstrating configuration
- Test data helpers for OpenLineage events

### ✅ Documentation
- **README.md** - Complete usage guide with examples
- **AWS_GLUE_USAGE.md** - Specific AWS Glue integration guide
- **Transport README** - Module-specific documentation
- Build and deployment instructions

### ✅ Build System
- Gradle build configuration following OpenLineage patterns
- Build script for easy compilation and JAR creation
- Proper dependency management

## 🔧 Technical Implementation

### Architecture
```
HttpInsecureTransport
├── HttpInsecureConfig (configuration)
├── HttpInsecureTransportBuilder (service discovery)
├── Custom SSL Context (trust all certificates)
├── Apache HttpClient 5 (HTTP implementation)
└── Console Logging (debugging support)
```

### SSL Security Bypass
```java
// Custom TrustManager that accepts all certificates
TrustManager[] trustAllCerts = new TrustManager[] {
  new X509TrustManager() {
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
      // Trust all server certificates
    }
  }
};

// Disabled hostname verification
DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(
    sslContext, NoopHostnameVerifier.INSTANCE);
```

### Console Logging Examples
The transport provides detailed logging for debugging:

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
```

## 🚀 Usage Examples

### Basic Configuration
```java
HttpInsecureConfig config = new HttpInsecureConfig();
config.setUrl(URI.create("https://your-endpoint.com"));
config.setTimeoutInMillis(10000);
config.setCompression(HttpInsecureConfig.Compression.GZIP);

HttpInsecureTransport transport = new HttpInsecureTransport(config);
OpenLineageClient client = new OpenLineageClient(transport);
```

### Spark Configuration
```properties
spark.openlineage.transport.type=http_insecure
spark.openlineage.transport.url=https://your-endpoint.com
spark.openlineage.transport.timeoutInMillis=10000
spark.openlineage.transport.compression=gzip
```

### AWS Glue Integration
1. Upload JAR to S3: `s3://bucket/jars/transports-http-insecure-1.21.0.jar`
2. Add JAR to Glue job dependencies
3. Configure via Spark properties or job parameters

## 📁 Project Structure

```
ol-tes/
├── README.md                          # Main documentation
├── AWS_GLUE_USAGE.md                  # AWS Glue specific guide
├── build.sh                           # Build script
└── client/java/
    ├── build.gradle                   # Root build configuration
    ├── settings.gradle                # Module configuration
    ├── gradlew                        # Gradle wrapper
    ├── transports.build.gradle        # Shared transport config
    ├── openlineage-java/              # Core OpenLineage classes
    │   ├── build.gradle
    │   └── src/main/java/io/openlineage/client/
    │       ├── OpenLineage.java
    │       ├── OpenLineageClient.java
    │       ├── OpenLineageClientException.java
    │       ├── OpenLineageClientUtils.java
    │       ├── MergeConfig.java
    │       └── transports/
    │           ├── Transport.java
    │           ├── TransportBuilder.java
    │           ├── TransportConfig.java
    │           └── TransportFactory.java
    └── transports-http-insecure/       # HTTP Insecure Transport
        ├── build.gradle
        ├── README.md
        └── src/
            ├── main/java/io/openlineage/client/transports/httpinsecure/
            │   ├── HttpInsecureConfig.java
            │   ├── HttpInsecureTransport.java
            │   ├── HttpInsecureTransportBuilder.java
            │   └── HttpInsecureTransportExample.java
            ├── main/resources/META-INF/services/
            │   └── io.openlineage.client.transports.TransportBuilder
            └── test/java/
                ├── io/openlineage/client/testdata/
                │   └── OpenLineageEventsDataHelper.java
                └── io/openlineage/client/transports/httpinsecure/
                    └── HttpInsecureTransportTest.java
```

## 🏗️ Build and Deploy

### Building
```bash
./build.sh
```

This creates: `client/java/build/libs/transports-http-insecure-1.21.0.jar`

### Deployment
1. Copy JAR to your project's classpath
2. For AWS Glue: Upload to S3 and add to job dependencies
3. Configure transport type as `http_insecure`

## 🔍 Key Differences from Standard HTTP Transport

| Feature | Standard HTTP | HTTP Insecure |
|---------|---------------|---------------|
| SSL Verification | ✅ Enabled | ❌ **Disabled** |
| Hostname Verification | ✅ Enabled | ❌ **Disabled** |
| Self-signed Certs | ❌ Rejected | ✅ **Accepted** |
| Console Logging | ⚪ Minimal | ✅ **Extensive** |
| Security Level | 🔒 High | ⚠️ **Low** |
| Use Case | Production | **Dev/Test Only** |

## 🎯 Perfect For

- **AWS Glue Development** - Testing with internal endpoints
- **Local Development** - Self-signed certificate environments  
- **Testing Environments** - Quick setup without certificate management
- **Internal Networks** - Trusted network environments
- **Debugging** - Extensive logging helps troubleshoot issues

## ⚡ Next Steps

1. **Test the Implementation**: Use the provided examples to test in your environment
2. **AWS Glue Integration**: Follow the AWS_GLUE_USAGE.md guide for Glue-specific setup
3. **Security Review**: Ensure you're only using this in appropriate environments
4. **Migration Planning**: Plan to move to secure transport for production use

## 📞 Support

This implementation follows the OpenLineage transport pattern and should be compatible with any OpenLineage client that supports pluggable transports. The extensive logging helps with troubleshooting, especially in AWS Glue environments where debugging can be challenging.

Remember: **Security first!** Only use this transport in development and testing environments where the security implications are acceptable.
