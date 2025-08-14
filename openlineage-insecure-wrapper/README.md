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

Resulting JARs:
* `target/openlineage-insecure-wrapper-1.0-SNAPSHOT.jar` (plain)
* `target/openlineage-insecure-all.jar` (shaded: bundles openlineage-java + openlineage-spark; still relies on Spark + SLF4J from runtime)

## Usage Scenarios

### 1. AWS Glue (Spark environment)

Upload the shaded JAR (`openlineage-insecure-all.jar`) to S3 and add it to the Glue job (no need to add a separate openlineage-spark jar):

```
--extra-jars s3://your-bucket/openlineage-insecure-all.jar
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

Use only the shaded jar:

```bash
spark-submit \
    --jars openlineage-insecure-all.jar \
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

Preferred (single listener): use the unified listener that installs insecure SSL then delegates to OpenLineage:

```bash
spark-submit \
    --jars openlineage-insecure-all.jar \
    --conf spark.extraListeners=io.openlineage.spark.insecure.InsecureOpenLineageUnifiedListener \
    --conf spark.openlineage.transport.type=http \
    --conf spark.openlineage.transport.url=https://openlineage.dev.local:5000 \
    --conf spark.openlineage.namespace=dev \
    --conf spark.app.name=demo-openlineage-job \
    your-app.jar
```

This path requires zero application code changes and only one listener. (Legacy: you can still use the two-listener approach if desired.)

### Troubleshooting: No events emitted

Common causes:
1. Wrong listener: ensure you used `io.openlineage.spark.insecure.InsecureOpenLineageUnifiedListener` (or the two-listener combination) in `spark.extraListeners`.
2. Missing openlineage classes: if you used the plain jar, add the official `openlineage-spark` jar OR switch to the shaded `openlineage-insecure-all.jar`.
3. (Two-listener legacy mode only) Ordering incorrect.
4. Endpoint unreachable / network (Glue VPC security groups, DNS, firewall). Test with a lightweight job hitting the URL (e.g. add a small Scala snippet doing `scala.io.Source.fromURL("https://.../health").mkString`).
5. Self-signed certificate still rejected: verify logs; add `--conf spark.executor.extraJavaOptions=-Djavax.net.debug=ssl --conf spark.driver.extraJavaOptions=-Djavax.net.debug=ssl` for verbose SSL debug (use sparingly; very verbose).
6. Missing required config: at minimum set `spark.openlineage.transport.type`, `spark.openlineage.transport.url`, and ensure `spark.app.name` (used as job name). Optionally set `spark.openlineage.namespace`.
7. Suppressed errors: enable debug logging: `--conf spark.openlineage.debug=true` (if supported by your openlineage-spark version) or raise root logger: `--conf spark.driver.extraJavaOptions='-Dorg.slf4j.simpleLogger.defaultLogLevel=debug'`.

Verification steps:
* Check driver logs for lines containing `OpenLineageSparkListener` and `Sending lineage event` (or similar).
* Use a local proxy (e.g. mitmproxy) pointing `spark.openlineage.transport.url` to inspect outgoing POSTs (testing only).
* Curl / health-check the lineage endpoint from another environment to confirm it is up.

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
