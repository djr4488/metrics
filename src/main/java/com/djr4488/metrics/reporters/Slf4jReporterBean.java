package com.djr4488.metrics.reporters;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.config.Slf4jReporterBeanConfig;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

/**
 * Created by djr4488 on 11/3/16.
 */
@ApplicationScoped
@Named("slf4jReporterBean")
public class Slf4jReporterBean implements ReporterBean {
    @Inject
    private Logger log;
    @Inject
    private Configurator configurator;
    private Slf4jReporter slf4jReporter;

    public void initialize(MetricRegistry metricRegistry) {
        Slf4jReporterBeanConfig cfg = getConfiguration();
        slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.TRACE)
                .build();
        slf4jReporter.start(cfg.slf4jReportFrequency(), TimeUnit.SECONDS);
    }

    public void stopReporter() {
        slf4jReporter.stop();
    }

    public void startReporter() {
        slf4jReporter.start(getConfiguration().slf4jReportFrequency(), TimeUnit.SECONDS);
    }

    private Slf4jReporterBeanConfig getConfiguration() {
        return configurator.getConfiguration(Slf4jReporterBeanConfig.class);
    }
}
