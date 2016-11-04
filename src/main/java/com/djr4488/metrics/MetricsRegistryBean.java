package com.djr4488.metrics;

/**
 * Created by djr4488 on 9/30/16.
 */
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.djr4488.metrics.reporters.ReporterBean;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Named("metricsRegistryBean")
public class MetricsRegistryBean {
    @Produces
    @ApplicationScoped
    private MetricRegistry metricRegistry = new MetricRegistry();
    private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    @Inject
    @ConfigProperty(name="enableSlf4jReporter", defaultValue="true")
    private Boolean enableSlf4jReporter;
    @Inject
    @ConfigProperty(name="slf4jReporterNames", defaultValue = "slf4jReporterBean")
    private List<String> slf4jReporterNames;
    @Inject
    @ConfigProperty(name="slf4jReporterNamesDelimiter", defaultValue = "|")
    private String slf4jReporterNamesDelimiter;
    @Inject
    @ConfigProperty(name="enableScheduledReporters", defaultValue = "false")
    private Boolean enableScheduledReporters;
    @Inject
    @ConfigProperty(name="scheduledReporterNames", defaultValue = "slf4jReporterBean")
    private List<String> scheduledReporterNames;
    @Inject
    @ConfigProperty(name="scheduledReporterNamesDelimiter", defaultValue = "|")
    private String scheduledReporterNamesDelimiter;
    @Inject
    @ConfigProperty(name="enableJvmCapture", defaultValue = "true")
    private Boolean enableJvmCapture;
    @Inject
    @ConfigProperty(name="healthCheckNamesToRegister", defaultValue = "databaseHealthCheck|jmsHealthCheck")
    private String healthCheckNamesToRegister;
    @Inject
    @ConfigProperty(name="healthCheckNamesDelimiter", defaultValue = "|")
    private String healthCheckNamesDelimiter;
    @Inject
    @ConfigProperty(name="eclipseLinkProfileWeight", defaultValue="ALL")
    private String eclipseLinkProfileWeight;
    private InetAddress address;
    @Inject
    private Logger log;

    public MetricsRegistryBean() {
    }

    @PostConstruct
    public void postContruct() {
        initializeJvmMetrics();
        initializeHealthChecks(buildHealthChecksToRegisterMap());
        initializeReporters();
    }

    private Map<String,HealthCheck> buildHealthChecksToRegisterMap() {
        log.debug("buildHealthChecksToRegisterMap() entered");
        Map<String, HealthCheck> healthCheckMap = new HashMap<>();
        for (String healthCheckName : healthCheckNamesToRegister.split(healthCheckNamesDelimiter)) {
            HealthCheck hc = getHealthCheckBeanByName(healthCheckName);
            if (null != hc) {
                healthCheckMap.put(healthCheckName, hc);
            }
        }
        return healthCheckMap;
    }

    /**
     * This method can return null but is used to find health checks by name
     * @param healthCheckBeanName String
     * @return HealthCheck
     */
    @SuppressWarnings("unchecked")
    private HealthCheck getHealthCheckBeanByName(String healthCheckBeanName) {
        try {
            return MetricsRegistryBean.getBeanByNameOfClass(healthCheckBeanName, HealthCheck.class);
        } catch (Exception ex) {
            log.error("getHealthCheckBeanByName()", ex);
        }
        return null;
    }

    private void initializeJvmMetrics() {
        if (enableJvmCapture) {
            metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
            metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
            metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());
            metricRegistry.register("jvm.class-loading", new ClassLoadingGaugeSet());
            metricRegistry.register("jvm.fd.usage", new FileDescriptorRatioGauge());
        }
    }

    private void initializeHealthChecks(Map<String, HealthCheck> healthCheckMetricsToRegister) {
        for (Map.Entry<String, HealthCheck> healthCheckEntry : healthCheckMetricsToRegister.entrySet()) {
            healthCheckRegistry.register(healthCheckEntry.getKey(), healthCheckEntry.getValue());
        }
        healthCheckRegistry.register("thread-deadlock", new ThreadDeadlockHealthCheck());
    }

    private void initializeReporters() {
        if (enableSlf4jReporter) {
            try {
                getBeanByNameOfClass("slf4jReporter", ReporterBean.class).initialize(metricRegistry);
            } catch (Exception ex) {
                log.debug("initializeReporters() unable to initialize slf4jReporter");
            }
        }
        if (enableScheduledReporters) {
            try {

            } catch (Exception ex) {
                log.debug("initializeReporters() unable to initialize scheduled reporters");
            }
        }
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }



    public static <T> T getBeanByNameOfClass(String name, Class<T> clazz)
    throws Exception {
        BeanManager bm = CDI.current().getBeanManager();
        Bean<T> bean = (Bean<T>) bm.getBeans(name).iterator().next();
        CreationalContext<T> ctx = bm.createCreationalContext(bean);
        return (T) bm.getReference(bean, clazz, ctx);
    }
    
    public String getEclipseLinkProfileWeight() {
        return eclipseLinkProfileWeight;
    }
}
