package com.agent.tests.libs;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.DebugCallRecordTreePrinter;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.recorders.collections.CollectionsRecordingMode;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.Test;

import javax.jms.IllegalStateException;
import javax.jms.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

class ActiveMQRecordingTest extends AbstractInstrumentationTest {

    @Test
    void testProduceAndConsumerWithActiveMQ() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(ActiveMQTestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ActiveMQTestCase.main"))
                        .withInstrumentedPackages()
                        .withRecordConstructors()
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(DebugCallRecordTreePrinter.printTree(singleRoot), singleRoot.getSubtreeSize(), greaterThan(3000));
    }

    @Test
    void testProduceAndConsumerWithActiveMQWithCollections() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(ActiveMQTestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ActiveMQTestCase.main"))
                        .withInstrumentedPackages()
                        .withRecordConstructors()
                        .withRecordCollections(CollectionsRecordingMode.JDK)
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(DebugCallRecordTreePrinter.printTree(singleRoot), singleRoot.getSubtreeSize(), greaterThan(3000));
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
