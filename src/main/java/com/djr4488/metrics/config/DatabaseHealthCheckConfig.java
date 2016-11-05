package com.djr4488.metrics.config;

import org.aeonbits.owner.Config;

/**
 * Created by djr4488 on 11/5/16.
 */
@Config.Sources({
        "classpath:DatabaseHealthCheckConfig.properties"
})
public interface DatabaseHealthCheckConfig extends Config {
    String contextLookupKey();
}
