package com.djr4488.metrics.health.database;

import com.codahale.metrics.health.HealthCheck;
import com.djr4488.metrics.MetricsRegistryBean;
import com.djr4488.metrics.TestConfiguratorProducer;
import com.djr4488.metrics.TestLogProducer;
import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.config.DatabaseHealthCheckConfig;
import com.djr4488.metrics.config.EclipseLinkProfilerConfig;
import com.djr4488.metrics.config.MetricsRegistryBeanConfig;
import junit.framework.TestCase;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(CdiRunner.class)
@AdditionalClasses({DatabaseHealthCheck.class, DatabaseHealthCheckConfig.class,MetricsRegistryBean.class})
@ActivatedAlternatives({TestConfiguratorProducer.class, TestLogProducer.class})
public class DatabaseHealthCheckTest extends TestCase {
    @Inject
    private DatabaseHealthCheck dhc;
    @Inject
    private Logger log;
    @Inject
    private Configurator configurator;
    @Inject
    private MetricsRegistryBean mrb;

    @Test
    public void testDatabaseHealthCheckSuccess() {
        List<String> persistenceNames = new ArrayList<>();
        persistenceNames.add("jdbc/metrics_persistence_unit");
        List<String> testSql = new ArrayList<>();
        testSql.add("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        try {
            DatabaseHealthCheckConfig dhcConfig = mock(DatabaseHealthCheckConfig.class);
            MetricsRegistryBeanConfig mrbConfig = mock(MetricsRegistryBeanConfig.class);
            EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
            when(configurator.getConfiguration(DatabaseHealthCheckConfig.class)).thenReturn(dhcConfig);
            when(dhcConfig.persistenceUnitNames()).thenReturn(persistenceNames);
            when(dhcConfig.testSqlByPersistenceName()).thenReturn(testSql);
            when(configurator.getConfiguration(MetricsRegistryBeanConfig.class)).thenReturn(mrbConfig);
            when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
            when(elpConfig.eclipseLinkProfileWeight()).thenReturn("ALL");
            HealthCheck.Result result = dhc.check();
            assertEquals(HealthCheck.Result.healthy(), result);
        } catch (Exception ex) {
            log.error("testDatabaseHealthCheckSuccess() ex:{}", ex);
        }
    }
}
