package io.openlineage.spark.insecure;

import io.openlineage.spark.InsecureOpenLineageSpark;
import org.apache.spark.scheduler.SparkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SparkListener that installs a global insecure SSL context when the Spark application starts.
 * This allows using only --conf based OpenLineage configuration (no code changes) while
 * accepting self-signed certificates for testing.
 *
 * WARNING: Disables TLS certificate + hostname verification JVM-wide for the process.
 */
public class InsecureSslInitListener extends SparkListener {
  private static final Logger log = LoggerFactory.getLogger(InsecureSslInitListener.class);

  static {
    try {
      InsecureOpenLineageSpark.installGlobalInsecureSSL();
      log.warn("[InsecureSslInitListener] Installed GLOBAL insecure SSL context. TEST USE ONLY.");
    } catch (RuntimeException ex) {
      log.error("Failed to install insecure SSL context", ex);
    }
  }

  public InsecureSslInitListener() {
    // Trigger static block; nothing else required.
  }
}
