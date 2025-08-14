package io.openlineage.client.transports;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.cert.X509Certificate;

/**
 * Factory class for creating insecure HttpTransport instances.
 * Works with OpenLineage's shaded HTTP components.
 */
public class InsecureHttpTransportWrapper {

    public static HttpTransport create(HttpConfig config) {
        // Install both global and shaded SSL contexts
        installInsecureSSL();
        installShadedInsecureSSL();
        return new HttpTransport(config);
    }

    public static HttpTransport create(URI uri) {
        HttpConfig config = new HttpConfig();
        config.setUrl(uri);
        return create(config);
    }

    private static void installInsecureSSL() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                    public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                }
            };
            
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) { return true; }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to install insecure SSL context", e);
        }
    }

    /**
     * Attempts to install insecure SSL for OpenLineage's shaded HTTP components.
     */
    private static void installShadedInsecureSSL() {
        try {
            // Set system properties that shaded HTTP components might read
            System.setProperty("javax.net.ssl.trustStore", "");
            System.setProperty("javax.net.ssl.trustStorePassword", "");
            System.setProperty("javax.net.ssl.keyStore", "");
            System.setProperty("javax.net.ssl.keyStorePassword", "");
            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
            
            // Try to access shaded SSL classes via reflection and disable verification
            trySetShadedSSLContext();
            
        } catch (Exception e) {
            // Not critical if this fails; global SSL should still work for many cases
            System.err.println("Warning: Could not install shaded SSL bypass: " + e.getMessage());
        }
    }
    
    private static void trySetShadedSSLContext() {
        try {
            // Attempt to access and configure shaded HttpClient SSL settings
            Class<?> httpsConnectionSocketFactoryClass = Class.forName(
                "io.openlineage.spark.shaded.org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory"
            );
            
            // If we can load the class, it means shaded components are present
            // Set additional system properties that might be read by shaded components
            System.setProperty("io.openlineage.ssl.trustAll", "true");
            System.setProperty("openlineage.http.ssl.verify", "false");
            
        } catch (ClassNotFoundException e) {
            // Shaded classes not found - this is fine for non-shaded distributions
        } catch (Exception e) {
            // Other reflection errors - log but continue
            System.err.println("Could not configure shaded SSL: " + e.getMessage());
        }
    }
}
