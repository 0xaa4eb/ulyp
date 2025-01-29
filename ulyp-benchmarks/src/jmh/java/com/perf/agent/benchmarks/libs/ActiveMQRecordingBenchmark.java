package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.openjdk.jmh.annotations.*;

import javax.jms.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 20)
@Measurement(iterations = 20)
public class ActiveMQRecordingBenchmark extends RecordingBenchmark {

    private static final int MESSAGE_COUNT = 5000;

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Queue queue;
    private int iter;

    @Setup(Level.Trial)
    public void setup() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory("vm://localhost" + (49900 + iter++) + "?broker.persistent=false");
        connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue("TEST_QUEUE_" + iter++);
        session.close();
    }

    @TearDown(Level.Invocation)
    public void drainQueue() throws JMSException {
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                consumer.receive();
            }
            consumer.close();
            session.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TearDown(Level.Trial)
    public void teardown() throws JMSException {
        connection.close();
        connectionFactory = null;
    }

    @Fork(jvmArgs = "-Dulyp.off", value = BenchmarkConstants.FORKS)
    @Benchmark
    public void baseline() {
        sendMsg();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.ActiveMQRecordingBenchmark.zxc", value = BenchmarkConstants.FORKS)
    @Benchmark
    public void instrumented() {
        sendMsg();
    }

    @Fork(jvmArgs = {
            "-Dulyp.methods=**.ActiveMQRecordingBenchmark.sendMsg",
            "-Dulyp.metrics",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void record() {
        sendMsg();
    }

    @Fork(jvmArgs = {
            "-Dulyp.methods=**.ActiveMQRecordingBenchmark.sendMsg",
            "-Dulyp.metrics"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void syncRecord(Counters counters) {
        execSyncRecord(counters, this::sendMsg);
    }

    private void sendMsg() {
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                ActiveMQTextMessage msg = new ActiveMQTextMessage();
                msg.setText(String.valueOf(ThreadLocalRandom.current().nextInt()));
                producer.send(msg);
            }
            producer.close();
            session.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
