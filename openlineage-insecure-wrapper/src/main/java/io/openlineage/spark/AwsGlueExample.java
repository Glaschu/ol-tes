package io.openlineage.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

/**
 * Example class demonstrating how to use the insecure OpenLineage wrapper in AWS Glue.
 * 
 * This class shows how to configure a Spark/Glue job to use the insecure HTTP transport
 * for OpenLineage when connecting to endpoints with self-signed certificates.
 */
public class AwsGlueExample {

    public static void main(String[] args) {
        // Create a SparkConf with insecure OpenLineage transport
        SparkConf conf = new SparkConf();
        
        // Configure OpenLineage with insecure transport
        // Replace with your actual OpenLineage API endpoint
        String openLineageApiUrl = "https://your-openlineage-api:5000";
        InsecureOpenLineageSpark.configureInsecureTransport(conf, openLineageApiUrl);
        
        // Optional: Explicitly set the insecure listener
        conf.set("spark.extraListeners", "io.openlineage.spark.agent.InsecureOpenLineageSparkListener");
        
        // Create the SparkSession
        SparkSession spark = SparkSession.builder()
            .config(conf)
            .appName("OpenLineage Insecure Example")
            .getOrCreate();
            
        try {
            // Your Spark/Glue job code here
            spark.sql("SHOW TABLES").show();
            
            // This will automatically send events to OpenLineage without certificate validation
            
        } finally {
            spark.stop();
        }
    }
    
    /**
     * Alternative approach that sets system properties directly.
     * You can use this if the service provider approach doesn't work.
     */
    public static void configureSystemProperties() {
        // Disable SSL certificate validation globally
        System.setProperty("javax.net.ssl.trustStore", "NONE");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("com.sun.net.ssl.checkRevocation", "false");
        System.setProperty("javax.net.ssl.trustManager", "TrustAllX509TrustManager");
    }
}
