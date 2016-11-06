package com.djr4488.metrics.health.database;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.naming.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by djr4488 on 11/6/16.
 */
@ApplicationScoped
@Named("databaseHealthCheckHelper")
public class DatabaseHealthCheckHelper {
    public Map<String, Object> getEntityManagerFactoryMap(String contextLookupKey)
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
