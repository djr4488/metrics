package com.djr4488.metrics;

import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.health.database.DatabaseHealthCheck;
import com.djr4488.metrics.health.database.DatabaseHealthCheckHelper;
import com.djr4488.metrics.health.jms.JMSHealthCheck;
import com.djr4488.metrics.reporters.InfluxReporterBean;
import com.djr4488.metrics.reporters.Slf4jReporterBean;
import junit.framework.TestCase;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.SortedSet;

/**
 * Created by djr4488 on 11/6/16.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({Configurator.class, DatabaseHealthCheck.class, DatabaseHealthCheckHelper.class, JMSHealthCheck.class,
                    InfluxReporterBean.class, Slf4jReporterBean.class})
@ActivatedAlternatives({TestLogProducer.class})
public class MetricsRegistryBeanTest extends TestCase {
    @Inject
    private Logger log;
    @Inject
    private MetricsRegistryBean mrb;

    @Test
    public void testHealthChecksPopulated() {
        SortedSet<String> healthCheckNames = mrb.getHealthCheckRegistry().getNames();
        assertTrue(healthCheckNames.contains("databaseHealthCheck"));
        assertTrue(healthCheckNames.contains("jmsHealthCheck"));
        assertTrue(healthCheckNames.contains("thread-deadlock"));
    }

    @Test
    public void testMetricRegistryPopulated() {
        SortedSet<String> metricNames = mrb.getMetricRegistry().getNames();
        assertTrue(metricNames.contains("jvm.memory.heap.committed"));
    }
}
