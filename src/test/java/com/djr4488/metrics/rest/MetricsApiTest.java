package com.djr4488.metrics.rest;

import com.codahale.metrics.MetricRegistry;
import com.djr4488.metrics.MetricsRegistryBean;
import com.djr4488.metrics.TestLogProducer;
import com.djr4488.metrics.config.Configurator;
import junit.framework.TestCase;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by djr4488 on 11/6/16.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({MetricsRegistryBean.class, Configurator.class, MetricsApi.class, MetricRegistry.class})
@ActivatedAlternatives({TestLogProducer.class})
public class MetricsApiTest extends TestCase {
    @Inject
    private MetricsApi metricsApi;
    @Inject
    private Logger log;

    @Test
    public void testMetricEndpoint() {
        MetricRegistry metricRegistry = metricsApi.metrics();
        assertTrue(metricRegistry.getNames().contains("jvm.memory.heap.committed"));
    }

    @Test
    public void testThreadDumpEndpoint() {
        try {
            String threadDump = metricsApi.threadDump();
            assertNotNull(threadDump);
            assertTrue(threadDump.contains("main id=1"));
        } catch (IOException ioEx) {
            fail("did not expect this");
        }
    }

    @Test
    public void testPing() {
        assertEquals("pong", metricsApi.ping());
    }

    @Test
    public void testHealth() {
        try {
            Response response = metricsApi.health();
            assertEquals(200, response.getStatus());
            String entity = response.getEntity().toString();
            assertTrue(entity.contains("\"healthy\" : true"));
        } catch (IOException ioEx) {
            fail("did not expect IOException");
        }
    }
}
