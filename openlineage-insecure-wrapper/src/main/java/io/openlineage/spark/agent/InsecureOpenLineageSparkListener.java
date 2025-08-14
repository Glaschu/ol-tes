package io.openlineage.spark.agent;

import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.InsecureHttpTransport;
import io.openlineage.spark.agent.lifecycle.plan.SparkPlanVisitor;
import io.openlineage.spark.agent.lifecycle.SparkListener;
import org.apache.spark.SparkConf;

import java.net.URI;
import java.util.Optional;

/**
 * A customized version of OpenLineageSparkListener that uses the InsecureHttpTransport
 * to disable SSL certificate validation. This allows connections to OpenLineage API
 * endpoints with self-signed certificates during testing.
 */
public class InsecureOpenLineageSparkListener extends OpenLineageSparkListener {
    
    /**
     * Constructor that creates a listener with SSL certificate validation disabled.
     * 
     * @param conf The SparkConf containing OpenLineage configuration
     */
    public InsecureOpenLineageSparkListener(SparkConf conf) {
        super(conf);
    }
    
    /**
     * Override the client creation method to use our insecure transport.
     * 
     * @param url The API URL from configuration
     * @return An OpenLineageClient with SSL validation disabled
     */
    @Override
    protected OpenLineageClient createOpenLineageClient(String url) {
        return OpenLineageClient.builder()
            .transport(new InsecureHttpTransport(URI.create(url)))
            .build();
    }
    
    /**
     * Override the transport creation method to use our insecure transport.
     * 
     * @param type The transport type
     * @param apiUrl The API URL
     * @return An OpenLineageClient with SSL validation disabled
     */
    @Override
    protected OpenLineageClient getOpenLineageClient(String type, String apiUrl) {
        // Always use our insecure HTTP transport regardless of the requested type
        return OpenLineageClient.builder()
            .transport(new InsecureHttpTransport(URI.create(apiUrl)))
            .build();
    }
}
