package io.openlineage.spark.insecure;

import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.InsecureConfig;
import io.openlineage.client.transports.InsecureTransport;
import io.openlineage.spark.agent.OpenLineageSparkListener;
import org.apache.spark.SparkConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Alternative approach: Create our own OpenLineageSparkListener that uses insecure transport from the start.
 */
public class DirectInsecureSparkListener extends OpenLineageSparkListener {
    private static final Logger log = LoggerFactory.getLogger(DirectInsecureSparkListener.class);
    
    static {
        // Ultra-aggressive SSL bypass first
        GlobalSSLBypass.forceSSLBypass();
        EarlySSLBootstrap.init();
    }

    public DirectInsecureSparkListener() {
        super();
        log.info("DirectInsecureSparkListener: Initializing with custom insecure client");
        replaceWithInsecureClient();
    }

    public DirectInsecureSparkListener(SparkConf conf) {
        super();
        log.info("DirectInsecureSparkListener: Initializing with SparkConf and custom insecure client");
        extractAndSetSystemProperties(conf);
        replaceWithInsecureClient();
    }

    private void extractAndSetSystemProperties(SparkConf conf) {
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
                    log.info("Set system property: {} = {}", key, value);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract configuration from SparkConf", e);
        }
    }

    private void replaceWithInsecureClient() {
        try {
            // Get OpenLineage transport URL from system properties
            String transportUrl = System.getProperty("spark.openlineage.transport.url");
            if (transportUrl == null) {
                transportUrl = System.getProperty("spark.openlineage.url");
            }
            
            if (transportUrl != null) {
                log.info("Creating insecure OpenLineage client for URL: {}", transportUrl);
                
                // Create insecure config and transport
                InsecureConfig config = new InsecureConfig();
                config.setUrl(URI.create(transportUrl));
                
                // Add any custom headers
                Map<String, String> headers = new HashMap<>();
                String apiKey = System.getProperty("spark.openlineage.transport.headers.api-key");
                if (apiKey != null) {
                    headers.put("api-key", apiKey);
                    log.info("Added API key header");
                }
                config.setHeaders(headers);
                
                InsecureTransport insecureTransport = new InsecureTransport(config);
                OpenLineageClient insecureClient = new OpenLineageClient(insecureTransport);
                
                // Try to replace the client field via reflection
                boolean injected = false;
                Class<?> clazz = this.getClass().getSuperclass();
                while (clazz != null && !injected) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (OpenLineageClient.class.isAssignableFrom(field.getType())) {
                            field.setAccessible(true);
                            Object oldClient = field.get(this);
                            field.set(this, insecureClient);
                            log.info("Successfully replaced OpenLineage client in field: {}, old client: {}", 
                                    field.getName(), oldClient != null ? oldClient.getClass().getSimpleName() : "null");
                            injected = true;
                            break;
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
                
                if (!injected) {
                    log.error("Failed to inject insecure client - could not find client field");
                } else {
                    log.info("Successfully initialized DirectInsecureSparkListener with insecure transport");
                }
            } else {
                log.warn("No OpenLineage transport URL found - using default client");
            }
        } catch (Exception e) {
            log.error("Failed to replace with insecure client", e);
        }
    }
}
