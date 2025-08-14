# AWS Glue Configuration Examples

This document provides examples of how to configure and use the HTTP Insecure Transport in AWS Glue environments.

## JAR Deployment

1. Upload the built JAR to S3:
```bash
aws s3 cp client/java/build/libs/transports-http-insecure-1.21.0.jar s3://your-glue-assets-bucket/jars/
```

2. In your Glue Job configuration, add the JAR to "Dependent JARs path":
```
s3://your-glue-assets-bucket/jars/transports-http-insecure-1.21.0.jar
```

## Configuration Methods

### Method 1: Spark Configuration (Recommended)

Add these to your Glue Job's "Job parameters":

```
Key: --conf
Value: spark.openlineage.transport.type=http_insecure

Key: --conf  
Value: spark.openlineage.transport.url=https://your-lineage-endpoint.com

Key: --conf
Value: spark.openlineage.transport.timeoutInMillis=10000

Key: --conf
Value: spark.openlineage.transport.compression=gzip
```

### Method 2: System Properties

In your Glue script:

```python
import sys
from awsglue.utils import getResolvedOptions
from pyspark.conf import SparkConf
from pyspark.context import SparkContext

# Configure OpenLineage transport
conf = SparkConf()
conf.set("spark.openlineage.transport.type", "http_insecure")
conf.set("spark.openlineage.transport.url", "https://your-lineage-endpoint.com")
conf.set("spark.openlineage.transport.timeoutInMillis", "10000")
conf.set("spark.openlineage.transport.compression", "gzip")

# Add custom headers if needed
conf.set("spark.openlineage.transport.headers.Authorization", "Bearer your-token")

sc = SparkContext(conf=conf)
```

### Method 3: Environment Variables

Set in Glue Job parameters:

```
Key: --conf
Value: spark.executorEnv.OPENLINEAGE_TRANSPORT_TYPE=http_insecure

Key: --conf
Value: spark.executorEnv.OPENLINEAGE_TRANSPORT_URL=https://your-lineage-endpoint.com
```

## Complete Example Glue Job

### PySpark Example

```python
import sys
from awsglue.transforms import *
from awsglue.utils import getResolvedOptions
from pyspark.context import SparkContext
from awsglue.context import GlueContext
from awsglue.job import Job
from pyspark.conf import SparkConf

args = getResolvedOptions(sys.argv, ['JOB_NAME'])

# Configure Spark with OpenLineage HTTP Insecure Transport
conf = SparkConf()
conf.set("spark.openlineage.transport.type", "http_insecure")
conf.set("spark.openlineage.transport.url", "https://your-lineage-endpoint.com")
conf.set("spark.openlineage.transport.timeoutInMillis", "10000")
conf.set("spark.openlineage.transport.compression", "gzip")

# Optional: Add authentication
conf.set("spark.openlineage.transport.headers.Authorization", "Bearer your-api-token")

sc = SparkContext(conf=conf)
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = Job(glueContext)
job.init(args['JOB_NAME'], args)

# Your ETL logic here
print("Starting ETL job with OpenLineage HTTP Insecure Transport...")

# Example: Read from S3
df = spark.read.option("header", "true").csv("s3://your-bucket/input/")

# Transform data
transformed_df = df.filter(df.column_name.isNotNull())

# Write to S3
transformed_df.write.mode("overwrite").parquet("s3://your-bucket/output/")

print("ETL job completed successfully!")

job.commit()
```

### Scala Example

```scala
import com.amazonaws.services.glue.util.GlueArgParser
import com.amazonaws.services.glue.util.Job
import com.amazonaws.services.glue.GlueContext
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf

object GlueJobWithOpenLineage {
  def main(sysArgs: Array[String]): Unit = {
    val args = GlueArgParser.getResolvedOptions(sysArgs, Seq("JOB_NAME").toArray)
    
    // Configure Spark with OpenLineage HTTP Insecure Transport
    val conf = new SparkConf()
    conf.set("spark.openlineage.transport.type", "http_insecure")
    conf.set("spark.openlineage.transport.url", "https://your-lineage-endpoint.com")
    conf.set("spark.openlineage.transport.timeoutInMillis", "10000")
    conf.set("spark.openlineage.transport.compression", "gzip")
    
    val sc = new SparkContext(conf)
    val glueContext = new GlueContext(sc)
    val spark = glueContext.getSparkSession
    
    val job = Job(glueContext)
    job.init(args("JOB_NAME"), args)
    
    // Your ETL logic here
    println("Starting ETL job with OpenLineage HTTP Insecure Transport...")
    
    // Example processing
    val df = spark.read.option("header", "true").csv("s3://your-bucket/input/")
    val transformedDf = df.filter(df.col("column_name").isNotNull)
    transformedDf.write.mode("overwrite").parquet("s3://your-bucket/output/")
    
    println("ETL job completed successfully!")
    
    job.commit()
  }
}
```

## Debugging in AWS Glue

The HTTP Insecure Transport includes extensive console logging. To see these logs in CloudWatch:

1. Enable logging in your Glue Job configuration
2. Set "Log level" to "INFO" or "DEBUG"
3. Check CloudWatch Logs for your job

Look for log entries like:
```
=== HttpInsecureTransport initialized ===
Target URI: https://your-endpoint.com/api/v1/lineage
SSL Certificate Verification: DISABLED
```

## Security Considerations for AWS Glue

1. **VPC Configuration**: Run your Glue job in a private VPC
2. **IAM Roles**: Use least-privilege IAM roles
3. **Secrets Manager**: Store API tokens in AWS Secrets Manager instead of hardcoding
4. **Network ACLs**: Restrict network access to trusted endpoints only

### Using AWS Secrets Manager for API Tokens

```python
import boto3
import json

def get_secret(secret_name, region_name="us-east-1"):
    session = boto3.session.Session()
    client = session.client(
        service_name='secretsmanager',
        region_name=region_name
    )
    
    try:
        get_secret_value_response = client.get_secret_value(
            SecretId=secret_name
        )
        return get_secret_value_response['SecretString']
    except Exception as e:
        print(f"Error retrieving secret: {e}")
        return None

# In your Glue job
api_token = get_secret("openlineage-api-token")
if api_token:
    conf.set("spark.openlineage.transport.headers.Authorization", f"Bearer {api_token}")
```

## Troubleshooting

### Common Issues

1. **ClassNotFoundException**: Ensure the JAR is in the correct S3 path and added to Glue job
2. **SSL Handshake Failures**: Verify the endpoint URL and that SSL verification is actually disabled
3. **Timeout Issues**: Increase the timeout value or check network connectivity
4. **Permission Issues**: Ensure Glue job has necessary IAM permissions for S3 and secrets

### Debug Checklist

- [ ] JAR file uploaded to S3 and accessible by Glue
- [ ] Correct transport type: `http_insecure`
- [ ] Valid endpoint URL (including protocol)
- [ ] Network connectivity from Glue to endpoint
- [ ] CloudWatch logs enabled to see debug output
- [ ] IAM permissions for required AWS services

## Performance Considerations

- Set appropriate timeout values based on your network latency
- Use GZIP compression for large lineage events
- Monitor CloudWatch metrics for job performance impact
- Consider batch sizes if sending many events

## Migration Path

To migrate from this insecure transport to a secure one:

1. Obtain proper SSL certificates for your lineage endpoint
2. Update endpoint to use valid certificates
3. Change transport type from `http_insecure` to `http`
4. Remove the insecure transport JAR from your dependencies
5. Test thoroughly in development environment first
