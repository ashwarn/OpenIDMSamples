/*
 * Copyright 2016-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

/**
 * A Simple JMS Topic Consumer.
 */
public class SimpleConsumer {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Missing connection argument. For example: SimpleConsumer tcp://localhost:61616");
        }

        Hashtable<String, String> contextMap = new Hashtable<>();
        contextMap.put("java.naming.factory.initial", "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        contextMap.put("java.naming.provider.url", args[0]);
        contextMap.put("topic.audit", "audit");
        InitialContext context = new InitialContext(contextMap);

        Properties jmsConfig = new Properties();
        jmsConfig.put("connectionFactory", "ConnectionFactory");
        jmsConfig.put("topic", "audit");

        ConnectionFactory connectionFactory;
        try {
            connectionFactory = (ConnectionFactory) context.lookup(jmsConfig.getProperty("connectionFactory"));
            System.out.println("Connection factory=" + connectionFactory.getClass().getName());

            Connection connection = null;
            try {
                connection = connectionFactory.createConnection();

                // lookup topic
                String topicName = jmsConfig.getProperty("topic");
                Topic jmsTopic = (Topic) context.lookup(topicName);

                // create a new TopicSession for the client
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // create a new subscriber to receive messages
                MessageConsumer consumer = session.createConsumer(jmsTopic);
                consumer.setMessageListener(new MessageListener() {
                    public void onMessage(Message message) {
                        try {
                            if (message instanceof TextMessage) {
                                System.out.println("--------Message "
                                        + new SimpleDateFormat("E yyyy.MM.dd 'at' HH:mm:ss.SSS z").format(new Date())
                                        + "--------");
                                System.out.println(((TextMessage) message).getText());
                                System.out.println("----------------------------------------------------------");
                            } else {
                                System.out.println("--------Received a non-TextMessage--------");
                            }
                        } catch (JMSException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                });
                connection.start();
                System.out.println("READY, listening for messages. (Press 'Enter' to exit)");
                System.in.read();
            } finally {
                if (null != connection) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
}
