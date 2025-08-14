package io.openlineage.spark.insecure;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * Ultra-aggressive SSL bypass that runs immediately when any class in this package is loaded.
 * This ensures SSL bypass happens before any HTTP clients are initialized.
 */
public class GlobalSSLBypass {
    
    // Static block runs immediately when class is loaded
    static {
        forceSSLBypass();
    }
    
    public static void forceSSLBypass() {
        try {
            // Clear ALL SSL-related properties that might cause issues
            String[] sslProperties = {
                "javax.net.ssl.keyStore",
                "javax.net.ssl.keyStorePassword", 
                "javax.net.ssl.keyStoreType",
                "javax.net.ssl.trustStore",
                "javax.net.ssl.trustStorePassword",
                "javax.net.ssl.trustStoreType",
                "javax.net.ssl.keyManagerFactory.algorithm",
                "javax.net.ssl.trustManagerFactory.algorithm"
            };
            
            for (String prop : sslProperties) {
                System.clearProperty(prop);
            }
            
            // Create trust manager that accepts everything
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            // Create hostname verifier that accepts everything
            HostnameVerifier trustAllHosts = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) { return true; }
            };
            
            // Install for both TLS and SSL
            for (String protocol : new String[]{"TLS", "SSL"}) {
                try {
                    SSLContext sc = SSLContext.getInstance(protocol);
                    sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    SSLContext.setDefault(sc);
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (Exception e) {
                    // Continue with next protocol
                }
            }
            
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHosts);
            
            // Set system properties for various SSL implementations
            System.setProperty("com.sun.net.ssl.checkRevocation", "false");
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
            System.setProperty("jsse.enableSNIExtension", "false");
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
            
            System.err.println("[GlobalSSLBypass] Ultra-aggressive SSL bypass installed globally");
            
        } catch (Exception e) {
            System.err.println("[GlobalSSLBypass] Failed to install SSL bypass: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
