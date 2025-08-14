package io.openlineage.client.transports;

import io.openlineage.client.OpenLineage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.net.URI;
import java.security.cert.X509Certificate;

/**
 * Factory class for creating insecure HttpTransport instances.
 * Works around the final HttpTransport class by creating instances with custom HttpClient.
 */
public class InsecureHttpTransportWrapper {

    public static HttpTransport create(HttpConfig config) {
        // Install global insecure SSL context before creating transport
        installInsecureSSL();
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
}
