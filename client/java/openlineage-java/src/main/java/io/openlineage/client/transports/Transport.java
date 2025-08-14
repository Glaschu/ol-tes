/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client.transports;

public abstract class Transport implements AutoCloseable {
    
    public abstract void emit(io.openlineage.client.OpenLineage.RunEvent runEvent);
    
    public abstract void emit(io.openlineage.client.OpenLineage.DatasetEvent datasetEvent);
    
    public abstract void emit(io.openlineage.client.OpenLineage.JobEvent jobEvent);
    
    @Override
    public void close() throws Exception {
        // Default implementation
    }
}
