package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.sql.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class H2BootstrapBenchmark extends RecordingBenchmark {

    @Param({"5000"})
    private int insertCount;

    private Connection connection;
    private int id = 0;

    public void setUp() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        connection = DriverManager.getConnection("jdbc:h2:mem:", "sa", "");

        try (Statement statement = connection.createStatement()) {
            statement.execute("create table test(id int primary key, name varchar)");
        }
    }

    public void tearDown() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select count(*) from test");
            resultSet.next();
            int count = resultSet.getInt(1);
            if (count == 0) {
                throw new RuntimeException("No rows present in database");
            }
        }
        connection.close();
    }

    @Fork(value = BenchmarkConstants.FORKS)
    @Benchmark
    public void baseline() {
        run();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=" + METHOD_MATCHERS + ",**.H2BootstrapBenchmark.asdasd",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 3)
    @Benchmark
    public void instrumented() {
        run();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=" + METHOD_MATCHERS + ",**.H2BootstrapBenchmark.run",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 3)
    @Benchmark
    public void record() {
        run();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=" + METHOD_MATCHERS + ",**.H2BootstrapBenchmark.run",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
        "-Dulyp.constructors"
    }, value = 3)
    @Benchmark
    public void syncRecord(Counters counters) {
        execSyncRecord(counters, this::run);
    }

    private void run() {
        try {
            setUp();
            for (int i = 0; i < insertCount; i++) {
                try (PreparedStatement prep = connection.prepareStatement("insert into test values(?, ?)")) {
                    prep.setInt(1, id++);
                    prep.setString(2, "Hello");
                    prep.execute();
                }
            }
            tearDown();
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }
}
