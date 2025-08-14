/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports.httpinsecure;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.apache.hc.core5.http.HttpHeaders.ACCEPT;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClientException;
import io.openlineage.client.OpenLineageClientUtils;
import io.openlineage.client.transports.Transport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;

@Slf4j
public final class HttpInsecureTransport extends Transport {
  private static final String API_V1 = "/api/v1";

  private final CloseableHttpClient http;
  private final URI uri;
  private final Map<String, String> headers;
  private @Nullable final HttpInsecureConfig.Compression compression;

  public HttpInsecureTransport(@NonNull final HttpInsecureConfig httpConfig) {
    this(withInsecureTimeout(httpConfig), httpConfig);
    
    // Console logging for AWS Glue debugging
    System.out.println("=== HttpInsecureTransport v1.33.0 initialized ===");
    System.out.println("Target URI: " + this.uri);
    System.out.println("Headers: " + this.headers);
    System.out.println("Compression: " + this.compression);
    System.out.println("SSL Certificate Verification: DISABLED");
    System.out.println("OpenLineage Version: 1.33.0 Compatible");
    System.out.println("Transport Type: http_insecure");
    System.out.println("===============================================");
  }

  public HttpInsecureTransport(
      @NonNull final CloseableHttpClient httpClient, @NonNull final HttpInsecureConfig httpConfig) {
    this.http = httpClient;
    try {
      this.uri = getUri(httpConfig);
    } catch (URISyntaxException e) {
      throw new OpenLineageClientException(e);
    }
    this.headers = httpConfig.getHeaders() != null ? httpConfig.getHeaders() : new HashMap<>();
    this.compression = httpConfig.getCompression();
    
    // Console logging for AWS Glue debugging
    System.out.println("=== HttpInsecureTransport initialized ===");
    System.out.println("Target URI: " + this.uri);
    System.out.println("Headers: " + this.headers);
    System.out.println("Compression: " + this.compression);
    System.out.println("SSL Certificate Verification: DISABLED");
    System.out.println("OpenLineage Version: 1.33.0 Compatible");
    System.out.println("Transport Type: http_insecure");
    System.out.println("===============================================");
  }

  private static CloseableHttpClient withInsecureTimeout(HttpInsecureConfig httpConfig) {
    int timeoutMs;
    if (httpConfig.getTimeoutInMillis() != null) {
      timeoutMs = httpConfig.getTimeoutInMillis();
    } else {
      timeoutMs = 5000; // default
    }
    Timeout timeout = Timeout.ofMilliseconds(timeoutMs);

    // Create a trust manager that accepts all certificates
    TrustManager[] trustAllCerts = new TrustManager[] {
      new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
          // Trust all client certificates
        }
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
          // Trust all server certificates
        }
      }
    };

    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new OpenLineageClientException("Failed to create insecure SSL context", e);
    }

    PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeout).build())
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
            .setConnPoolPolicy(PoolReusePolicy.LIFO)
            .setDefaultConnectionConfig(
                ConnectionConfig.custom()
                    .setSocketTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setTimeToLive(timeout)
                    .build());

    // Set up insecure TLS strategy
    DefaultClientTlsStrategy tlsStrategy = new DefaultClientTlsStrategy(
        sslContext,
        NoopHostnameVerifier.INSTANCE);
    connectionManagerBuilder.setTlsSocketStrategy(tlsStrategy);

    RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectionRequestTimeout(timeout)
            .setResponseTimeout(timeout)
            .build();

    System.out.println("=== HttpInsecureTransport SSL Context ===");
    System.out.println("SSL Context: " + sslContext.getProtocol());
    System.out.println("Hostname Verification: DISABLED");
    System.out.println("Trust All Certificates: ENABLED");
    System.out.println("=========================================");

    return HttpClientBuilder.create()
        .setDefaultRequestConfig(requestConfig)
        .setConnectionManager(connectionManagerBuilder.build())
        .build();
  }

  private URI getUri(HttpInsecureConfig httpConfig) throws URISyntaxException {
    URI url = httpConfig.getUrl();
    if (url == null) {
      throw new OpenLineageClientException(
          "url can't be null, try setting transport.url in config");
    }
    URIBuilder builder = new URIBuilder(url);
    if (StringUtils.isNotBlank(url.getPath())) {
      if (StringUtils.isNotBlank(httpConfig.getEndpoint())) {
        throw new OpenLineageClientException("You can't pass both uri and endpoint parameters.");
      }
    } else {
      String endpoint =
          StringUtils.isNotBlank(httpConfig.getEndpoint())
              ? httpConfig.getEndpoint()
              : API_V1 + "/lineage";
      builder.setPath(endpoint);
    }
    return builder.build();
  }

  @Override
  public void emit(@NonNull OpenLineage.RunEvent runEvent) {
    emit(OpenLineageClientUtils.toJson(runEvent));
  }

  @Override
  public void emit(@NonNull OpenLineage.DatasetEvent datasetEvent) {
    emit(OpenLineageClientUtils.toJson(datasetEvent));
  }

  @Override
  public void emit(@NonNull OpenLineage.JobEvent jobEvent) {
    emit(OpenLineageClientUtils.toJson(jobEvent));
  }

  public void emit(String eventAsJson) {
    System.out.println("=== HttpInsecureTransport.emit() called ===");
    System.out.println("Transport Type: http_insecure");
    System.out.println("OpenLineage Version: 1.33.0");
    System.out.println("Event JSON length: " + eventAsJson.length());
    System.out.println("Target URI: " + this.uri);
    
    try {
      ClassicRequestBuilder request = ClassicRequestBuilder.post(uri);
      setHeaders(request);
      setBody(request, eventAsJson);

      System.out.println("Sending HTTP POST request to: " + uri);
      
      http.execute(request.build(), response -> {
        System.out.println("HTTP Response Code: " + response.getCode());
        System.out.println("HTTP Response Reason: " + response.getReasonPhrase());
        
        if (response.getCode() >= 400) {
          String body = "";
          try {
            if (response.getEntity() != null) {
              body = EntityUtils.toString(response.getEntity(), UTF_8);
              System.out.println("HTTP Response Body: " + body);
            }
          } catch (IOException | ParseException e) {
            log.warn("Failed to parse response body", e);
          }
          throw new OpenLineageClientException(
              String.format(
                  "code: %d, response: %s", response.getCode(), body));
        }
        
        System.out.println("Event successfully sent via http_insecure transport!");
        return response;
      });
    } catch (Exception e) {
      System.err.println("Failed to send lineage event via http_insecure transport: " + e.getMessage());
      e.printStackTrace();
      throw new OpenLineageClientException(
          String.format("Failed to send lineage event: %s", eventAsJson), e);
    }
    
    System.out.println("=========================================");
  }

  private void setBody(ClassicRequestBuilder request, String body) {
    HttpEntity entity = new StringEntity(body, APPLICATION_JSON);
    if (compression == HttpInsecureConfig.Compression.GZIP) {
      entity = new GzipCompressingEntity(entity);
      System.out.println("Applied GZIP compression to request body");
    }
    request.setEntity(entity);
  }

  private void setHeaders(ClassicRequestBuilder request) {
    this.headers.forEach((key, value) -> {
      request.setHeader(key, value);
      System.out.println("Added header: " + key + " = " + value);
    });
    // set headers to accept json
    request.setHeader(ACCEPT, APPLICATION_JSON.toString());
    request.setHeader(CONTENT_TYPE, APPLICATION_JSON.toString());
  }

  @Override
  public void close() throws Exception {
    System.out.println("=== HttpInsecureTransport.close() called ===");
    this.http.close();
    System.out.println("HTTP client closed successfully");
    System.out.println("===========================================");
  }
}
