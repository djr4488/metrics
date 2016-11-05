package com.djr4488.metrics.config;

import org.aeonbits.owner.Config;

/**
 * Created by djr4488 on 11/5/16.
 */
@Config.Sources({
        "classpath:Slf4jReporterBeanConfig.properties"
})
public interface Slf4jReporterBeanConfig extends Config {
    @DefaultValue("60")
    Integer slf4jReportFrequency();
}
