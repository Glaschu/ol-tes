package io.openlineage.spark.insecure;

import io.openlineage.spark.InsecureOpenLineageSpark;

/**
 * Bootstrap class that installs insecure SSL context immediately when loaded.
 * This ensures SSL bypass happens as early as possible in the JVM lifecycle.
 */
public class EarlySSLBootstrap {
    static {
        try {
            // Force immediate SSL context installation
            InsecureOpenLineageSpark.installGlobalInsecureSSL();
            
            // Set system properties that might be read by shaded HTTP components
            System.setProperty("javax.net.ssl.trustStore", "");
            System.setProperty("javax.net.ssl.trustStorePassword", "");
            System.setProperty("javax.net.ssl.keyStore", "");
            System.setProperty("javax.net.ssl.keyStorePassword", "");
            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
            
            // Properties that might be checked by OpenLineage's shaded HTTP client
            System.setProperty("io.openlineage.ssl.trustAll", "true");
            System.setProperty("openlineage.http.ssl.verify", "false");
            System.setProperty("httpclient.ssl.verify", "false");
            System.setProperty("apache.http.ssl.verify", "false");
            
            // Disable all SSL verification at JVM level
            System.setProperty("trust_all_cert", "true");
            System.setProperty("javax.net.ssl.trustStoreType", "");
            
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
