/*
/* Copyright 2018-2025 contributors to the OpenLineage project
/* SPDX-License-Identifier: Apache-2.0
*/

package io.openlineage.client;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

public class OpenLineage {
    
    public OpenLineage(URI producer) {
        // Constructor
    }
    
    public RunEventBuilder newRunEventBuilder() {
        return new RunEventBuilder();
    }
    
    public DatasetEventBuilder newDatasetEventBuilder() {
        return new DatasetEventBuilder();
    }
    
    public JobEventBuilder newJobEventBuilder() {
        return new JobEventBuilder();
    }
    
    public static class RunEvent extends BaseEvent {
        private Job job;
        private Run run;
        
        public Job getJob() { return job; }
        public Run getRun() { return run; }
    }
    
    public static class DatasetEvent extends BaseEvent {
        private StaticDataset dataset;
        
        public StaticDataset getDataset() { return dataset; }
    }
    
    public static class JobEvent extends BaseEvent {
        private Job job;
        
        public Job getJob() { return job; }
    }
    
    public static class BaseEvent {
        private ZonedDateTime eventTime = ZonedDateTime.now();
        
        public ZonedDateTime getEventTime() { return eventTime; }
    }
    
    public static class Job {
        private String namespace;
        private String name;
        
        public String getNamespace() { return namespace; }
        public String getName() { return name; }
    }
    
    public static class Run {
        private UUID runId;
        
        public UUID getRunId() { return runId; }
    }
    
    public static class StaticDataset {
        private String namespace;
        private String name;
        
        public String getNamespace() { return namespace; }
        public String getName() { return name; }
    }
    
    public static class RunEventBuilder {
        private RunEvent event = new RunEvent();
        
        public RunEventBuilder job(Job job) {
            event.job = job;
            return this;
        }
        
        public RunEventBuilder run(Run run) {
            event.run = run;
            return this;
        }
        
        public RunEvent build() {
            return event;
        }
    }
    
    public static class DatasetEventBuilder {
        private DatasetEvent event = new DatasetEvent();
        
        public DatasetEventBuilder dataset(StaticDataset dataset) {
            event.dataset = dataset;
            return this;
        }
        
        public DatasetEvent build() {
            return event;
        }
    }
    
    public static class JobEventBuilder {
        private JobEvent event = new JobEvent();
        
        public JobEventBuilder job(Job job) {
            event.job = job;
            return this;
        }
        
        public JobEvent build() {
            return event;
        }
    }
    
    public static class JobBuilder {
        private Job job = new Job();
        
        public JobBuilder namespace(String namespace) {
            job.namespace = namespace;
            return this;
        }
        
        public JobBuilder name(String name) {
            job.name = name;
            return this;
        }
        
        public Job build() {
            return job;
        }
    }
    
    public static class RunBuilder {
        private Run run = new Run();
        
        public RunBuilder runId(UUID runId) {
            run.runId = runId;
            return this;
        }
        
        public Run build() {
            return run;
        }
    }
    
    public static class StaticDatasetBuilder {
        private StaticDataset dataset = new StaticDataset();
        
        public StaticDatasetBuilder namespace(String namespace) {
            dataset.namespace = namespace;
            return this;
        }
        
        public StaticDatasetBuilder name(String name) {
            dataset.name = name;
            return this;
        }
        
        public StaticDataset build() {
            return dataset;
        }
    }
}
