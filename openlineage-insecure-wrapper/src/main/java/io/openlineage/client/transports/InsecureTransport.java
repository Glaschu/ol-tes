package io.openlineage.client.transports;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClientUtils;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Custom insecure transport that uses HttpURLConnection with disabled SSL verification.
 * This completely bypasses Apache HTTP Client and shaded dependencies.
 */
public final class InsecureTransport extends Transport {
    
    private final URI uri;
    private final Map<String, String> headers;
    private final TokenProvider tokenProvider;
    
    static {
        // Install global insecure SSL context immediately
        installGlobalInsecureSSL();
    }
    
    public InsecureTransport(InsecureConfig config) {
        super(Type.HTTP);
        this.uri = config.getUrl();
        this.headers = config.getHeaders() != null ? config.getHeaders() : new java.util.HashMap<>();
        this.tokenProvider = config.getAuth();
        
        if (uri == null) {
            throw new IllegalArgumentException("URL is required for InsecureTransport");
        }
    }
    
    @Override
    public void emit(OpenLineage.RunEvent runEvent) {
        try {
            String json = OpenLineageClientUtils.toJson(runEvent);
            
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Add custom headers
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            
            // Add authentication if provided
            if (tokenProvider != null) {
                String token = tokenProvider.getToken();
                if (token != null && !token.isEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }
            }
            
            // Write the JSON payload
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check response
            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to emit OpenLineage event", e);
        }
    }
    
    private static void installGlobalInsecureSSL() {
        try {
            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };
            
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to install insecure SSL context", e);
        }
    }
}
