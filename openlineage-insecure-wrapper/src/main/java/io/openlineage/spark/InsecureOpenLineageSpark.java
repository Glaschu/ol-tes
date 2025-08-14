package io.openlineage.spark;

import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.InsecureHttpTransport;

import java.net.URI;
import java.util.Properties;
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
        return OpenLineageClient.builder()
            .transport(new InsecureHttpTransport(URI.create(url)))
            .build();
    }
    
    /**
     * Creates an InsecureHttpTransport with the given URI.
     * This can be used directly in custom transport configurations.
     * 
     * @param uri The URI for the OpenLineage API
     * @return An InsecureHttpTransport instance
     */
    public static InsecureHttpTransport createInsecureTransport(URI uri) {
        return new InsecureHttpTransport(uri);
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
        
        // Set system properties to disable SSL certificate validation globally
        System.setProperty("javax.net.ssl.trustStore", "NONE");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("com.sun.net.ssl.checkRevocation", "false");
        System.setProperty("javax.net.ssl.trustManagerAlgorithm", "X509");
    }
}
