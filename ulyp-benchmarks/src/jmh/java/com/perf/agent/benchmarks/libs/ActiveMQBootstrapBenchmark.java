package com.perf.agent.benchmarks.libs;

import java.util.concurrent.TimeoutException;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import com.ulyp.agent.util.AgentHelper;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.openjdk.jmh.annotations.*;

import javax.jms.IllegalStateException;
import javax.jms.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class ActiveMQBootstrapBenchmark {

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

    @Fork(value = 2)
    @Benchmark
    public void baseline() throws JMSException {
        runTest();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.zxc",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void instrumentOnly() throws JMSException {
        runTest();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.runTest",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void instrumentAndRecord() throws JMSException {
        runTest();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.runTest",
        "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void instrumentAndRecordSync() throws JMSException, InterruptedException, TimeoutException {
        runTest();
        AgentHelper.syncWriting();
    }

    private void runTest() throws JMSException {
        setup();
        sendMessages();
        receiveMessages();
        teardown();
    }
}
