package com.djr4488.metrics.health.database;

/**
 * Created by djr4488 on 9/30/16.
 */
import com.codahale.metrics.health.HealthCheck;
import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.config.DatabaseHealthCheckConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Persistence;
import java.util.List;

@ApplicationScoped
@Named("databaseHealthCheck")
public class DatabaseHealthCheck extends HealthCheck {
    @Inject
    private Configurator configurator;

    @Override
    protected Result check()
    throws Exception {
        List<String> persistenceUnitNames = getConfiguration().persistenceUnitNames();
        List<String> testSqlByPersistenceName = getConfiguration().testSqlByPersistenceName();
        int index = 0;
        for (String persistenceUnitName : persistenceUnitNames) {
            Persistence.createEntityManagerFactory(persistenceUnitName).createEntityManager()
                    .createNativeQuery(testSqlByPersistenceName.get(index)).getSingleResult();
            index++;
        }
        return Result.healthy();
    }

    private DatabaseHealthCheckConfig getConfiguration() {
        return configurator.getConfiguration(DatabaseHealthCheckConfig.class);
    }
}

