/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports.httpinsecure;

import io.openlineage.client.transports.Transport;
import io.openlineage.client.transports.TransportBuilder;
import io.openlineage.client.transports.TransportConfig;

public class HttpInsecureTransportBuilder implements TransportBuilder {

  @Override
  public TransportConfig getConfig() {
    return new HttpInsecureConfig();
  }

  @Override
  public Transport build(TransportConfig config) {
    return new HttpInsecureTransport((HttpInsecureConfig) config);
  }

  @Override
  public String getType() {
    return "http_insecure";
  }
}
