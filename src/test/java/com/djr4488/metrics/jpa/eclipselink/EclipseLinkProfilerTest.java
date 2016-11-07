package com.djr4488.metrics.jpa.eclipselink;

import com.djr4488.metrics.MetricsRegistryBean;
import com.djr4488.metrics.TestConfiguratorProducer;
import com.djr4488.metrics.TestLogProducer;
import com.djr4488.metrics.config.Configurator;
import com.djr4488.metrics.config.EclipseLinkProfilerConfig;
import com.djr4488.metrics.config.MetricsRegistryBeanConfig;
import junit.framework.TestCase;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.sessions.SessionProfiler;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.mockito.Mockito.*;

/**
 * Created by djr4488 on 11/7/16.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({MetricsRegistryBean.class})
@ActivatedAlternatives({TestLogProducer.class,TestConfiguratorProducer.class})
public class EclipseLinkProfilerTest extends TestCase {
    @Inject
    private Configurator configurator;

    @Before
    public void setup() {
        MetricsRegistryBeanConfig mrbConfig = mock(MetricsRegistryBeanConfig.class);
        when(configurator.getConfiguration(MetricsRegistryBeanConfig.class)).thenReturn(mrbConfig);
        when(mrbConfig.enableSlf4jReporter()).thenReturn(Boolean.FALSE);
        when(mrbConfig.enableJvmCapture()).thenReturn(Boolean.FALSE);
        when(mrbConfig.enableScheduledReporters()).thenReturn(Boolean.FALSE);
    }

    @Test
    public void testGetSessionProfilerALL() {
        EclipseLinkProfiler elp = new EclipseLinkProfiler();
        EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
        when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
        when(elpConfig.eclipseLinkProfileWeight()).thenReturn("ALL");
        assertEquals(SessionProfiler.ALL, elp.getSessionProfiler());
    }

    @Test
    public void testGetSessionProfilerHEAVY() {
        EclipseLinkProfiler elp = new EclipseLinkProfiler();
        EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
        when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
        when(elpConfig.eclipseLinkProfileWeight()).thenReturn("HEAVY");
        assertEquals(SessionProfiler.HEAVY, elp.getSessionProfiler());
    }

    @Test
    public void testGetSessionProfilerNORMAL() {
        EclipseLinkProfiler elp = new EclipseLinkProfiler();
        EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
        when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
        when(elpConfig.eclipseLinkProfileWeight()).thenReturn("NORMAL");
        assertEquals(SessionProfiler.NORMAL, elp.getSessionProfiler());
    }

    @Test
    public void testGetSessionProfilersNONE() {
        EclipseLinkProfiler elp = new EclipseLinkProfiler();
        EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
        when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
        when(elpConfig.eclipseLinkProfileWeight()).thenReturn("NONE");
        assertEquals(SessionProfiler.NONE, elp.getSessionProfiler());
    }

    @Test
    public void testGetSessionProfilerDEFAULT() {
        EclipseLinkProfiler elp = new EclipseLinkProfiler();
        EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
        when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
        when(elpConfig.eclipseLinkProfileWeight()).thenReturn("");
        assertEquals(SessionProfiler.NONE, elp.getSessionProfiler());
    }

    @Test
    public void testOccuredALLProfileWeight() {
        EclipseLinkProfiler elp = new EclipseLinkProfiler();
        EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
        DatabaseQuery dbQuery = mock(DatabaseQuery.class);
        AbstractSession abstractSession = mock(AbstractSession.class);
        when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
        when(elpConfig.eclipseLinkProfileWeight()).thenReturn("ALL");
        when(dbQuery.getMonitorName()).thenReturn("MonName");
        elp.occurred("opNameToastyRoasty", dbQuery, abstractSession);
        verify(dbQuery, times(1)).getMonitorName();
    }

    @Test
    public void testOccureNONEProfileWeight() {
        EclipseLinkProfiler elp = new EclipseLinkProfiler();
        EclipseLinkProfilerConfig elpConfig = mock(EclipseLinkProfilerConfig.class);
        DatabaseQuery dbQuery = mock(DatabaseQuery.class);
        AbstractSession abstractSession = mock(AbstractSession.class);
        when(configurator.getConfiguration(EclipseLinkProfilerConfig.class)).thenReturn(elpConfig);
        when(elpConfig.eclipseLinkProfileWeight()).thenReturn("NONE");
        when(dbQuery.getMonitorName()).thenReturn("MonName");
        elp.occurred("opNameToastyRoasty", dbQuery, abstractSession);
        verify(dbQuery, times(0)).getMonitorName();
    }
}
