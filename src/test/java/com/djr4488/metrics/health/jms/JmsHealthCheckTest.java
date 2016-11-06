package com.djr4488.metrics.health.jms;

import com.codahale.metrics.health.HealthCheck;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.jms.*;
import javax.management.RuntimeErrorException;

import static org.mockito.Mockito.when;

/**
 * Created by djr4488 on 11/6/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class JmsHealthCheckTest extends TestCase {
    @Mock
    private ConnectionFactory connectionFactory;
    @Mock
    private Connection conn;
    @Mock
    private Session session;
    @Mock
    private TemporaryQueue tempQueue;
    @Mock
    private MessageProducer msgProducer;
    @Mock
    private MessageConsumer msgConsumer;
    @Mock
    private TextMessage textMessage;
    @InjectMocks
    private JMSHealthCheck jmsHealthCheck = new JMSHealthCheck();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testJmsHealthCheckSuccess() {
        try {
            when(connectionFactory.createConnection()).thenReturn(conn);
            when(conn.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
            when(session.createTemporaryQueue()).thenReturn(tempQueue);
            when(session.createProducer(tempQueue)).thenReturn(msgProducer);
            when(session.createConsumer(tempQueue)).thenReturn(msgConsumer);
            when(msgConsumer.receive(5000)).thenReturn(textMessage);
            when(textMessage.getText()).thenReturn("Test message sent");
            assertEquals(HealthCheck.Result.healthy(), jmsHealthCheck.check());
        } catch (Exception ex) {
            fail("did not expect exceptions here");
        }
    }

    @Test
    public void testJmsHealthCheckTimeout() {
        try {
            when(connectionFactory.createConnection()).thenReturn(conn);
            when(conn.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
            when(session.createTemporaryQueue()).thenReturn(tempQueue);
            when(session.createProducer(tempQueue)).thenReturn(msgProducer);
            when(session.createConsumer(tempQueue)).thenReturn(msgConsumer);
            when(msgConsumer.receive(5000)).thenReturn(textMessage);
            when(textMessage.getText()).thenReturn("Test message sent");
            when(textMessage.getText()).thenAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation){
                    try {
                        Thread.sleep(6000);
                        return "ABCD1234";
                    } catch (Exception ex) {
                        return "Test message sent";
                    }
                }
            });
            assertEquals(HealthCheck.Result.unhealthy("Did not receive testMessage via tempQueue in 5000 milliseconds"),
                    jmsHealthCheck.check());
        } catch (Exception ex) {
            fail("did not expect exceptions here");
        }
    }

    @Test
    public void testJmsHealthCheckCreateConnectionFails() {
        try {
            JMSException ex = new JMSException("test");
            when(connectionFactory.createConnection()).thenThrow(ex);
            jmsHealthCheck.check();
            fail("expected failure");
        } catch (Exception ex1) {
            assertTrue(ex1 instanceof JMSException);
            assertEquals("test", ex1.getMessage());
        }
    }
}
