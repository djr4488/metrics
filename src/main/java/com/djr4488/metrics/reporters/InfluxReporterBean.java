package com.djr4488.metrics.reporters;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.config.InfluxReporterBeanConfig;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;

import javax.ejb.Schedule;
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
    private Configurator configurator;
    private ScheduledReporter influxReporter;

    @Override
    public void initialize(MetricRegistry metricRegistry) {
        InfluxReporterBeanConfig cfg = getConfiguration();
        influxReporter = InfluxdbReporter.forRegistry(metricRegistry)
                .protocol(new HttpInfluxdbProtocol(cfg.protocol(), cfg.influxUrl(), cfg.influxPort(), cfg.username(), cfg.password(), cfg.database()))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .skipIdleMetrics(false)
                .tag("appName", cfg.appName())
                .tag("server", getLocalHostName())
                .tag("cluster", cfg.clusterName())
                .transformer(new CategoriesMetricMeasurementTransformer("module", "artifact"))
                .build();
        influxReporter.start(cfg.influxReportFrequency(), TimeUnit.SECONDS);

    }

    public void stopReporter() {
        influxReporter.stop();

    }

    public void startReporter() {
        influxReporter.start(getConfiguration().influxReportFrequency(), TimeUnit.SECONDS);
    }

    private String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhEx) {
            return getConfiguration().appName();
        }
    }

    private InfluxReporterBeanConfig getConfiguration() {
        return configurator.getConfiguration(InfluxReporterBeanConfig.class);
    }

    @Schedule(hour = "*", minute = "*/5")
    public void startStopReporter() {
        if (getConfiguration().enableInfluxReporter()) {
            startReporter();
        } else {
            stopReporter();
        }
    }
}
