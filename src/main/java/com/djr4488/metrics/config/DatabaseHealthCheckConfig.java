package com.djr4488.metrics.config;

import org.aeonbits.owner.Config;

import java.util.List;

/**
 * Created by djr4488 on 11/5/16.
 */
@Config.Sources({
        "classpath:DatabaseHealthCheckConfig.properties"
})
public interface DatabaseHealthCheckConfig extends Config {
    @Separator(";")
    List<String> persistenceUnitNames();
    @Separator(";")
    List<String> testSqlByPersistenceName();
}
