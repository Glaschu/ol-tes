/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client;

public interface MergeConfig<T> {
    T mergeWithNonNull(T other);
    
    default <U> U mergePropertyWith(U current, U other) {
        return other != null ? other : current;
    }
}
