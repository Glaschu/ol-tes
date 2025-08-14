package io.openlineage.spark.insecure;

import io.openlineage.spark.InsecureOpenLineageSpark;
import io.openlineage.spark.agent.OpenLineageSparkListener;
import org.apache.spark.SparkConf;
import org.apache.spark.scheduler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single drop-in SparkListener:
 *  - Installs a permissive (INSECURE) SSL context (test-only) on class load
 *  - Delegates all Spark lifecycle callbacks to the standard OpenLineageSparkListener
 *
 * Usage (Method 1 - Auto-discovery via ServiceLoader):
 *   Just add this JAR to --extra-jars; listener auto-registers.
 *   Configure normal spark.openlineage.* transport configs.
 *
 * Usage (Method 2 - Explicit):
 *   --conf spark.extraListeners=io.openlineage.spark.insecure.InsecureOpenLineageUnifiedListener
 *   plus normal spark.openlineage.* transport configs.
 *
 * SECURITY: DO NOT USE IN PRODUCTION.
 */
public class InsecureOpenLineageUnifiedListener extends SparkListener {
  private static final Logger log = LoggerFactory.getLogger(InsecureOpenLineageUnifiedListener.class);
  private final OpenLineageSparkListener delegate;

  static {
    try {
      InsecureOpenLineageSpark.installGlobalInsecureSSL();
      log.warn("[InsecureOpenLineageUnifiedListener] Installed GLOBAL insecure SSL context. TEST USE ONLY.");
    } catch (RuntimeException ex) {
      log.error("Failed to install insecure SSL context", ex);
    }
  }

  public InsecureOpenLineageUnifiedListener() {
    this.delegate = createDelegate();
  }

  // Spark may try (SparkConf) constructor via reflection; provide it.
  public InsecureOpenLineageUnifiedListener(SparkConf conf) {
    this.delegate = createDelegate();
  }

  private OpenLineageSparkListener createDelegate() {
    try {
      return new OpenLineageSparkListener();
    } catch (Throwable t) {
      log.error("Failed to instantiate OpenLineageSparkListener; lineage events will NOT be emitted", t);
      return new OpenLineageSparkListener(); // final attempt; may still fail later
    }
  }

  @Override
  public void onJobStart(SparkListenerJobStart jobStart) { delegate.onJobStart(jobStart); }

  @Override
  public void onJobEnd(SparkListenerJobEnd jobEnd) { delegate.onJobEnd(jobEnd); }

  @Override
  public void onTaskEnd(SparkListenerTaskEnd taskEnd) { delegate.onTaskEnd(taskEnd); }

  @Override
  public void onApplicationStart(SparkListenerApplicationStart appStart) { delegate.onApplicationStart(appStart); }

  @Override
  public void onApplicationEnd(SparkListenerApplicationEnd appEnd) { delegate.onApplicationEnd(appEnd); }

  @Override
  public void onOtherEvent(SparkListenerEvent event) { delegate.onOtherEvent(event); }
}
