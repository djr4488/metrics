package com.djr4488.metrics.config;

import junit.framework.TestCase;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Created by djr4488 on 11/6/16.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({DatabaseHealthCheckConfig.class, InfluxReporterBeanConfig.class, MetricsRegistryBeanConfig.class,
                    Slf4jReporterBeanConfig.class, Configurator.class})
public class ConfiguratorTest extends TestCase {
    @Inject
    private Configurator configurator;

    //given: key=value
    //and: the following key value pairings
    // contextLookupKey=java:openejb/PersistenceUnit/
    //when:
    // getting configuration for key
    //then:
    // expect value
    @Test
    public void testConfiguratorDatabaseHealthCheckConfiguration() {
        DatabaseHealthCheckConfig dbConfig = configurator.getConfiguration(DatabaseHealthCheckConfig.class);
        assertNotNull(dbConfig);
        assertNotNull(dbConfig.contextLookupKey());
        assertEquals("java:openejb/PersistenceUnit/", dbConfig.contextLookupKey());
    }

    /**
     * given: key=value
     * and: the following key value pairings
     *  influxUrl=influxdb.url
     *  influxPort=443
     *  username=influxUserName
     *  password=influxPassword
     *  database=metricsDatabase
     *  protocol=https
     *  appName=metrics_extensions
     *  clusterName=metrics_cluster
     *  influxReportFrequency=30
     *  enableInfluxReporter=false
     * when:
     *  getting configuration for key
     * then:
     *  expect value
     */
    @Test
    public void testConfiguratorInfluxReporterBeanConfiguration() {
        InfluxReporterBeanConfig cfg = configurator.getConfiguration(InfluxReporterBeanConfig.class);
        assertNotNull(cfg);
        assertEquals("influxdb.url", cfg.influxUrl());
        assertEquals(443, cfg.influxPort().intValue());
        assertEquals("influxUserName", cfg.username());
        assertEquals("influxPassword", cfg.password());
        assertEquals("metricsDatabase", cfg.database());
        assertEquals("https", cfg.protocol());
        assertEquals("metrics_extensions", cfg.appName());
        assertEquals("metrics_cluster", cfg.clusterName());
        assertEquals(30, cfg.influxReportFrequency().intValue());
        assertEquals(false, cfg.enableInfluxReporter().booleanValue());
    }

    //given: key=value
    //and: the following key value pairings
    // enableSlf4jReporter=true
    // slf4jReporterNames=slf4jReporterBean
    // enableScheduledReporters=false
    // scheduledReporterNames=influxReporterBean
    // enableJvmCapture=true
    // healthCheckNamesToRegister=databaseHealthCheck;jmsHealthCheck
    //when:
    // getting configuration for key
    //then:
    // expect value
    @Test
    public void testMetricsRegistryBeanConfiguration() {
        MetricsRegistryBeanConfig cfg = configurator.getConfiguration(MetricsRegistryBeanConfig.class);
        assertNotNull(cfg);
        assertEquals(true, cfg.enableSlf4jReporter().booleanValue());
        assertEquals("slf4jReporterBean", cfg.slf4jReporterNames().get(0));
        assertEquals(true, cfg.enableScheduledReporters().booleanValue());
        assertEquals("influxReporterBean", cfg.scheduledReporterNames().get(0));
        assertEquals(true, cfg.enableJvmCapture().booleanValue());
        assertEquals("databaseHealthCheck", cfg.healthCheckNamesToRegister().get(0));
        assertEquals("jmsHealthCheck", cfg.healthCheckNamesToRegister().get(1));
    }

    //given: key=value
    //and: the following key value pairings
    // slf4jReportFrequency=30
    // enableSlf4jReporter=true
    //when:
    // getting configuration for key
    //then:
    // expect value
    @Test
    public void testSlf4jReporterBeanConfiguration() {
        Slf4jReporterBeanConfig cfg = configurator.getConfiguration(Slf4jReporterBeanConfig.class);
        assertNotNull(cfg);
        assertEquals(30, cfg.slf4jReportFrequency().intValue());
        assertEquals(true, cfg.enableSlf4jReporter().booleanValue());
    }

    //given: key=value
    //and: the following key value pairings
    // eclipseLinkProfileWeight=NONE
    //when:
    // getting configuration for key
    //then:
    // expect value
    @Test
    public void testEclipseLinkProfilerConfiguration() {
        EclipseLinkProfilerConfig cfg = configurator.getConfiguration(EclipseLinkProfilerConfig.class);
        assertNotNull(cfg);
        assertEquals("NONE", cfg.eclipseLinkProfileWeight());
    }
}
