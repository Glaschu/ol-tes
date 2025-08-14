/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports.httpinsecure;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Example demonstrating how to use the HttpInsecureTransport
 * for sending OpenLineage events to an endpoint with self-signed certificates.
 */
public class HttpInsecureTransportExample {
    
    public static void main(String[] args) {
        try {
            // Configure the HTTP Insecure transport
            HttpInsecureConfig config = new HttpInsecureConfig();
            config.setUrl(URI.create("https://your-lineage-endpoint.com"));
            config.setTimeoutInMillis(10000);
            config.setCompression(HttpInsecureConfig.Compression.GZIP);
            
            // Add custom headers if needed
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer your-api-token");
            headers.put("X-Custom-Header", "custom-value");
            config.setHeaders(headers);
            
            // Create the transport and client
            HttpInsecureTransport transport = new HttpInsecureTransport(config);
            OpenLineageClient client = new OpenLineageClient(transport);
            
            // Create a sample run event
            OpenLineage ol = new OpenLineage(URI.create("https://example.com/producer"));
            
            OpenLineage.Job job = new OpenLineage.JobBuilder()
                .namespace("example-namespace")
                .name("example-job")
                .build();
                
            OpenLineage.Run run = new OpenLineage.RunBuilder()
                .runId(UUID.randomUUID())
                .build();
                
            OpenLineage.RunEvent runEvent = ol.newRunEventBuilder()
                .job(job)
                .run(run)
                .build();
            
            // Emit the event
            System.out.println("Sending lineage event...");
            client.emit(runEvent);
            System.out.println("Lineage event sent successfully!");
            
            // Clean up
            client.close();
            
        } catch (Exception e) {
            System.err.println("Failed to send lineage event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
