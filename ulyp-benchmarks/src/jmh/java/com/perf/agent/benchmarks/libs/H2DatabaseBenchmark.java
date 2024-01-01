package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
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

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.H2MemDatabaseBenchmark.asdasd",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void insertInstrumented() throws Exception {
        try (PreparedStatement prep = connection.prepareStatement("insert into test values(?, ?)")) {
            prep.setInt(1, id++);
            prep.setString(2, "Hello");
            prep.execute();
        }
    }

    @Fork(jvmArgs = {
            BenchmarkConstants.AGENT_PROP,
            "-Dulyp.file=/tmp/test.dat",
            "-Dulyp.methods=**.H2MemDatabaseBenchmark.insertRecord",
            "-Dcom.ulyp.slf4j.simpleLogger.defaultLogLevel=OFF",
            "-Dulyp.constructors"
    }, value = 2)
    @Benchmark
    public void insertRecord() throws Exception {
        try (PreparedStatement prep = connection.prepareStatement("insert into test values(?, ?)")) {
            prep.setInt(1, id++);
            prep.setString(2, "Hello");
            prep.execute();
        }
    }

    @Fork(value = 2)
    @Benchmark
    public void insertNoAgent() throws Exception {
        try (PreparedStatement prep = connection.prepareStatement("insert into test values(?, ?)")) {
            prep.setInt(1, id++);
            prep.setString(2, "Hello");
            prep.execute();
        }
    }
}
