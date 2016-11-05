package com.djr4488.metrics.reporters;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;

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
    private String influxUrl;
    @Inject
    private Integer influxPort;
    @Inject
    private String username;
    @Inject
    private String password;
    @Inject
    private String database;
    @Inject
    private String protocol;
    @Inject
    private String appName;
    @Inject
    private String clusterName;
    @Inject
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
