package com.agent.tests.libs;

import com.agent.tests.util.*;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.CallRecord;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import javax.jms.*;
import javax.jms.IllegalStateException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.agent.tests.util.RecordingMatchers.*;
import static org.hamcrest.Matchers.allOf;
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
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(
                DebugCallRecordTreePrinter.printTree(singleRoot),
                singleRoot.getSubtreeSize(),
                greaterThan(5000)
        );
    }

    public static class ActiveMQTestCase {

        public static void main(String[] args) throws Exception {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
                Connection connection = connectionFactory.createConnection();
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue("TEST_QUEUE");

                MessageProducer producer = session.createProducer(queue);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                for (int i = 0; i < 10; i++) {
                    ActiveMQTextMessage msg = new ActiveMQTextMessage();
                    msg.setText("Text message 123");
                    producer.send(msg);
                }
                producer.close();

                MessageConsumer consumer = session.createConsumer(queue);
                for (int i = 0; i < 10; i++) {
                    Message message = consumer.receive(5000);
                    if (message == null) {
                        throw new IllegalStateException("There must be a message in the queue! Something is wrong");
                    }
                    String text = ((ActiveMQTextMessage) message).getText();
                    if (!text.equals("Text message 123")) {
                        throw new IllegalStateException("Message has wrong content");
                    }
                }
                session.close();
                connection.close();
            } finally {
                System.exit(0);
            }
        }
    }
}
