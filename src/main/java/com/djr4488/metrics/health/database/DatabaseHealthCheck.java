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
import javax.persistence.EntityManagerFactory;
import java.util.Map;

@ApplicationScoped
@Named("databaseHealthCheck")
public class DatabaseHealthCheck extends HealthCheck {
    private static final String HEALTH_CHECK_QUERY = "SELECT 1";
    @Inject
    private DatabaseHealthCheckHelper databaseHealthCheckHelper;
    @Inject
    private Configurator configurator;

    @Override
    protected Result check()
    throws Exception {
        Map<String, Object> mapOfEMFs = databaseHealthCheckHelper.getEntityManagerFactoryMap(getContextLookupKeyFromConfig());
        for (Map.Entry<String, Object> nameToFactory : mapOfEMFs.entrySet()) {
            ((EntityManagerFactory) nameToFactory.getValue()).createEntityManager().createNativeQuery(HEALTH_CHECK_QUERY).getSingleResult();
        }
        return Result.healthy();
    }

    private String getContextLookupKeyFromConfig() {
        DatabaseHealthCheckConfig cfg = configurator.getConfiguration(DatabaseHealthCheckConfig.class);
        return cfg.contextLookupKey();
    }
}

