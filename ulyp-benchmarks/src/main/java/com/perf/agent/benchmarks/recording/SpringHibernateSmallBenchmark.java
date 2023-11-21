package com.perf.agent.benchmarks.recording;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.perf.agent.benchmarks.BenchmarkScenarioBuilder;
import com.perf.agent.benchmarks.recording.util.ApplicationConfiguration;
import com.perf.agent.benchmarks.recording.util.User;
import com.perf.agent.benchmarks.recording.util.UserService;
import com.ulyp.core.util.MethodMatcher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;

public class SpringHibernateSmallBenchmark implements Benchmark {

    private UserService saver;

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        SpringHibernateSmallBenchmark benchmark = new SpringHibernateSmallBenchmark();
        benchmark.setUp();
        benchmark.run();
        benchmark.tearDown();

        System.out.println("Took: " + (System.currentTimeMillis() - start));
    }

    @Override
    public List<BenchmarkScenario> getProfiles() {
        return Arrays.asList(
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(SpringHibernateSmallBenchmark.class, "main"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(UserService.class, "save"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(SpringHibernateSmallBenchmark.class, "doesntExist"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withAgentDisabled()
                        .build()
        );
    }

    @Override
    public void setUp() {
        ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        saver = context.getBean(UserService.class);
    }

    @Override
    public void tearDown() {
        int count = saver.findAll().size();
        if (count != 1) {
            throw new RuntimeException("Doesn't work, users found: " + count);
        }
    }

    @Override
    public void run() {
        User user = new User("Test", "User");
        saver.save(user);
    }
}
