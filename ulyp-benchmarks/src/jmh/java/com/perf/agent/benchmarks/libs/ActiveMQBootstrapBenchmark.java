package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.RecordingBenchmark;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.openjdk.jmh.annotations.*;

import javax.jms.IllegalStateException;
import javax.jms.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class ActiveMQBootstrapBenchmark extends RecordingBenchmark {

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Queue queue;

    public void setup() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue("TEST_QUEUE");
        session.close();
    }

    public void teardown() throws JMSException {
        connection.close();
    }

    public void sendMessages() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        for (int i = 0; i < 5000; i++) {
            ActiveMQTextMessage msg = new ActiveMQTextMessage();
            msg.setText(String.valueOf(i));
            producer.send(msg);
        }
        producer.close();
        session.close();
    }

    public void receiveMessages() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(queue);
        for (int i = 0; i < 5000; i++) {
            Message message = consumer.receive(5000);
            if (message == null) {
                throw new IllegalStateException("There must be a message in the queue! Something is wrong");
            }
            String text = ((ActiveMQTextMessage) message).getText();
            if (!text.equals(String.valueOf(i))) {
                throw new IllegalStateException("Message has wrong content");
            }
        }
    }

    @Fork(jvmArgs = "-Dulyp.off", value = 3)
    @Benchmark
    public void baseline() {
        runTest();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.ActiveMQZxcxckzxc.kdusdhfe", value = 3)
    @Benchmark
    public void instrumented() {
        runTest();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.ActiveMQBootstrapBenchmark.runTest", value = 3)
    @Benchmark
    public void record() {
        runTest();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.ActiveMQBootstrapBenchmark.runTest", value = 3)
    @Benchmark
    public void syncRecord(Counters counters) {
        execSyncRecord(counters, this::runTest);
    }

    private void runTest() {
        try {
            setup();
            sendMessages();
            receiveMessages();
            teardown();
        } catch (JMSException e) {
            throw new RuntimeException("Test failed", e);
        }
    }
}
