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
import org.eclipse.persistence.sessions.SessionProfilerAdapter;
import org.eclipse.persistence.tools.profiler.Profile;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EclipseLinkProfiler extends SessionProfilerAdapter implements Serializable, Cloneable {
    protected List<Profile> profiles;
    protected transient AbstractSession session;
    protected boolean shouldLogProfile;
    protected int nestLevel;
    protected long nestTime;
    protected long profileTime;
    protected Map<Integer, Map<String, Long>> operationTimingsByThread;
    protected Map<Integer, Map<String, Long>> operationStartTimesByThread;
    private MetricsRegistryBean metricsRegistryBean;

    public EclipseLinkProfiler() {
        this(false);
    }

    /** @deprecated */
    public EclipseLinkProfiler(Session session) {
        this(session, false);
    }

    /** @deprecated */
    public EclipseLinkProfiler(Session session, boolean shouldLogProfile) {
        this.profiles = new Vector();
        this.session = (AbstractSession)session;
        this.shouldLogProfile = false;
        this.nestLevel = 0;
        this.operationTimingsByThread = new Hashtable();
        this.operationStartTimesByThread = new Hashtable();
        this.shouldLogProfile = false;
    }

    public EclipseLinkProfiler(boolean shouldLogProfile) {
        this.profiles = new Vector();
        this.shouldLogProfile = false;
        this.nestLevel = 0;
        this.profileTime = 0L;
        this.nestTime = 0L;
        this.operationTimingsByThread = new Hashtable();
        this.operationStartTimesByThread = new Hashtable();
        metricsRegistryBean = this.getMetricsRegistryBean();
    }

    protected void addProfile(Profile profile) {
        this.getProfiles().add(profile);
    }

    public EclipseLinkProfiler clone() {
        try {
            return (EclipseLinkProfiler)super.clone();
        } catch (CloneNotSupportedException var1) {
            throw new InternalError();
        }
    }

    public void endOperationProfile(String operationName, DatabaseQuery databaseQuery) {
        Long endTime = System.nanoTime();
        Long startTime = (Long)this.getOperationStartTimes().get(operationName);
        String query = databaseQuery.getSQLString();
        String logQuery;
        metricsRegistryBean = getMetricsRegistryBean();
        long time = endTime - startTime.longValue();
        if(this.getNestLevel() == 0) {
            if(time == 0L) {
                return;
            }
            Profile totalTime = new Profile();
            totalTime.setTotalTime(time);
            totalTime.setLocalTime(time);
            totalTime.addTiming(operationName, time);
            this.addProfile(totalTime);
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
    }

    public void endOperationProfile(String operationName, DatabaseQuery query, int weight) {
        this.endOperationProfile(operationName, query);
    }

    protected int getNestLevel() {
        return this.nestLevel;
    }

    protected long getNestTime() {
        return this.nestTime;
    }

    protected Map<String, Long> getOperationStartTimes() {
        Integer threadId = Integer.valueOf(Thread.currentThread().hashCode());
        if(this.getOperationStartTimesByThread().get(threadId) == null) {
            this.getOperationStartTimesByThread().put(threadId, new Hashtable(10));
        }

        return (Map)this.getOperationStartTimesByThread().get(threadId);
    }

    protected Map<Integer, Map<String, Long>> getOperationStartTimesByThread() {
        return this.operationStartTimesByThread;
    }

    protected Map<String, Long> getOperationTimings() {
        Integer threadId = Integer.valueOf(Thread.currentThread().hashCode());
        if(this.getOperationTimingsByThread().get(threadId) == null) {
            this.getOperationTimingsByThread().put(threadId, new Hashtable(10));
        }

        return (Map)this.getOperationTimingsByThread().get(threadId);
    }

    protected Map<Integer, Map<String, Long>> getOperationTimingsByThread() {
        return this.operationTimingsByThread;
    }

    public List<Profile> getProfiles() {
        return this.profiles;
    }

    protected long getProfileTime() {
        return this.profileTime;
    }

    public AbstractSession getSession() {
        return this.session;
    }

    public Object profileExecutionOfQuery(DatabaseQuery query, Record row, AbstractSession session) {
        long profileStartTime = System.nanoTime();
        long nestedProfileStartTime = this.getProfileTime();
        Profile profile = new Profile();
        profile.setQueryClass(query.getClass());
        profile.setDomainClass(query.getReferenceClass());
        Object result = null;
        try {
            this.setNestLevel(this.getNestLevel() + 1);
            long startNestTime = this.getNestTime();
            Map timingsBeforeExecution = (Map) ((Hashtable) this.getOperationTimings()).clone();
            Map startTimingsBeforeExecution = (Map) ((Hashtable) this.getOperationStartTimes()).clone();
            long startTime = System.nanoTime();

            Object var18;
            try {
                result = session.internalExecuteQuery(query, (AbstractRecord) row);
                var18 = result;
            } finally {
                long endTime = System.nanoTime();
                this.setNestLevel(this.getNestLevel() - 1);

                String profileEndTime;
                long operationTime;
                for (Iterator var22 = this.getOperationTimings().keySet().iterator(); var22.hasNext(); profile.addTiming(profileEndTime, operationTime)) {
                    profileEndTime = (String) var22.next();
                    Long totalTimeIncludingProfiling = (Long) timingsBeforeExecution.get(profileEndTime);
                    long operationEndTime = ((Long) this.getOperationTimings().get(profileEndTime)).longValue();
                    if (totalTimeIncludingProfiling != null) {
                        operationTime = operationEndTime - totalTimeIncludingProfiling.longValue();
                    } else {
                        operationTime = operationEndTime;
                    }
                }

                profile.setTotalTime(endTime - startTime - (this.getProfileTime() - nestedProfileStartTime));
                profile.setLocalTime(profile.getTotalTime() - (this.getNestTime() - startNestTime));
                if (result instanceof Collection) {
                    profile.setNumberOfInstancesEffected((long) ((Collection) result).size());
                } else {
                    profile.setNumberOfInstancesEffected(1L);
                }

                this.addProfile(profile);
                long profileEndTime1;
                long totalTimeIncludingProfiling1;

                if (this.getNestLevel() == 0) {
                    this.setNestTime(0L);
                    this.setProfileTime(0L);
                    this.setOperationTimings(new Hashtable());
                    this.setOperationStartTimes(new Hashtable());
                    profileEndTime1 = System.nanoTime();
                    totalTimeIncludingProfiling1 = profileEndTime1 - profileStartTime;
                    profile.setProfileTime(totalTimeIncludingProfiling1 - profile.getTotalTime());
                } else {
                    this.setNestTime(startNestTime + profile.getTotalTime());
                    this.setOperationTimings(timingsBeforeExecution);
                    this.setOperationStartTimes(startTimingsBeforeExecution);
                    profileEndTime1 = System.nanoTime();
                    totalTimeIncludingProfiling1 = profileEndTime1 - profileStartTime;
                    this.setProfileTime(this.getProfileTime() + (totalTimeIncludingProfiling1 - (endTime - startTime)));
                    profile.setProfileTime(totalTimeIncludingProfiling1 - profile.getTotalTime());
                    Iterator operationTime1 = ((Map) ((Hashtable) startTimingsBeforeExecution).clone()).keySet().iterator();

                    while (operationTime1.hasNext()) {
                        String timingName = (String) operationTime1.next();
                        startTimingsBeforeExecution.put(timingName, Long.valueOf(((Number) startTimingsBeforeExecution.get(timingName)).longValue() + totalTimeIncludingProfiling1));
                    }
                }
            }
            return var18;
        } catch (Exception ex) {
            return result;
        }
    }

    protected void setNestLevel(int nestLevel) {
        this.nestLevel = nestLevel;
    }

    protected void setNestTime(long nestTime) {
        this.nestTime = nestTime;
    }

    protected void setOperationStartTimes(Map<String, Long> operationStartTimes) {
        Integer threadId = Integer.valueOf(Thread.currentThread().hashCode());
        this.getOperationStartTimesByThread().put(threadId, operationStartTimes);
    }

    protected void setOperationTimings(Map<String, Long> operationTimings) {
        Integer threadId = Integer.valueOf(Thread.currentThread().hashCode());
        this.getOperationTimingsByThread().put(threadId, operationTimings);
    }

    protected void setProfileTime(long profileTime) {
        this.profileTime = profileTime;
    }

    public void setSession(Session session) {
        this.session = (AbstractSession)session;
    }

    public void startOperationProfile(String operationName) {
        this.getOperationStartTimes().put(operationName, Long.valueOf(System.nanoTime()));
    }

    public void startOperationProfile(String operationName, DatabaseQuery query, int weight) {
        this.startOperationProfile(operationName);
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
                BeanManager bm = CDI.current().getBeanManager();
                Bean<MetricsRegistryBean> bean = (Bean<MetricsRegistryBean>) bm.getBeans("metricsRegistryBean").iterator().next();
                CreationalContext<MetricsRegistryBean> ctx = bm.createCreationalContext(bean);
                metricsRegistryBean = (MetricsRegistryBean) bm.getReference(bean, MetricsRegistryBean.class, ctx);
                return metricsRegistryBean;
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        return metricsRegistryBean;
    }
}
