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

    @Fork(value = 2)
    @Benchmark
    public void bootstrapBaseline() {
        run();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.H2MemDatabaseBenchmark.asdasd",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void bootstrapInstrumented() {
        run();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.H2MemDatabaseBenchmark.run",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void bootstrapRecord() {
        run();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.H2MemDatabaseBenchmark.run",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
        "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void bootstrapRecordSync(Counters counters) {
        execRecordAndSync(counters, this::run);
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
