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
@Measurement(iterations = 60)
public class ActiveMQBenchmark extends RecordingBenchmark {

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

    @TearDown(Level.Trial)
    public void teardown() throws JMSException {
        connection.close();
        connectionFactory = null;
    }

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public void sendMsgBaseline() {
        sendMsg();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.zxc",
            "-Dulyp.constructors"
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void sendMsgInstrumented() {
        sendMsg();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQBenchmark.sendMsg",
            "-Dulyp.constructors",
            "-Dulyp.metrics",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void sendMsgRecord() {
        sendMsg();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.ActiveMQBenchmark.sendMsg",
        "-Dulyp.constructors",
        "-Dulyp.metrics",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=INFO",
    }, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void sendMsgRecordSync(Counters counters) {
        execRecordAndSync(counters, this::sendMsg);
    }

    private void sendMsg() {
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            for (int i = 0; i < 100; i++) {
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
