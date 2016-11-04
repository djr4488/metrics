package com.djr4488.metrics;

/**
 * Created by djr4488 on 9/30/16.
 */
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
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
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Named("metricsRegistryBean")
public class MetricsRegistryBean {
    @Produces
    @ApplicationScoped
    private MetricRegistry metricRegistry = new MetricRegistry();
    private HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
    private Slf4jReporter slf4jReporter;
    private ScheduledReporter influxReporter;
    @Inject
    @ConfigProperty(name="influxUrl", defaultValue = "influxUrl")
    private String influxUrl;
    @Inject
    @ConfigProperty(name="influxPort", defaultValue = "7000")
    private Integer influxPort;
    @Inject
    @ConfigProperty(name="influxUsername", defaultValue = "username")
    private String username;
    @Inject
    @ConfigProperty(name="influxPassword", defaultValue = "password")
    private String password;
    @Inject
    @ConfigProperty(name="influxDatabaseSchema", defaultValue = "database")
    private String database;
    @Inject
    @ConfigProperty(name="influxProtocol", defaultValue = "protocol")
    private String protocol;
    @Inject
    @ConfigProperty(name="slf4jReportFrequency", defaultValue = "30")
    private Integer slf4jReportFrequency;
    @Inject
    @ConfigProperty(name="influxReportFrequency", defaultValue = "30")
    private Integer influxReportFrequency;
    @Inject
    @ConfigProperty(name="enableSlf4jReporter", defaultValue = "true")
    private Boolean enableSlf4jReporter;
    @Inject
    @ConfigProperty(name="enableInfluxReporter", defaultValue = "false")
    private Boolean enableInfluxReporter;
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
    @ConfigProperty(name="applicationName", defaultValue = "appName")
    private String appName;
    @Inject
    @ConfigProperty(name="clusterName", defaultValue = "clusterName")
    private String clusterName;
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
        initializeSlf4jReporter();
        initializeMethodScheduledReporter();
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

    private void initializeSlf4jReporter() {
        if (enableSlf4jReporter) {
            slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
                    .outputTo(log)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .withLoggingLevel(Slf4jReporter.LoggingLevel.TRACE)
                    .build();
            slf4jReporter.start(slf4jReportFrequency, TimeUnit.SECONDS);
        }
    }

    private void initializeMethodScheduledReporter() {
        if (enableInfluxReporter) {
            influxReporter = InfluxdbReporter.forRegistry(metricRegistry)
                    .protocol(new HttpInfluxdbProtocol(protocol, influxUrl, influxPort, username, password, database))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .skipIdleMetrics(false)
                    .tag("appName", appName)
                    .tag("server", getLocalHostName())
                    .tag("cluster", clusterName)
                    .transformer(new CategoriesMetricMeasurementTransformer("module", "artifact"))
                    .build();
            influxReporter.start(influxReportFrequency, TimeUnit.SECONDS);
        }
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    private String getLocalHostName() {
        try {
            address = InetAddress.getLocalHost();
            return address.getHostName();
        } catch (UnknownHostException uhEx) {
            return appName;
        }
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
