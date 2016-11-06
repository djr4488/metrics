package com.djr4488.metrics.config;

import org.aeonbits.owner.Config;

/**
 * Created by djr4488 on 11/5/16.
 */
@Config.Sources({
        "classpath:InfluxReporterBeanConfig.properties"
})
public interface InfluxReporterBeanConfig extends Config {
    String influxUrl();
    Integer influxPort();
    String username();
    String password();
    String database();
    String protocol();
    String appName();
    String clusterName();
    Integer influxReportFrequency();
    @DefaultValue("false")
    Boolean enableInfluxReporter();
}
