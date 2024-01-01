package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.openjdk.jmh.annotations.*;

import javax.jms.*;
import javax.jms.IllegalStateException;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5, time = 1)
public class ActiveMQInstrumentationBenchmark {

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Queue queue;
    private int iter;

    @Setup(Level.Iteration)
    public void setup() throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory("vm://localhost" + (49900 + iter++) + "?broker.persistent=false");
        connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue("TEST_QUEUE_" + iter++);
        session.close();
    }

    @TearDown(Level.Iteration)
    public void teardown() throws JMSException {
        connection.close();
        connectionFactory = null;
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
                throw new javax.jms.IllegalStateException("There must be a message in the queue! Something is wrong");
            }
            String text = ((ActiveMQTextMessage) message).getText();
            System.out.println(">>>" + text);
            if (!text.equals(String.valueOf(i))) {
                throw new IllegalStateException("Message has wrong content");
            }
        }
    }

    @Fork(value = 2)
    @Benchmark
    public void noAgent() throws JMSException {
        sendMessages();
        receiveMessages();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.zxc",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void instrumentOnly() throws JMSException {
        sendMessages();
        receiveMessages();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.ActiveMQInstrumentationBenchmark.instrumentAndRecord",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void instrumentAndRecord() throws JMSException {
        sendMessages();
        receiveMessages();
    }
}
