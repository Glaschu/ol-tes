/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports.httpinsecure;

import static io.openlineage.client.testdata.OpenLineageEventsDataHelper.datasetEvent;
import static io.openlineage.client.testdata.OpenLineageEventsDataHelper.jobEvent;
import static io.openlineage.client.testdata.OpenLineageEventsDataHelper.runEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.OpenLineageClientException;
import io.openlineage.client.transports.TransportFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import java.net.URI;

class HttpInsecureTransportTests {
  private static final String TEST_URL = "https://localhost:1500";

  private final CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
  private OpenLineageClient openLineageClient;

  @BeforeEach
  public void beforeEach() {
    HttpInsecureConfig config = new HttpInsecureConfig();
    config.setUrl(URI.create(TEST_URL));
    HttpInsecureTransport httpInsecureTransport = new HttpInsecureTransport(httpClient, config);
    openLineageClient = new OpenLineageClient(httpInsecureTransport);
  }

  @Test
  void transportFactoryCreatesHttpInsecureTransport() {
    HttpInsecureConfig config = new HttpInsecureConfig();
    config.setUrl(URI.create(TEST_URL));
    TransportFactory transportFactory = new TransportFactory(config);

    assertTrue(transportFactory.build() instanceof HttpInsecureTransport);
  }

  @Test
  void httpInsecureTransportRaisesExceptionWhenUrlNotProvided() {
    HttpInsecureConfig config = new HttpInsecureConfig();

    assertThrows(
        OpenLineageClientException.class,
        () -> new HttpInsecureTransport(httpClient, config));
  }

  @Test
  void httpInsecureTransportWithoutClientRaisesExceptionWhenUrlNotProvided() {
    HttpInsecureConfig config = new HttpInsecureConfig();

    assertThrows(OpenLineageClientException.class, () -> new HttpInsecureTransport(config));
  }

  @Test
  void clientEmitsRunEventHttpInsecureTransport() throws Exception {
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    when(response.getCode()).thenReturn(200);
    when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
        .thenReturn(response);
    
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    OpenLineage.RunEvent runEvent = runEvent();

    openLineageClient.emit(runEvent);

    verify(httpClient, times(1)).execute(captor.capture(), any(HttpClientResponseHandler.class));
    assertEquals(TEST_URL + "/api/v1/lineage", captor.getValue().getUri().toString());
  }

  @Test
  void clientEmitsDataSetEventHttpInsecureTransport() throws Exception {
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    when(response.getCode()).thenReturn(200);
    when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
        .thenReturn(response);
    
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    OpenLineage.DatasetEvent datasetEvent = datasetEvent();

    openLineageClient.emit(datasetEvent);

    verify(httpClient, times(1)).execute(captor.capture(), any(HttpClientResponseHandler.class));
    assertNotNull(captor.getValue());
  }

  @Test
  void clientEmitsJobEventHttpInsecureTransport() throws Exception {
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    when(response.getCode()).thenReturn(200);
    when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
        .thenReturn(response);
    
    ArgumentCaptor<ClassicHttpRequest> captor = ArgumentCaptor.forClass(ClassicHttpRequest.class);
    OpenLineage.JobEvent jobEvent = jobEvent();

    openLineageClient.emit(jobEvent);

    verify(httpClient, times(1)).execute(captor.capture(), any(HttpClientResponseHandler.class));
    assertNotNull(captor.getValue());
  }

  @SneakyThrows
  @Test
  void clientClose() {
    openLineageClient.close();

    verify(httpClient, times(1)).close();
  }
}
