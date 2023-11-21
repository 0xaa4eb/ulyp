package com.perf.agent.benchmarks.recording;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.perf.agent.benchmarks.BenchmarkScenarioBuilder;
import com.ulyp.core.util.MethodMatcher;

public class H2MemDatabaseBenchmark implements Benchmark {

    private static final int MESSAGE_COUNT = Integer.parseInt(System.getProperty("rowCount", "400"));
    private Connection connection;

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        H2MemDatabaseBenchmark benchmark = new H2MemDatabaseBenchmark();
        benchmark.setUp();
        benchmark.run();
        benchmark.tearDown();

        System.out.println("Took: " + (System.currentTimeMillis() - start));
        System.out.println("Message count: " + MESSAGE_COUNT);
    }

    @Override
    public List<BenchmarkScenario> getProfiles() {
        return Arrays.asList(
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(H2MemDatabaseBenchmark.class, "main"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(H2MemDatabaseBenchmark.class, "doesntExits"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withAdditionalArgs("-DrowCount=4000000")
                        .withMethodToRecord(new MethodMatcher(H2MemDatabaseBenchmark.class, "doesntExits"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withAgentDisabled()
                        .build()
        );
    }

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
            if (count != MESSAGE_COUNT) {
                throw new RuntimeException("Row number " + count + " doesn't match the expected value which is " + MESSAGE_COUNT);
            }
        }
        connection.close();
    }

    public void run() throws Exception {

        for (int id = 0; id < MESSAGE_COUNT; id++) {
            new Inserter(connection).insert(id);
        }
    }

    private static class Inserter {
        private final Connection connection;

        private Inserter(Connection connection) {
            this.connection = connection;
        }

        public void insert(int id) throws SQLException {
            try (PreparedStatement prep = connection.prepareStatement("insert into test values(?, ?)")) {
                prep.setInt(1, id);
                prep.setString(2, "Hello");
                prep.execute();
            }
        }
    }
}