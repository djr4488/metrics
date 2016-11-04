package com.djr4488.metrics.reporters;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
import org.apache.deltaspike.core.api.config.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Created by djr4488 on 11/3/16.
 */
@ApplicationScoped
@Named("influxReporterBean")
public class InfluxReporterBean implements ReporterBean {
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
    @ConfigProperty(name="applicationName", defaultValue = "appName")
    private String appName;
    @Inject
    @ConfigProperty(name="clusterName", defaultValue = "clusterName")
    private String clusterName;
    @Inject
    @ConfigProperty(name="influxReportFrequency", defaultValue = "30")
    private Integer influxReportFrequency;
    private ScheduledReporter influxReporter;
    @Override
    public void initialize(MetricRegistry metricRegistry) {
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

    private String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhEx) {
            return appName;
        }
    }
}
