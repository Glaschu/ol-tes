package io.openlineage.client;

import io.openlineage.client.transports.HttpConfig;
import io.openlineage.client.transports.HttpTransport;
import io.openlineage.spark.InsecureOpenLineageSpark;
import java.net.URI;

/**
 * Example usage class demonstrating insecure client creation.
 */
public class InsecureOpenLineageClientExample {

    public static OpenLineageClient createInsecureClient(String apiUrl) {
        InsecureOpenLineageSpark.installGlobalInsecureSSL();
        HttpConfig cfg = new HttpConfig();
        cfg.setUrl(URI.create(apiUrl));
        return new OpenLineageClient(new HttpTransport(cfg));
    }

    public static void main(String[] args) {
        String apiUrl = "https://your-openlineage-api:5000";
        OpenLineageClient client = createInsecureClient(apiUrl);
        System.out.println("Created insecure OpenLineage client for URL: " + apiUrl);
        System.out.println("WARNING: SSL certificate validation is disabled! DO NOT USE IN PRODUCTION.");
    }
}
