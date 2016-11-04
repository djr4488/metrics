package com.djr4488.metrics.reporters;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import org.apache.deltaspike.core.api.config.ConfigProperty;
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
    @ConfigProperty(name="slf4jReportFrequency", defaultValue = "30")
    private Integer slf4jReportFrequency;

    private Slf4jReporter slf4jReporter;

    public void initialize(MetricRegistry metricRegistry) {
        slf4jReporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .withLoggingLevel(Slf4jReporter.LoggingLevel.TRACE)
                .build();
        slf4jReporter.start(slf4jReportFrequency, TimeUnit.SECONDS);
    }
}
