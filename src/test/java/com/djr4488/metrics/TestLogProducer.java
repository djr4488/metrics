package com.djr4488.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Created by djr4488 on 11/6/16.
 */
@Alternative
public class TestLogProducer {
    @Produces
    public Logger getLogger(InjectionPoint injectionPoint) {
        String className = injectionPoint.getClass().getSimpleName();
        return LoggerFactory.getLogger(className);
    }
}
