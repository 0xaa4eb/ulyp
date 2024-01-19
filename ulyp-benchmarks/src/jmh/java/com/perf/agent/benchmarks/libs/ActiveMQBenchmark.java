package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import com.ulyp.agent.util.AgentHelper;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.openjdk.jmh.annotations.*;

import javax.jms.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2, time = 3)
@Measurement(iterations = 5, time = 3)
public class ActiveMQBenchmark {

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

    @Fork(value = 2)
    @Benchmark
    public void sendMsgBaseline() throws JMSException {
        sendMsg();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.zxc",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void sendMsgInstrumented() throws JMSException {
        sendMsg();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.sendMsg",
            "-Dulyp.constructors",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = 2)
    @Benchmark
    public void sendMsgRecord() throws JMSException {
        sendMsg();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.sendMsg",
        "-Dulyp.constructors",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
    }, value = 2)
    @Benchmark
    public void sendMsgRecordSync() throws JMSException, InterruptedException, TimeoutException {
        sendMsg();
        AgentHelper.syncWriting();
    }

    private void sendMsg() throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(queue);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        ActiveMQTextMessage msg = new ActiveMQTextMessage();
        msg.setText(String.valueOf(ThreadLocalRandom.current().nextInt()));
        producer.send(msg);
        producer.close();
        session.close();
    }
}
