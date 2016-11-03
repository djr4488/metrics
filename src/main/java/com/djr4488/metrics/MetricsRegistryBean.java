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
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
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
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
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
    private String influxUrl;
    @Inject
    private Integer port;
    @Inject
    private String user;
    @Inject
    private String password;
    @Inject
    private String database;
    @Inject
    private String protocol;
    @Inject
    private Integer slf4jReportFrequency;
    @Inject
    private Integer influxReportFrequency;
    @Inject
    private Boolean enableSlf4jReporter;
    @Inject
    private Boolean enableInfluxReporter;
    @Inject
    private Boolean enableJvmCapture;
    @Inject
    private List<String> healthCheckNamesToRegister;
    @Inject
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

    private void initializeJvmMetrics() {
        if (enableJvmCapture) {
            metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
            metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
            metricRegistry.register("jvm.thread-states", new ThreadStatesGaugeSet());
            metricRegistry.register("jvm.class-loading", new ClassLoadingGaugeSet());
            metricRegistry.register("jvm.fd.usage", new FileDescriptorRatioGauge());
            metricRegistry.register("jvm.cached-thread-states",
                    new CachedThreadStatesGaugeSet(ManagementFactory.getThreadMXBean(), new ThreadDeadlockDetector(), 30, TimeUnit.SECONDS));
        }
    }

    private Map<String,HealthCheck> buildHealthChecksToRegisterMap() {
        Map<String, HealthCheck> healthCheckMap = new HashMap<>();
        for (String healthCheckName : healthCheckNamesToRegister) {
            healthCheckMap.put(healthCheckName, getHealthCheckBeanByName(healthCheckName));
        }
        return healthCheckMap;
    }

    @SuppressWarnings("unchecked")
    public HealthCheck getHealthCheckBeanByName(String healthCheckBeanName) {
        try {
            return MetricsRegistryBean.getBeanByNameOfClass(healthCheckBeanName, HealthCheck.class);
        } catch(Exception ex) {
            log.error("getHealthCheckBeanByName() for {} with ex:{}", healthCheckBeanName, ex);
            return null;
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
                    .build();
            slf4jReporter.start(slf4jReportFrequency, TimeUnit.SECONDS);
        }
    }

    private void initializeMethodScheduledReporter() {
        if (enableInfluxReporter) {
            influxReporter = InfluxdbReporter.forRegistry(metricRegistry)
                    .protocol(new HttpInfluxdbProtocol(protocol, influxUrl, port, user, password, database))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .skipIdleMetrics(false)
                    .tag("appName", System.getenv("VIRTUAL_HOST"))
                    .tag("registryType", "method")
                    .tag("server", getLocalHostName())
                    .tag("cluster", System.getenv("AWS_CLUSTER"))
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
            return System.getenv("VIRTUAL_HOST");
        }
    }

    public static <T> T getBeanByNameOfClass(String name, Class<T> clazz)
    throws Exception {
        T cdiBean;
        BeanManager bm = CDI.current().getBeanManager();
        Bean<T> bean = (Bean<T>) bm.getBeans(name).iterator().next();
        CreationalContext<T> ctx = bm.createCreationalContext(bean);
        return (T) bm.getReference(bean, clazz, ctx);
    }

    public String getEclipseLinkProfileWeight() {
        return eclipseLinkProfileWeight;
    }
}
