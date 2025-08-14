/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports.httpinsecure;

import io.openlineage.client.MergeConfig;
import io.openlineage.client.transports.TransportConfig;
import java.net.URI;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public final class HttpInsecureConfig
    implements TransportConfig, MergeConfig<HttpInsecureConfig> {

  public enum Compression {
    @JsonProperty("gzip")
    GZIP
  }

  @Getter @Setter private URI url;
  @Getter @Setter private @Nullable String endpoint;
  @Getter @Setter private @Nullable Integer timeoutInMillis;
  @Getter @Setter private @Nullable Map<String, String> headers;
  @Getter @Setter private @Nullable Compression compression;

  @Override
  public HttpInsecureConfig mergeWithNonNull(HttpInsecureConfig other) {
    return new HttpInsecureConfig(
        mergePropertyWith(url, other.url),
        mergePropertyWith(endpoint, other.endpoint),
        mergePropertyWith(timeoutInMillis, other.timeoutInMillis),
        mergePropertyWith(headers, other.headers),
        mergePropertyWith(compression, other.compression));
  }
}
