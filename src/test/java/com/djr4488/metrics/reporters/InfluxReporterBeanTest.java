package com.djr4488.metrics.reporters;


import com.codahale.metrics.MetricRegistry;
import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.config.InfluxReporterBeanConfig;
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
public class InfluxReporterBeanTest extends TestCase {
    @Mock
    private Configurator configurator;
    @Mock
    private InfluxReporterBeanConfig irbConfig;
    @Mock
    private MetricRegistry metricRegistry;
    @InjectMocks
    private InfluxReporterBean irb = new InfluxReporterBean();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInfluxReporterBeanStartStopWhenEnabled() {
        when(configurator.getConfiguration(InfluxReporterBeanConfig.class)).thenReturn(irbConfig);
        when(irbConfig.enableInfluxReporter()).thenReturn(Boolean.TRUE);
        when(irbConfig.influxReportFrequency()).thenReturn(30);
        when(irbConfig.appName()).thenReturn("appName");
        when(irbConfig.clusterName()).thenReturn("clusterName");
        when(irbConfig.influxUrl()).thenReturn("localhost");
        when(irbConfig.influxPort()).thenReturn(443);
        when(irbConfig.username()).thenReturn("user");
        when(irbConfig.password()).thenReturn("pass");
        when(irbConfig.protocol()).thenReturn("http");
        when(irbConfig.database()).thenReturn("metrics");
        irb.initialize(metricRegistry);
        irb.startStopReporter();
        assertTrue(irb.isReporterBeanEnabled());
    }

    @Test
    public void testInfluxReporterBeanStartStopWhenDisabled() {
        when(configurator.getConfiguration(InfluxReporterBeanConfig.class)).thenReturn(irbConfig);
        when(irbConfig.enableInfluxReporter()).thenReturn(Boolean.FALSE);
        when(irbConfig.influxReportFrequency()).thenReturn(30);
        when(irbConfig.appName()).thenReturn("appName");
        when(irbConfig.clusterName()).thenReturn("clusterName");
        when(irbConfig.influxUrl()).thenReturn("localhost");
        when(irbConfig.influxPort()).thenReturn(443);
        when(irbConfig.username()).thenReturn("user");
        when(irbConfig.password()).thenReturn("pass");
        when(irbConfig.protocol()).thenReturn("http");
        when(irbConfig.database()).thenReturn("metrics");
        irb.initialize(metricRegistry);
        irb.startStopReporter();
        assertFalse(irb.isReporterBeanEnabled());
    }
}
