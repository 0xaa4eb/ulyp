package com.perf.agent.benchmarks.recording;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.perf.agent.benchmarks.Benchmark;
import com.perf.agent.benchmarks.BenchmarkScenario;
import com.perf.agent.benchmarks.BenchmarkScenarioBuilder;
import com.perf.agent.benchmarks.recording.util.ApplicationConfiguration;
import com.perf.agent.benchmarks.recording.util.Department;
import com.perf.agent.benchmarks.recording.util.DepartmentService;
import com.perf.agent.benchmarks.recording.util.Person;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.core.util.PackageList;

public class SpringHibernateMediumBenchmark implements Benchmark {

    private static final int PEOPLE_PER_DEPT = Integer.parseInt(System.getProperty("peoplePerDeptCount", "30"));
    private static final int DEPT_COUNT = Integer.parseInt(System.getProperty("deptCount", "20"));
    private DepartmentService departmentService;

    public static void main(String[] args) throws Exception {
        System.out.println(ManagementFactory.getRuntimeMXBean().getInputArguments());

        long start = System.currentTimeMillis();

        SpringHibernateMediumBenchmark benchmark = new SpringHibernateMediumBenchmark();
        benchmark.setUp();
        benchmark.run();
        benchmark.tearDown();

        System.out.println("Took: " + (System.currentTimeMillis() - start));
    }

    @Override
    public List<BenchmarkScenario> getProfiles() {
        return Arrays.asList(
                new BenchmarkScenarioBuilder()
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(DepartmentService.class, "save"))
                        .withAdditionalArgs("-DdeptCount=250")
                        .withAdditionalArgs("-DpeoplePerDeptCount=20")
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build(),
                new BenchmarkScenarioBuilder()
                        .withMethodToRecord(new MethodMatcher(DepartmentService.class, "shuffle"))
                        .withWriteDisabled()
                        .withInstrumentedPackages(new PackageList("com", "org"))
                        .build()
        );
    }

    @Override
    public void setUp() {
        ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
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
    public void run() {
        Histogram histogram = new Histogram(1, TimeUnit.MINUTES.toMillis(5), 2);

        for (int i = 0; i < 1; i++) {
            long startNanos = System.nanoTime();
            departmentService.shufflePeople();
            long elapsed = System.nanoTime() - startNanos;
            histogram.recordValue(elapsed / 1000000);
        }

        histogram.outputPercentileDistribution(System.out, 1.0);
    }
}
