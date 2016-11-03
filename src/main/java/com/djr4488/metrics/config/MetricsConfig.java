package com.djr4488.metrics.config;

import org.apache.deltaspike.core.api.config.PropertyFileConfig;

/**
 * Created by djr4488 on 11/1/16.
 */
public class MetricsConfig implements PropertyFileConfig {

    private static final long serialVersionUID = -7762380573887097591L;

    @Override
    public String getPropertyFileName() {
        return "metrics-config.properties";
    }

    @Override
    public boolean isOptional() {
        return false;
    }
}
