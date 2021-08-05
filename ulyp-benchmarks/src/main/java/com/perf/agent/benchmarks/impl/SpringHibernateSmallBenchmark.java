package com.perf.agent.benchmarks.impl;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkProfile;
import com.perf.agent.benchmarks.BenchmarkProfileBuilder;
import com.perf.agent.benchmarks.impl.spring.ApplicationConfiguration;
import com.perf.agent.benchmarks.impl.spring.User;
import com.perf.agent.benchmarks.impl.spring.UserService;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;

public class SpringHibernateSmallBenchmark implements Benchmark {

    @Override
    public List<BenchmarkProfile> getProfiles() {
        return Arrays.asList(
                new BenchmarkProfileBuilder()
                        .withMethodToRecord(new MethodMatcher(SpringHibernateSmallBenchmark.class, "main"))
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build(),
                new BenchmarkProfileBuilder()
                        .withMethodToRecord(new MethodMatcher(UserService.class, "save"))
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build(),
                new BenchmarkProfileBuilder()
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build(),
                new BenchmarkProfileBuilder().build()
        );
    }

    private ApplicationContext context;
    private UserService saver;

    @Override
    public void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        saver = context.getBean(UserService.class);
    }

    @Override
    public void tearDown() throws Exception {
        int count = saver.findAll().size();
        if (count != 1) {
            throw new RuntimeException("Doesn't work, users found: " + count);
        }
    }

    @Override
    public void run() throws Exception {
        User user = new User("Test", "User");
        saver.save(user);
    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();

        SpringHibernateSmallBenchmark benchmark = new SpringHibernateSmallBenchmark();
        benchmark.setUp();
        benchmark.run();
        benchmark.tearDown();

        System.out.println("Took: " + (System.currentTimeMillis() - start));
    }
}
