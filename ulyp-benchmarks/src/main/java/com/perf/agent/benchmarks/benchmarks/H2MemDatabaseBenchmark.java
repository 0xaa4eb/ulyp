package com.perf.agent.benchmarks.benchmarks;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.perf.agent.benchmarks.BenchmarkProfileBuilder;
import com.ulyp.core.util.MethodMatcher;

import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class H2MemDatabaseBenchmark implements Benchmark {

    private Connection connection;

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        H2MemDatabaseBenchmark benchmark = new H2MemDatabaseBenchmark();
        benchmark.setUp();
        benchmark.run();
        benchmark.tearDown();

        System.out.println("Took: " + (System.currentTimeMillis() - start));
    }

    @Override
    public List<BenchmarkScenario> getProfiles() {
        return Arrays.asList(
                new BenchmarkProfileBuilder()
                        .withMethodToRecord(new MethodMatcher(H2MemDatabaseBenchmark.class, "main"))
                        .build(),
                new BenchmarkProfileBuilder()
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

        Statement statement = connection.createStatement();
        statement.execute("create table test(id int primary key, name varchar)");
    }

    public void tearDown() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select count(*) from test");
        resultSet.next();
        System.out.println(resultSet.getInt(1));
        connection.close();

        statement.close();
    }

    public void run() throws Exception {

        for (int id = 0; id < 400; id++) {
            new Inserter(connection).insert(id);
        }
    }

    private static class Inserter {
        private final Connection connection;

        private Inserter(Connection connection) {
            this.connection = connection;
        }

        public void insert(int id) throws SQLException {
            PreparedStatement prep = connection.prepareStatement(
                    "insert into test values(?, ?)");
            prep.setInt(1, id);
            prep.setString(2, "Hello");
            prep.execute();
        }
    }
}