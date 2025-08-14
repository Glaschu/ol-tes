package io.openlineage.spark.insecure;

import io.openlineage.spark.InsecureOpenLineageSpark;

/**
 * Bootstrap class that installs insecure SSL context immediately when loaded.
 * This ensures SSL bypass happens as early as possible in the JVM lifecycle.
 */
public class EarlySSLBootstrap {
    static {
        try {
            // Clear all SSL-related system properties that might cause keystore errors
            System.clearProperty("javax.net.ssl.keyStore");
            System.clearProperty("javax.net.ssl.keyStorePassword");
            System.clearProperty("javax.net.ssl.keyStoreType");
            System.clearProperty("javax.net.ssl.trustStore");
            System.clearProperty("javax.net.ssl.trustStorePassword");
            System.clearProperty("javax.net.ssl.trustStoreType");
            
            // Force immediate SSL context installation
            InsecureOpenLineageSpark.installGlobalInsecureSSL();
            
            // Set system properties that might be read by shaded HTTP components
            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
            System.setProperty("sun.net.useExclusiveBind", "false");
            
            // Properties that might be checked by OpenLineage's shaded HTTP client
            System.setProperty("io.openlineage.ssl.trustAll", "true");
            System.setProperty("openlineage.http.ssl.verify", "false");
            System.setProperty("httpclient.ssl.verify", "false");
            System.setProperty("apache.http.ssl.verify", "false");
            
            // Disable all SSL verification at JVM level
            System.setProperty("trust_all_cert", "true");
            
            System.err.println("[EarlySSLBootstrap] Comprehensive SSL bypass installed. TEST ONLY!");
        } catch (Exception e) {
            System.err.println("[EarlySSLBootstrap] Failed to install SSL bypass: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Trigger static block
    public static void init() {
        // Static block already executed
    }
}
