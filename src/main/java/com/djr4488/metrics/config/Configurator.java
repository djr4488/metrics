package com.djr4488.metrics.config;

import org.aeonbits.owner.ConfigFactory;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by djr4488 on 11/6/16.
 */
@ApplicationScoped
public class Configurator {
    public <T> T getConfiguration(Class clazz) {
        return (T)ConfigFactory.create(clazz);
    }
}
