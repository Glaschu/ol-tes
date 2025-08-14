/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports.httpinsecure;

import io.openlineage.client.transports.Transport;
import io.openlineage.client.transports.TransportBuilder;
import io.openlineage.client.transports.TransportConfig;

public class HttpInsecureTransportBuilder implements TransportBuilder {

  public static final String TRANSPORT_TYPE = "http_insecure";

  static {
    System.out.println("=== HttpInsecureTransportBuilder class loaded ===");
    System.out.println("Transport type: " + TRANSPORT_TYPE);
    System.out.println("Builder class: " + HttpInsecureTransportBuilder.class.getName());
    System.out.println("=================================================");
  }

  public HttpInsecureTransportBuilder() {
    System.out.println("HttpInsecureTransportBuilder instance created for type: " + TRANSPORT_TYPE);
  }

  @Override
  public TransportConfig getConfig() {
    return new HttpInsecureConfig();
  }

  @Override
  public Transport build(TransportConfig config) {
    if (!(config instanceof HttpInsecureConfig)) {
      throw new IllegalArgumentException(
          "Expected HttpInsecureConfig but got " + config.getClass().getSimpleName());
    }
    return new HttpInsecureTransport((HttpInsecureConfig) config);
  }

  @Override
  public String getType() {
    return TRANSPORT_TYPE;
  }
}
