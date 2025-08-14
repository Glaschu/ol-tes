package io.openlineage.spark;

import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.HttpConfig;
import io.openlineage.client.transports.HttpTransport;

import java.net.URI;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.spark.SparkConf;

/**
 * A utility class for initializing the OpenLineage Spark client with
 * an insecure HTTP transport that disables SSL certificate validation.
 * 
 * This class provides methods to integrate with Spark listener initialization.
 */
public class InsecureOpenLineageSpark {

    /**
     * Constructs an OpenLineageClient with SSL certificate validation disabled.
     * This can be used directly with OpenLineageSparkListener.
     * 
     * @param url The URL of the OpenLineage API
     * @return A client with disabled SSL certificate validation
     */
    public static OpenLineageClient createInsecureClient(String url) {
        installGlobalInsecureSSL();
        return new OpenLineageClient(io.openlineage.client.transports.InsecureHttpTransportWrapper.create(URI.create(url)));
    }
    
    /**
     * Creates an InsecureHttpTransport with the given URI.
     * This can be used directly in custom transport configurations.
     * 
     * @param uri The URI for the OpenLineage API
     * @return An InsecureHttpTransport instance
     */
    public static HttpTransport createInsecureTransport(URI uri) {
        installGlobalInsecureSSL();
        return io.openlineage.client.transports.InsecureHttpTransportWrapper.create(uri);
    }
    
    /**
     * Configures Spark to use the InsecureHttpTransport for OpenLineage.
     * Call this method in your Spark application before creating the SparkSession.
     * 
     * @param conf The SparkConf to configure
     * @param apiUrl The OpenLineage API URL
     */
    public static void configureInsecureTransport(SparkConf conf, String apiUrl) {
        // Configure Spark to use HTTP transport
        conf.set("spark.openlineage.transport.type", "http");
        conf.set("spark.openlineage.transport.url", apiUrl);
        installGlobalInsecureSSL();
    }

    /**
     * Installs a permissive trust manager & hostname verifier globally. Use ONLY for testing.
     */
    public static void installGlobalInsecureSSL() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[0]; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }
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
