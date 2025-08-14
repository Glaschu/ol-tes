# OpenLineage Spark - Insecure Wrapper

This project provides a wrapper JAR for OpenLineage Spark integration that disables SSL certificate validation for the HTTP transport. This is **only intended for testing environments** where self-signed certificates are used and proper certificate management is not feasible.

## Security Warning

**WARNING**: This wrapper disables SSL certificate validation, which makes your connections vulnerable to man-in-the-middle attacks. Never use this in production environments!

## How it works

This wrapper provides three ways to disable SSL certificate validation in OpenLineage Spark:

1. **Service Provider Override**: Overrides the standard `HttpTransportFactory` to create insecure transports
2. **Custom Spark Listener**: Provides an `InsecureOpenLineageSparkListener` that uses the insecure transport
3. **Direct API Usage**: Allows you to create insecure clients and transports directly in your code

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

This works without any code changes because the JAR automatically overrides the HTTP transport factory.

### In Spark Applications

Add the JAR to your Spark submit command:

```bash
spark-submit --jars openlineage-insecure-wrapper-1.0-SNAPSHOT.jar ...
```

#### Using the Custom Spark Listener

You can explicitly use the insecure listener in your Spark configuration:

```
--conf spark.extraListeners=io.openlineage.spark.agent.InsecureOpenLineageSparkListener
--conf spark.openlineage.transport.type=http
--conf spark.openlineage.transport.url=https://your-server:port
```

#### Using the Helper Class

You can also use the provided helper class in your Spark application:

```java
import io.openlineage.spark.InsecureOpenLineageSpark;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

SparkConf conf = new SparkConf();
InsecureOpenLineageSpark.configureInsecureTransport(conf, "https://your-server:port");

SparkSession spark = SparkSession.builder()
    .config(conf)
    .appName("Your Spark App")
    .getOrCreate();
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
