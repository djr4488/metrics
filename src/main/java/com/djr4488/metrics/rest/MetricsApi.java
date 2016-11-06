package com.djr4488.metrics.rest;

/**
 * Created by djr4488 on 9/30/16.
 */
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.json.HealthCheckModule;
import com.codahale.metrics.jvm.ThreadDump;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.djr4488.metrics.MetricsRegistryBean;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.SortedMap;

@ApplicationScoped
@Path("metrics")
public class MetricsApi {
    @Inject
    private Logger log;
    @Inject
    private MetricsRegistryBean metricsRegistryBean;
    private transient ObjectMapper mapper = new ObjectMapper().registerModule(new HealthCheckModule());

    @Path("metrics")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MetricRegistry metrics() {
        log.info("metrics() entered");
        return metricsRegistryBean.getMetricRegistry();
    }

    @Path("threadDump")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String threadDump()
    throws IOException {
        log.info("threadDump() entered");
        try (OutputStream output = new ByteArrayOutputStream()) {
            ThreadDump threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
            threadDump.dump(output);
            return output.toString();
        }
    }

    @Path("ping")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        log.info("ping() entered");
        return "pong";
    }

    @Path("health")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response health()
            throws IOException {
        log.info("health() entered");
        SortedMap<String, HealthCheck.Result> healthCheckResults = metricsRegistryBean.getHealthCheckRegistry().runHealthChecks();
        if (healthCheckResults.isEmpty()) {
            return Response.ok("No health checks").build();
        } else {
            try (OutputStream output = new ByteArrayOutputStream()) {
                getWriter().writeValue(output, healthCheckResults);
                Response resp;
                if (isAllHealthy(healthCheckResults)) {
                    resp = Response.ok(output.toString()).build();
                } else {
                    resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(output.toString()).build();
                }
                return resp;
            }
        }
    }

    private ObjectWriter getWriter() {
        return mapper.writerWithDefaultPrettyPrinter();
    }

    private  boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        for (HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }
}

