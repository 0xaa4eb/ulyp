package com.agent.tests.libs;

import javax.jms.*;
import javax.jms.IllegalStateException;

import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.DebugCallRecordTreePrinter;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ActiveMQRecordingTest extends AbstractInstrumentationTest {

    @Test
    public void testProduceAndConsumerWithActiveMQ() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(ActiveMQTestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ActiveMQTestCase.main"))
                        .withInstrumentedPackages()
                        .withRecordConstructors()
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(
                DebugCallRecordTreePrinter.printTree(singleRoot),
                singleRoot.getSubtreeSize(),
                greaterThan(3000)
        );
    }

    @Test
    public void testProduceAndConsumerWithActiveMQWithCollections() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(ActiveMQTestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ActiveMQTestCase.main"))
                        .withInstrumentedPackages()
                        .withRecordConstructors()
                        .withRecordCollections(CollectionsRecordingMode.JAVA)
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(
                DebugCallRecordTreePrinter.printTree(singleRoot),
                singleRoot.getSubtreeSize(),
                greaterThan(3000)
        );
    }

    public static class ActiveMQTestCase {

        private static final int messageCount = 500;

        public static void main(String[] args) throws Exception {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("TEST_QUEUE");

            sendMessages(session, queue, messageCount);
            readMessages(session, queue, messageCount);

            session.close();
            connection.close();
        }

        private static void readMessages(Session session, Queue queue, int messageCount) throws JMSException {
            MessageConsumer consumer = session.createConsumer(queue);
            for (int i = 0; i < messageCount; i++) {
                Message message = consumer.receive(5000);
                if (message == null) {
                    throw new IllegalStateException("There must be a message in the queue! Something is wrong");
                }
                String text = ((ActiveMQTextMessage) message).getText();
                if (!text.equals("Text message 123")) {
                    throw new IllegalStateException("Message has wrong content");
                }
            }
        }

        private static void sendMessages(Session session, Queue queue, int messageCount) throws JMSException {
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            for (int i = 0; i < messageCount; i++) {
                ActiveMQTextMessage msg = new ActiveMQTextMessage();
                msg.setText("Text message 123");
                producer.send(msg);
            }
            producer.close();
        }
    }
}
