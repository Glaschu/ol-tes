/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client;

import io.openlineage.client.transports.Transport;

public class OpenLineageClient implements AutoCloseable {
    
    private final Transport transport;
    
    public OpenLineageClient(Transport transport) {
        this.transport = transport;
    }
    
    public void emit(OpenLineage.RunEvent runEvent) {
        transport.emit(runEvent);
    }
    
    public void emit(OpenLineage.DatasetEvent datasetEvent) {
        transport.emit(datasetEvent);
    }
    
    public void emit(OpenLineage.JobEvent jobEvent) {
        transport.emit(jobEvent);
    }
    
    @Override
    public void close() throws Exception {
        transport.close();
    }
}
