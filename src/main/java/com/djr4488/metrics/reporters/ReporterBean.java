package com.djr4488.metrics.reporters;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by djr4488 on 11/3/16.
 */
public interface ReporterBean {
    public void initialize(MetricRegistry metricRegistry);
    public void stopReporter();
    public void startReporter();
}
