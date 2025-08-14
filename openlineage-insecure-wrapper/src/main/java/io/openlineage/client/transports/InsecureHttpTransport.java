package io.openlineage.client.transports;

import io.openlineage.client.OpenLineageClientException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * An extension of HttpTransport that disables SSL certificate validation.
 * WARNING: This should only be used for testing environments and not in production.
 */
@Slf4j
public class InsecureHttpTransport extends HttpTransport {

    /**
     * Creates an insecure HTTP transport with the specified configuration.
     * This transport will not validate SSL certificates.
     *
     * @param httpConfig The HTTP configuration
     */
    public InsecureHttpTransport(@NonNull HttpConfig httpConfig) {
        super(createInsecureHttpClient(), httpConfig);
        log.warn("SECURITY WARNING: Using InsecureHttpTransport which disables SSL certificate validation. " +
                "This should only be used in testing environments!");
    }

    /**
     * Creates a HTTP client that accepts all SSL certificates without validation.
     *
     * @return An insecure HTTP client
     */
    private static CloseableHttpClient createInsecureHttpClient() {
        try {
            // Create an SSL context with our trust-all manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { new InsecureTrustManager() }, new java.security.SecureRandom());
            
            // Create a connection socket factory that doesn't verify hostnames
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext, 
                NoopHostnameVerifier.INSTANCE
            );
            
            // Build and return a client with the insecure SSL setup
            return HttpClientBuilder.create()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setConnectionManager(
                    PoolingHttpClientConnectionManagerBuilder.create()
                        .setSSLSocketFactory(sslSocketFactory)
                        .build())
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create insecure HTTP client", e);
        }
    }

    /**
     * Convenience factory method to create an insecure transport from a URI string.
     *
     * @param uri The URI string for the OpenLineage API endpoint
     * @return An insecure HTTP transport
     */
    public static InsecureHttpTransport forUri(String uri) {
        try {
            HttpConfig config = new HttpConfig();
            config.setUrl(new URI(uri));
            return new InsecureHttpTransport(config);
        } catch (URISyntaxException e) {
            throw new OpenLineageClientException("Invalid URI", e);
        }
    }
}
