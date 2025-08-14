/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client;

public class OpenLineageClientException extends RuntimeException {
    public OpenLineageClientException(String message) {
        super(message);
    }
    
    public OpenLineageClientException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public OpenLineageClientException(Throwable cause) {
        super(cause);
    }
}
