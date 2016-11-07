package com.djr4488.metrics;

import com.djr4488.metrics.config.Configurator;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import static org.mockito.Mockito.mock;

/**
 * Created by djr4488 on 11/6/16.
 */
@Alternative
public class TestConfiguratorProducer {
    private static final Configurator configurator = mock(Configurator.class);

    @Produces
    public Configurator getMockConfigurator(InjectionPoint injectionPoint) {
        return configurator;
    }
}
