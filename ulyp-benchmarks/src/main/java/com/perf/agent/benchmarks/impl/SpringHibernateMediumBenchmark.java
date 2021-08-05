package com.perf.agent.benchmarks.impl;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkProfile;
import com.perf.agent.benchmarks.BenchmarkProfileBuilder;
import com.perf.agent.benchmarks.impl.spring.*;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;
import org.HdrHistogram.Histogram;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SpringHibernateMediumBenchmark implements Benchmark {

    private static final int PEOPLE_PER_DEPT = 30;
    private static final int DEPT_COUNT = 20;

    @Override
    public List<BenchmarkProfile> getProfiles() {
        return Arrays.asList(
                new BenchmarkProfileBuilder()
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build(),
                new BenchmarkProfileBuilder()
                        .withMethodToRecord(new MethodMatcher(DepartmentService.class, "shuffle"))
                        .withUiDisabled()
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build()
        );
    }

    private ApplicationContext context;
    private DepartmentService departmentService;

    @Override
    public void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        departmentService = context.getBean(DepartmentService.class);

        for (int i = 0; i < DEPT_COUNT; i++) {
            Department department = new Department();

            for (int z = 0; z < PEOPLE_PER_DEPT; z++) {
                Person person = new Person();
                person.setFirstName("Jeff_" + i + "_" + z);
                person.setLastName("");
                person.setPhoneNumber("+15324234234");
                person.setAge(ThreadLocalRandom.current().nextInt(50));
                department.addPerson(person);
            }

            departmentService.save(department);
        }
    }

    @Override
    public void tearDown() throws Exception {
        int peopleCount = departmentService.countPeople();
        if (peopleCount != DEPT_COUNT * PEOPLE_PER_DEPT) {
            throw new RuntimeException("People " + peopleCount);
        }

        departmentService.removeAll();
    }

    @Override
    public void run() throws Exception {
        Histogram histogram = new Histogram(1, TimeUnit.MINUTES.toMillis(5), 2);

        for (int i = 0; i < 100; i++) {
            long startNanos = System.nanoTime();
            departmentService.shufflePeople();
            long elapsed = System.nanoTime() - startNanos;
            histogram.recordValue(elapsed / 1000000);
        }

        histogram.outputPercentileDistribution(System.out, 1.0);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(ManagementFactory.getRuntimeMXBean().getInputArguments());

        long start = System.currentTimeMillis();

        SpringHibernateMediumBenchmark benchmark = new SpringHibernateMediumBenchmark();
        benchmark.setUp();
        benchmark.run();
        benchmark.tearDown();

        System.out.println("Took: " + (System.currentTimeMillis() - start));
    }
}
