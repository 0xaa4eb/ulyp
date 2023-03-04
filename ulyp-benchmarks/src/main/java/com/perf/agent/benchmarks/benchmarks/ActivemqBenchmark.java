package com.perf.agent.benchmarks.benchmarks;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.perf.agent.benchmarks.BenchmarkScenarioBuilder;
import com.ulyp.core.util.MethodMatcher;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.IllegalStateException;
import javax.jms.*;
import java.util.Arrays;
import java.util.List;

public class ActivemqBenchmark implements Benchmark {

    private static final int MESSAGE_COUNT = 50000;

    public static void main(String[] args) throws Exception {
        try {
            long start = System.currentTimeMillis();

            ActivemqBenchmark benchmark = new ActivemqBenchmark();
            benchmark.setUp();
            benchmark.run();
            benchmark.tearDown();

            System.out.println("Took: " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<BenchmarkScenario> getProfiles() {
        return Arrays.asList(
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(ActivemqBenchmark.class, "main"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(ActivemqBenchmark.class, "run"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(ActivemqBenchmark.class, "doesntExist"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withAgentDisabled()
                        .build()
        );
    }

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Queue queue;

    public void setUp() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue("TEST_QUEUE");
        session.close();
    }

    public void tearDown() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(queue);
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            Message message = consumer.receive(5000);
            if (message == null) {
                throw new IllegalStateException("There must be a message in the queue! Something is wrong");
            }
            String text = ((ActiveMQTextMessage) message).getText();
            if (!text.equals(String.valueOf(i))) {
                throw new IllegalStateException("Message has wrong content");
            }
        }
        session.close();
        connection.close();
    }

    public void run() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            ActiveMQTextMessage msg = new ActiveMQTextMessage();
            msg.setText(String.valueOf(i));
            producer.send(msg);
        }
        producer.close();
        session.close();
    }
}