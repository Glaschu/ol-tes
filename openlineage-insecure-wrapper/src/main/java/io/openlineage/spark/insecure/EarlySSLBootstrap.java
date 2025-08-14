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
            
            // Set additional system properties for maximum coverage
            System.setProperty("javax.net.ssl.trustStore", "");
            System.setProperty("javax.net.ssl.trustStorePassword", "");
            System.setProperty("javax.net.ssl.keyStore", "");
            System.setProperty("javax.net.ssl.keyStorePassword", "");
            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
            
            System.err.println("[EarlySSLBootstrap] Insecure SSL context installed at bootstrap. TEST ONLY!");
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
