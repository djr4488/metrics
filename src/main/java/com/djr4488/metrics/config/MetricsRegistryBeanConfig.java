package com.djr4488.metrics.config;

import org.aeonbits.owner.Config;

import java.util.List;

/**
 * Created by djr4488 on 11/1/16.
 */
@Config.Sources({
        "classpath:MetricsRegistryBeanConfig.properties"
})
public interface MetricsRegistryBeanConfig extends Config {
    @DefaultValue("false")
    Boolean enableSlf4jReporter();
    @Separator(";")
    @DefaultValue("slf4jReporterBean")
    List<String> slf4jReporterNames();
    @DefaultValue("false")
    Boolean enableScheduledReporters();
    @Separator(";")
    @DefaultValue("slf4jReporter")
    List<String> scheduledReporterNames();
    @DefaultValue("false")
    Boolean enableJvmCapture();
    @Separator(";")
    List<String> healthCheckNamesToRegister();
}
