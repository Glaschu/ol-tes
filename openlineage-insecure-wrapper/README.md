# OpenLineage Spark – Insecure SSL Helper (TEST ONLY)

This module provides convenience helpers to disable SSL certificate & hostname verification **only for local / sandbox testing** of the `openlineage-spark` integration against endpoints that use self‑signed certificates.

> ⚠️ **SECURITY WARNING**: Disabling SSL validation exposes you to Man‑in‑the‑Middle attacks and data compromise. **Never ship this to production.** Prefer fixing certificates instead.

## What changed vs the original intent

Recent OpenLineage versions (≥ 0.22.0) made `HttpTransport` final and removed the pluggable `TransportFactory` interface pattern used earlier. Therefore this project no longer tries to:

* Override `HttpTransportFactory` via ServiceLoader
* Subclass `HttpTransport`
* Provide a custom SparkListener replacement

Instead we take the minimal, explicit approach: install a permissive (trust‑all) JVM SSL context + hostname verifier, then use the standard `HttpTransport` / `openlineage-spark` logic unchanged.

## Features Provided

* `InsecureOpenLineageSpark.installGlobalInsecureSSL()` – installs a temporary global trust manager & hostname verifier (current JVM only)
* `InsecureOpenLineageSpark.configureInsecureTransport(SparkConf, url)` – helper to set Spark OpenLineage HTTP configs and install insecure SSL
* `InsecureOpenLineageSpark.createInsecureClient(url)` – build an `OpenLineageClient` that will ignore cert validation (after installing insecure SSL)

## Build

```bash
mvn clean package
```

Resulting JAR: `target/openlineage-insecure-wrapper-1.0-SNAPSHOT.jar`

## Usage Scenarios

### 1. AWS Glue (Spark environment)

Upload the built JAR to S3 and add it to the Glue job (alongside the official `openlineage-spark` JAR you already supply):

```
--extra-jars s3://your-bucket/openlineage-insecure-wrapper-1.0-SNAPSHOT.jar
```

In your Glue script (Scala or Python via JVM config) before creating the session:

```java
import io.openlineage.spark.InsecureOpenLineageSpark;
import org.apache.spark.SparkConf;

SparkConf conf = new SparkConf();
InsecureOpenLineageSpark.configureInsecureTransport(conf, "https://openlineage.dev.local:5000");
// Then build SparkSession as usual; openlineage-spark will pick up configs.
```

Required Spark configs set by helper:
* `spark.openlineage.transport.type = http`
* `spark.openlineage.transport.url = <your URL>`

### 2. Plain spark-submit (local / test cluster)

Put both jars on the classpath (this helper and the standard `openlineage-spark`). Example:

```bash
spark-submit \
    --jars openlineage-insecure-wrapper-1.0-SNAPSHOT.jar,openlineage-spark-0.22.0.jar \
    --conf spark.openlineage.transport.type=http \
    --conf spark.openlineage.transport.url=https://openlineage.dev.local:5000 \
    your-app.jar
```

Inside your code (before first HTTPS OpenLineage emission) optionally call:

```java
InsecureOpenLineageSpark.installGlobalInsecureSSL();
```

If you call `configureInsecureTransport(conf, url)` that already installs the global bypass.

### 2b. spark-submit with ONLY --conf (no code changes)

Add the provided listener so insecure SSL is installed as soon as Spark initializes listeners:

```bash
spark-submit \
    --jars openlineage-insecure-wrapper-1.0-SNAPSHOT.jar,openlineage-spark-0.22.0.jar \
    --conf spark.extraListeners=io.openlineage.spark.insecure.InsecureSslInitListener \
    --conf spark.openlineage.transport.type=http \
    --conf spark.openlineage.transport.url=https://openlineage.dev.local:5000 \
    your-app.jar
```

This path requires zero application code changes. The listener's static initializer installs the global insecure SSL context before OpenLineage first sends events.

### 3. Direct client usage (manual emission)

```java
import io.openlineage.spark.InsecureOpenLineageSpark;
import io.openlineage.client.OpenLineageClient;

OpenLineageClient client = InsecureOpenLineageSpark.createInsecureClient("https://openlineage.dev.local:5000");
// client.emit(runEvent);
```

## Cleanup / Safety Tips

* Call the insecure helpers only in test profiles (e.g., guard with an env var)
* Prefer generating proper CA-signed or self-signed certs and trusting them explicitly
* Restart JVM after testing—changes are global and process‑wide

## Limitations

* Does NOT transparently replace internal OpenLineage transport logic; it merely makes standard HTTPS calls skip validation
* Global SSL bypass affects ALL HTTPS traffic in the JVM, not just OpenLineage

## License

Apache License 2.0
