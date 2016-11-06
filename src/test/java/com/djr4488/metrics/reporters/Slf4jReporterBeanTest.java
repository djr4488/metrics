package com.djr4488.metrics.reporters;

import com.codahale.metrics.MetricRegistry;
import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.config.Slf4jReporterBeanConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Created by djr4488 on 11/6/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class Slf4jReporterBeanTest extends TestCase {
    @Mock
    private Configurator configurator;
    @Mock
    private MetricRegistry metricRegistry;
    @Mock
    private Slf4jReporterBeanConfig slf4jConfig;
    @InjectMocks
    private Slf4jReporterBean srb = new Slf4jReporterBean();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSlf4jReporterWhenEnabled() {
        when(configurator.getConfiguration(Slf4jReporterBeanConfig.class)).thenReturn(slf4jConfig);
        when(slf4jConfig.enableSlf4jReporter()).thenReturn(Boolean.TRUE);
        when(slf4jConfig.slf4jReportFrequency()).thenReturn(30);
        srb.initialize(metricRegistry);
        srb.startStopReporter();
        assertTrue(srb.isReporterBeanEnabled());
    }

    @Test
    public void testSlf4ReporterWhenDisabled() {
        when(configurator.getConfiguration(Slf4jReporterBeanConfig.class)).thenReturn(slf4jConfig);
        when(slf4jConfig.enableSlf4jReporter()).thenReturn(Boolean.FALSE);
        when(slf4jConfig.slf4jReportFrequency()).thenReturn(30);
        srb.initialize(metricRegistry);
        srb.startStopReporter();
        assertFalse(srb.isReporterBeanEnabled());
    }
}
