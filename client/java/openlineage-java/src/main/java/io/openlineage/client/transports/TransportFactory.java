/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports;

public final class TransportFactory {
    private final TransportConfig transportConfig;
    
    public TransportFactory(TransportConfig transportConfig) {
        this.transportConfig = transportConfig;
    }
    
    public Transport build() {
        // Simple implementation for testing
        if (transportConfig instanceof io.openlineage.client.transports.httpinsecure.HttpInsecureConfig) {
            return new io.openlineage.client.transports.httpinsecure.HttpInsecureTransport(
                (io.openlineage.client.transports.httpinsecure.HttpInsecureConfig) transportConfig);
        }
        throw new IllegalArgumentException("Unsupported transport config type: " + transportConfig.getClass());
    }
}
