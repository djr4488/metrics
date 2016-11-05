package com.djr4488.metrics.health.database;

/**
 * Created by djr4488 on 9/30/16.
 */
import com.codahale.metrics.health.HealthCheck;
import com.djr4488.metrics.config.DatabaseHealthCheckConfig;
import org.aeonbits.owner.ConfigFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import java.util.Map;
import java.util.TreeMap;

@ApplicationScoped
@Named("databaseHealthCheck")
public class DatabaseHealthCheck extends HealthCheck {
    private static final String HEALTH_CHECK_QUERY = "SELECT 1";
    private String contextLookupKey;

    @Override
    protected Result check()
    throws Exception {
        initializeDatabaseHealthCheck();
        Map<String, Object> mapOfEMFs = getEntityManagerFactoryMap();
        for (Map.Entry<String, Object> nameToFactory : mapOfEMFs.entrySet()) {
            ((EntityManagerFactory) nameToFactory.getValue()).createEntityManager().createNativeQuery(HEALTH_CHECK_QUERY).getSingleResult();
        }
        return Result.healthy();
    }

    private synchronized void initializeDatabaseHealthCheck() {
        if (null != contextLookupKey) {
            DatabaseHealthCheckConfig cfg = ConfigFactory.create(DatabaseHealthCheckConfig.class);
            contextLookupKey = cfg.contextLookupKey();
        }
    }

    private Map<String, Object> getEntityManagerFactoryMap()
            throws NamingException {
        Context context = new InitialContext();
        return contextToMap((Context)context.lookup(contextLookupKey));
    }

    private static Map<String,Object> contextToMap(Context context) throws NamingException {
        Map<String, Object> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        contextToMap(context, "", map);
        return map;
    }

    private static void contextToMap(Context context, String baseName, Map<String,Object> results)
            throws NamingException {
        NamingEnumeration<Binding> namingEnumeration = context.listBindings("");
        while (namingEnumeration.hasMoreElements()) {
            Binding binding = namingEnumeration.nextElement();
            String name = binding.getName();
            String fullName = baseName + name;
            Object object = binding.getObject();
            results.put(fullName, object);
            if (object instanceof Context) {
                contextToMap((Context) object, fullName + "/", results);
            }
        }
    }
}

