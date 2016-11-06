package com.djr4488.metrics.config;

import org.aeonbits.owner.Config;

/**
 * Created by djr4488 on 11/6/16.
 */
@Config.Sources({
        "classpath:InfluxReporterBeanConfig.properties"
})
public interface EclipseLinkProfilerConfig {
    @Config.DefaultValue("NONE")
    String eclipseLinkProfileWeight();
}
