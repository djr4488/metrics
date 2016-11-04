package com.djr4488.metrics.jpa.eclipselink;

/**
 * Created by djr4488 on 9/30/16.
 * Credit is provided to EclispeLink PerformanceProfiler author(James Sutherland?), which is where most of this code came from.
 */
import com.codahale.metrics.Timer;
import com.djr4488.metrics.MetricsRegistryBean;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.eclipse.persistence.sessions.SessionProfilerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class EclipseLinkProfiler extends SessionProfilerAdapter implements Serializable, Cloneable {
    protected static final String COUNTER = "Counter:";
    protected static final String TIMER = "Timer:";
    transient protected AbstractSession session;
    protected Map<String, Object> operationTimings;
    protected Map<Integer, Map<String, Long>> operationStartTimesByThread;//facilitates concurrency
    protected int profileWeight;
    private MetricsRegistryBean metricsRegistryBean;
    private static final Logger log = LoggerFactory.getLogger("EclipseLinkProfiler");

    public EclipseLinkProfiler() {
        this.operationTimings = new ConcurrentHashMap();
        this.operationStartTimesByThread = new ConcurrentHashMap();
        this.profileWeight = SessionProfiler.ALL;
        //this.metricsRegistryBean = this.getMetricsRegistryBean();
    }

    public long getDumpTime() {
        //do nothing
        return 60000;
    }

    public void setDumpTime(long dumpTime) {
        //do nothing
    }

    public EclipseLinkProfiler clone() {
        try {
            return (EclipseLinkProfiler)super.clone();
        } catch (CloneNotSupportedException exception) {
            throw new InternalError();
        }
    }

    public void checkDumpTime() {
        //do nothing
    }

    public void dumpResults() {
        //do nothing
    }

    public void endOperationProfile(String operationName, DatabaseQuery databaseQuery) {
        if (getSessionProfiler() < SessionProfiler.HEAVY) {
            return;
        }
        Long endTime = System.nanoTime();
        Long startTime = (Long)this.getOperationStartTimes().get(operationName);
        String query = databaseQuery.getSQLString();
        String logQuery;
        metricsRegistryBean = getMetricsRegistryBean();
        long time = endTime - startTime.longValue();
        if (null != metricsRegistryBean && null != startTime && !query.contains("LAST_INSERT_ID")
                && !query.contains("SELECT 1")) {
            if (query.contains("SELECT") && query.contains(" FROM ")) {
                logQuery = formatQuery(query);
            } else {
                logQuery = query;
            }
            Timer queryTimer = metricsRegistryBean.getMetricRegistry().timer(operationName + ":" + logQuery);
            queryTimer.update(endTime - startTime, TimeUnit.NANOSECONDS);
        }
        Long totalTime1 = (Long)this.getOperationTimings().get(operationName);
        if(totalTime1 == null) {
            this.getOperationTimings().put(operationName, Long.valueOf(time));
        } else {
            this.getOperationTimings().put(operationName, Long.valueOf(totalTime1.longValue() + time));
        }
    }

    public void endOperationProfile(String operationName, DatabaseQuery query, int weight) {
        if (getSessionProfiler() < weight) {
            return;
        }
        this.endOperationProfile(operationName, query);
    }

    protected Map<String, Long> getOperationStartTimes() {
        Integer threadId = Integer.valueOf(Thread.currentThread().hashCode());
        Map<String, Long> times = this.operationStartTimesByThread.get(threadId);
        if (times == null) {
            times = new Hashtable<String, Long>();
            this.operationStartTimesByThread.put(threadId, times);
        }
        return times;
    }

    protected Map<Integer, Map<String, Long>> getOperationStartTimesByThread() {
        return operationStartTimesByThread;
    }

    public Object getOperationTime(String operation) {
        return this.operationTimings.get(operation);
    }

    public Map<String, Object> getOperationTimings() {
        return operationTimings;
    }

    public AbstractSession getSession() {
        return this.session;
    }

    public Object profileExecutionOfQuery(DatabaseQuery query, Record row, AbstractSession session) {
        if (getSessionProfiler() < SessionProfiler.HEAVY) {
            return session.internalExecuteQuery(query, (AbstractRecord)row);
        }
        startOperationProfile(TIMER + query.getMonitorName());
        startOperationProfile(TIMER + query.getClass().getSimpleName());
        occurred(COUNTER + query.getClass().getSimpleName(), session);
        occurred(COUNTER + query.getMonitorName(), session);
        try {
            return session.internalExecuteQuery(query, (AbstractRecord)row);
        } finally {
            endOperationProfile(TIMER + query.getMonitorName());
            endOperationProfile(TIMER + query.getClass().getSimpleName());
            checkDumpTime();
        }
    }

    public void setSession(Session session) {
        this.session = (AbstractSession)session;
    }

    public void startOperationProfile(String operationName) {
        getOperationStartTimes().put(operationName, Long.valueOf(System.nanoTime()));
    }

    public void startOperationProfile(String operationName, DatabaseQuery query, int weight) {
        if (getSessionProfiler() < weight) {
            return;
        }
        startOperationProfile(operationName);
        if (query != null) {
            startOperationProfile(TIMER + query.getMonitorName() + ":" + operationName.substring(TIMER.length(), operationName.length()));
        }
    }

    public void update(String operationName, Object value) {
        this.operationTimings.put(operationName, value);
    }

    public void occurred(String operationName, AbstractSession session) {
        if (getSessionProfiler() < SessionProfiler.NORMAL) {
            return;
        }
        synchronized (this.operationTimings) {
            Long occurred = (Long)this.operationTimings.get(operationName);
            if (occurred == null) {
                this.operationTimings.put(operationName, Long.valueOf(1));
            } else {
                this.operationTimings.put(operationName, Long.valueOf(occurred.longValue() + 1));
            }
        }
    }

    public void occurred(String operationName, DatabaseQuery query, AbstractSession session) {
        if (getSessionProfiler() < SessionProfiler.NORMAL) {
            return;
        }
        occurred(operationName, session);
        occurred(COUNTER + query.getMonitorName() + ":" + operationName.substring(COUNTER.length(), operationName.length()), session);
    }

    public int getProfileWeight() {
        return -1;
    }

    public void initialize() {
    }

    private String formatQuery(String query) {
        int indexBegin = 7;
        int indexEnd = query.indexOf(" FROM ");
        return query.replace(query.substring(indexBegin, indexEnd), "*");
    }

    @SuppressWarnings("unchecked")
    public MetricsRegistryBean getMetricsRegistryBean() {
        if (null == metricsRegistryBean) {
            try {
                metricsRegistryBean = MetricsRegistryBean.getBeanByNameOfClass("metricsRegistryBean", MetricsRegistryBean.class);
            } catch (Exception ex) {
                log.error("getMetricsRegistryBean()", ex);
            }
        }
        return metricsRegistryBean;
    }

    public int getSessionProfiler() {
        switch (getMetricsRegistryBean().getEclipseLinkProfileWeight()) {
            case "ALL":
                return SessionProfiler.ALL;
            case "HEAVY":
                return SessionProfiler.HEAVY;
            case "NORMAL":
                return SessionProfiler.NORMAL;
            case "NONE":
                return SessionProfiler.NONE;
            default:
                return SessionProfiler.ALL;
        }
    }
}
