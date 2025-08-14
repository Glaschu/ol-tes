package io.openlineage.spark.insecure;

import io.openlineage.spark.InsecureOpenLineageSpark;
import io.openlineage.spark.agent.OpenLineageSparkListener;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.InsecureConfig;
import io.openlineage.client.transports.InsecureTransport;
import org.apache.spark.SparkConf;
import org.apache.spark.scheduler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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
      // Force ultra-aggressive SSL bypass immediately
      GlobalSSLBypass.forceSSLBypass();
      
      // Force early bootstrap
      EarlySSLBootstrap.init();
      
      // Install insecure SSL immediately when class loads
      InsecureOpenLineageSpark.installGlobalInsecureSSL();
      log.warn("[InsecureOpenLineageUnifiedListener] Installed GLOBAL insecure SSL context. TEST USE ONLY.");
      
      // Also set system properties as backup
      System.setProperty("javax.net.ssl.trustStore", "");
      System.setProperty("javax.net.ssl.trustStorePassword", "");
      System.setProperty("javax.net.ssl.trustStoreType", "JKS");
      System.setProperty("com.sun.net.ssl.checkRevocation", "false");
      System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
      
    } catch (RuntimeException ex) {
      log.error("Failed to install insecure SSL context", ex);
    }
  }

  public InsecureOpenLineageUnifiedListener() {
    this.delegate = createDelegate();
    log.info("InsecureOpenLineageUnifiedListener initialized with SSL bypass");
  }

  // Spark may try (SparkConf) constructor via reflection; provide it.
  public InsecureOpenLineageUnifiedListener(SparkConf conf) {
    // Extract configuration before creating delegate
    extractConfigurationToSystemProperties(conf);
    this.delegate = createDelegate();
    log.info("InsecureOpenLineageUnifiedListener initialized with SparkConf and SSL bypass");
  }

  private void extractConfigurationToSystemProperties(SparkConf conf) {
    // Extract OpenLineage configuration from SparkConf and set as system properties
    // This ensures our injection mechanism can find the configuration
    try {
      String[] configKeys = {
        "spark.openlineage.transport.url",
        "spark.openlineage.url", 
        "spark.openlineage.transport.headers.api-key",
        "spark.openlineage.transport.timeout",
        "spark.openlineage.namespace"
      };
      
      for (String key : configKeys) {
        String value = conf.get(key, null);
        if (value != null) {
          System.setProperty(key, value);
          log.info("Extracted configuration: {} = {}", key, value);
        }
      }
    } catch (Exception e) {
      log.warn("Failed to extract configuration from SparkConf", e);
    }
  }

  private OpenLineageSparkListener createDelegate() {
    try {
      // Re-install SSL bypass right before creating delegate (in case it got reset)
      InsecureOpenLineageSpark.installGlobalInsecureSSL();
      
      OpenLineageSparkListener listener = new OpenLineageSparkListener();
      
      // Try to replace the client in the listener with our insecure one
      injectInsecureClient(listener);
      
      return listener;
    } catch (Throwable t) {
      log.error("Failed to instantiate OpenLineageSparkListener; lineage events will NOT be emitted", t);
      throw new RuntimeException("Cannot create OpenLineageSparkListener even with SSL bypass", t);
    }
  }
  
  /**
   * Attempts to inject an insecure OpenLineageClient into the listener via reflection.
   */
  private void injectInsecureClient(OpenLineageSparkListener listener) {
    try {
      // Get OpenLineage transport URL from system properties or environment
      String transportUrl = System.getProperty("spark.openlineage.transport.url");
      if (transportUrl == null) {
        transportUrl = System.getProperty("spark.openlineage.url");
      }
      if (transportUrl == null) {
        transportUrl = System.getenv("OPENLINEAGE_URL");
      }
      
      if (transportUrl != null) {
        log.info("Injecting insecure client for URL: {}", transportUrl);
        
        // Create insecure config and transport
        InsecureConfig config = new InsecureConfig();
        config.setUrl(URI.create(transportUrl));
        
        // Add any custom headers from system properties
        Map<String, String> headers = new HashMap<>();
        String apiKey = System.getProperty("spark.openlineage.transport.headers.api-key");
        if (apiKey != null) {
          headers.put("api-key", apiKey);
          log.info("Added API key header");
        }
        config.setHeaders(headers);
        
        log.info("Creating InsecureTransport with URL: {}", transportUrl);
        InsecureTransport insecureTransport = new InsecureTransport(config);
        OpenLineageClient insecureClient = new OpenLineageClient(insecureTransport);
        
        // Try to replace the client field via reflection
        // Note: This is fragile and may break with OpenLineage version changes
        boolean injected = false;
        try {
          Field clientField = findClientField(listener.getClass());
          if (clientField != null) {
            clientField.setAccessible(true);
            Object oldClient = clientField.get(listener);
            clientField.set(listener, insecureClient);
            log.info("Successfully injected insecure OpenLineage client, replaced: {}", 
                    oldClient != null ? oldClient.getClass().getSimpleName() : "null");
            injected = true;
          }
        } catch (Exception e) {
          log.warn("Could not inject insecure client via reflection: {}", e.getMessage());
        }
        
        if (!injected) {
          log.error("Failed to inject insecure client - events may fail with SSL errors");
        }
      } else {
        log.warn("No OpenLineage transport URL found in configuration - skipping client injection");
      }
    } catch (Exception e) {
      log.warn("Failed to inject insecure client: {}", e.getMessage());
    }
  }
  
  private Field findClientField(Class<?> clazz) {
    while (clazz != null) {
      for (Field field : clazz.getDeclaredFields()) {
        if (OpenLineageClient.class.isAssignableFrom(field.getType())) {
          return field;
        }
      }
      clazz = clazz.getSuperclass();
    }
    return null;
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
