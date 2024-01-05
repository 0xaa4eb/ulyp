package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import com.ulyp.agent.util.AgentHelper;

import org.openjdk.jmh.annotations.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 3)
@Measurement(iterations = 10, time = 3)
public class H2DatabaseBenchmark {

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

    @Fork(value = 2)
    @Benchmark
    public void insertNoAgent() throws Exception {
        insertRow();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.H2DatabaseBenchmark.asdasd",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void insertInstrumented() throws Exception {
        insertRow();
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.H2DatabaseBenchmark.insertRow",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void insertRecord() throws Exception {
        insertRow();
    }

    @Fork(jvmArgs = {
        BenchmarkConstants.AGENT_PROP,
        "-Dulyp.file=/tmp/test.dat",
        "-Dulyp.methods=**.H2DatabaseBenchmark.insertRow",
        "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
        "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void insertRecordSync() throws Exception {
        insertRow();
        AgentHelper.syncWriting();
    }

    private void insertRow() throws SQLException {
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
    }
}
