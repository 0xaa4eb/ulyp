package com.perf.agent.benchmarks.libs;

import com.perf.agent.benchmarks.RecordingBenchmark;
import com.perf.agent.benchmarks.libs.util.ApplicationConfiguration;
import com.perf.agent.benchmarks.libs.util.Department;
import com.perf.agent.benchmarks.libs.util.DepartmentService;
import com.perf.agent.benchmarks.libs.util.Person;
import com.perf.agent.benchmarks.util.BenchmarkConstants;
import org.openjdk.jmh.annotations.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class SpringHibernateBootstrapBenchmark extends RecordingBenchmark {

    private static final int PEOPLE_PER_DEPT = Integer.parseInt(System.getProperty("peoplePerDeptCount", "30"));
    private static final int DEPT_COUNT = Integer.parseInt(System.getProperty("deptCount", "20"));
    private DepartmentService departmentService;

    public void setup() {
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

    public void tearDown() throws Exception {
        int peopleCount = departmentService.countPeople();
        if (peopleCount != DEPT_COUNT * PEOPLE_PER_DEPT) {
            throw new RuntimeException("People " + peopleCount);
        }

        departmentService.removeAll();
    }

    @Fork(jvmArgs = "-Dulyp.off", value = BenchmarkConstants.FORKS)
    @Benchmark
    public void baseline() {
        runTest();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.SpringHibernateBootstrapBenchmark.asdasd", value = 3)
    @Benchmark
    public void instrumented() {
        runTest();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.SpringHibernateBootstrapBenchmark.runTest", value = 3)
    @Benchmark
    public void record() {
        runTest();
    }

    @Fork(jvmArgs = "-Dulyp.methods=" + METHOD_MATCHERS + ",**.SpringHibernateBootstrapBenchmark.runTest", value = 3)
    @Benchmark
    public void syncRecord(Counters counters) {
        execSyncRecord(counters, this::runTest);
    }

    private void runTest() {
        try {
            setup();
            departmentService.shufflePeople();
            tearDown();
        } catch (Exception e) {
            throw new RuntimeException("Test failed", e);
        }
    }
}
