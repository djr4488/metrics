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
import com.djr4488.metrics.config.MetricsRegistryBeanConfig;
import com.djr4488.metrics.reporters.ReporterBean;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
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
    private Boolean enableSlf4jReporter;
    private List<String> slf4jReporterNames;
    private Boolean enableScheduledReporters;
    private List<String> scheduledReporterNames;
    private Boolean enableJvmCapture;
    private List<String> healthCheckNamesToRegister;
    private String eclipseLinkProfileWeight;
    private Logger log;

    public MetricsRegistryBean() {
    }

    @PostConstruct
    public void postContruct() {
        this.log = LoggerFactory.getLogger(this.getClass());
        initializeConfiguration();
        initializeJvmMetrics();
        initializeHealthChecks(buildHealthChecksToRegisterMap());
        initializeReporters();
    }

    private void initializeConfiguration() {
        log.debug("initializeConfiguration() initializing configuration");
        MetricsRegistryBeanConfig cfg = ConfigFactory.create(MetricsRegistryBeanConfig.class);
        this.enableSlf4jReporter = cfg.enableSlf4jReporter();
        this.slf4jReporterNames = cfg.slf4jReporterNames();
        this.enableScheduledReporters = cfg.enableScheduledReporters();
        this.scheduledReporterNames = cfg.scheduledReporterNames();
        this.enableJvmCapture = cfg.enableJvmCapture();
        this.healthCheckNamesToRegister = cfg.healthCheckNamesToRegister();
        this.eclipseLinkProfileWeight = cfg.eclipseLinkProfileWeight();
    }

    private Map<String,HealthCheck> buildHealthChecksToRegisterMap() {
        log.debug("buildHealthChecksToRegisterMap() entered");
        Map<String, HealthCheck> healthCheckMap = new HashMap<>();
        for (String healthCheckName : healthCheckNamesToRegister) {
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
        log.debug("getHealthCheckBeanByName() healthCheckBeanName:{}", healthCheckBeanName);
        try {
            return MetricsRegistryBean.getBeanByNameOfClass(healthCheckBeanName, HealthCheck.class);
        } catch (Exception ex) {
            log.error("getHealthCheckBeanByName()", ex);
        }
        return null;
    }

    private void initializeJvmMetrics() {
        if (enableJvmCapture) {
            log.debug("initializeJvmMetrics() entered");
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
            initializeSlf4jReporters();
        }
        if (enableScheduledReporters) {
            initializeScheduledReporters();
        }
    }

    private void initializeSlf4jReporters() {
        for (String slf4jReporterName : slf4jReporterNames) {
            ReporterBean reporterBean = getReporterBeanByName(slf4jReporterName);
            if (reporterBean != null) {
                reporterBean.initialize(metricRegistry);
            }
        }
    }

    private void initializeScheduledReporters() {
        for (String scheduledReporterName : scheduledReporterNames) {
            ReporterBean reporterBean = getReporterBeanByName(scheduledReporterName);
            if (reporterBean != null) {
                reporterBean.initialize(metricRegistry);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ReporterBean getReporterBeanByName(String reporterBeanName) {
        log.debug("getReporterBeanByName() entered reporterBeanName:{}", reporterBeanName);
        try {
            return MetricsRegistryBean.getBeanByNameOfClass(reporterBeanName, ReporterBean.class);
        } catch (Exception ex) {
            log.error("getReporterBeanByName()", ex);
        }
        return null;
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
