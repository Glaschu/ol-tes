package io.openlineage.client;

import io.openlineage.client.transports.InsecureHttpTransport;

/**
 * Example usage class for the InsecureHttpTransport.
 * This demonstrates how to use the insecure transport directly in code.
 */
public class InsecureOpenLineageClientExample {
    
    public static OpenLineageClient createInsecureClient(String apiUrl) {
        return OpenLineageClient.builder()
            .transport(InsecureHttpTransport.forUri(apiUrl))
            .build();
    }
    
    public static void main(String[] args) {
        // Example usage
        String apiUrl = "https://your-openlineage-api:5000";
        OpenLineageClient client = createInsecureClient(apiUrl);
        
        // Use the client to emit events...
        // client.emit(...);
        
        System.out.println("Created insecure OpenLineage client for URL: " + apiUrl);
        System.out.println("WARNING: SSL certificate validation is disabled!");
    }
}
