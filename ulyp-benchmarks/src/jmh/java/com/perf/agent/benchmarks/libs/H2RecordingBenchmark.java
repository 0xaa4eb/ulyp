package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 20)
@Measurement(iterations = 30)
public class H2RecordingBenchmark extends RecordingBenchmark {

    public static final int INSERTS_PER_INVOCATION = 300;

    private Connection connection;
    private int id = 0;

    @Setup(Level.Trial)
    public void setUp() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        connection = DriverManager.getConnection("jdbc:h2:mem:", "sa", "");

        try (Statement statement = connection.createStatement()) {
            statement.execute("create table test(" +
                    "id int primary key, " +
                    "i1 int, " +
                    "i2 int, " +
                    "i3 int, " +
                    "i4 int, " +
                    "i5 int, " +
                    "s1 varchar," +
                    "s2 varchar," +
                    "s3 varchar," +
                    "s4 varchar," +
                    "s5 varchar)");
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

    @Fork(jvmArgs = "-Dulyp.off", value = BenchmarkConstants.FORKS)
    @Benchmark
    public void baseline() {
        insertRow();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.H2RecordingBenchmark.asdasd", value = BenchmarkConstants.FORKS)
    @Benchmark
    public void instrumented() {
        insertRow();
    }

    @Fork(jvmArgs = {"-Dulyp.methods=**.H2RecordingBenchmark.insertRow", "-Dulyp.metrics"}, value = BenchmarkConstants.FORKS)
    @Benchmark
    public void record() {
        insertRow();
    }

    @Fork(jvmArgs = "-Dulyp.methods=**.H2RecordingBenchmark.insertRow", value = BenchmarkConstants.FORKS)
    @Benchmark
    public void syncRecord(Counters counters) {
        execSyncRecord(counters, this::insertRow);
    }

    private void insertRow() {
        try {
            for (int i = 0; i < INSERTS_PER_INVOCATION; i++) {
                try (PreparedStatement prep = connection.prepareStatement("insert into test values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    prep.setInt(1, id++);
                    prep.setInt(2, ThreadLocalRandom.current().nextInt());
                    prep.setInt(3, ThreadLocalRandom.current().nextInt());
                    prep.setInt(4, ThreadLocalRandom.current().nextInt());
                    prep.setInt(5, ThreadLocalRandom.current().nextInt());
                    prep.setInt(6, ThreadLocalRandom.current().nextInt());
                    prep.setString(7, String.valueOf(ThreadLocalRandom.current().nextLong()));
                    prep.setString(8, String.valueOf(ThreadLocalRandom.current().nextLong()));
                    prep.setString(9, String.valueOf(ThreadLocalRandom.current().nextLong()));
                    prep.setString(10, String.valueOf(ThreadLocalRandom.current().nextLong()));
                    prep.setString(11, String.valueOf(ThreadLocalRandom.current().nextLong()));
                    prep.execute();
                }
                connection.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
