package com.djr4488.metrics.health.jms;

/**
 * Created by djr4488 on 9/30/16.
 */
import com.codahale.metrics.health.HealthCheck;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

@ApplicationScoped
@Named("jmsHealthCheck")
public class JMSHealthCheck extends HealthCheck {
    private static final Integer millisToWait = 5000;
    @Resource(name = "jms/connectionFactory" )
    private ConnectionFactory factory;

    @Override
    protected Result check()
    throws Exception {
        Connection conn = factory.createConnection();
        conn.start();
        try {
            try (Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)){
                TemporaryQueue tempQueue = session.createTemporaryQueue();
                try {
                    try (MessageProducer msgProducer = session.createProducer(tempQueue)){
                        msgProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                        final String messageText = "Test message sent";
                        msgProducer.send(tempQueue, session.createTextMessage(messageText));
                        try (MessageConsumer consumer = session.createConsumer(tempQueue)){
                            TextMessage receivedMessage = (TextMessage)consumer.receive(millisToWait);
                            if (null != receivedMessage && messageText.equals(receivedMessage.getText())) {
                                return Result.healthy();
                            } else {
                                return Result.unhealthy("Did not receive testMessage via tempQueue in " +
                                        millisToWait + " milliseconds");
                            }
                        }
                    }
                } finally {
                    tempQueue.delete();
                }
            }
        } finally {
            conn.close();
        }
    }
}

