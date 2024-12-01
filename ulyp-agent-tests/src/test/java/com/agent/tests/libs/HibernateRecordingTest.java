package com.agent.tests.libs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import com.ulyp.storage.tree.Recording;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.agent.tests.libs.util.hibernate.ApplicationConfiguration;
import com.agent.tests.libs.util.hibernate.Department;
import com.agent.tests.libs.util.hibernate.DepartmentService;
import com.agent.tests.libs.util.hibernate.Person;
import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.DebugCallRecordTreePrinter;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HibernateRecordingTest extends AbstractInstrumentationTest {

    @Test
    void testSaveEntityWithHibernate() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(HibernateSaveEntityTest.class)
                        .withMethodToRecord(MethodMatcher.parse("**.HibernateSaveEntityTest.main"))
                        .withInstrumentedPackages()
                        .withRecordConstructors()
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(DebugCallRecordTreePrinter.printTree(singleRoot), singleRoot.getSubtreeSize(), greaterThan(15000));
    }

    public static class HibernateSaveEntityTest {

        public static void main(String[] args) throws Exception {
            ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
            DepartmentService departmentService = context.getBean(DepartmentService.class);

            Department department = new Department();
            for (int i = 0; i < 5; i++) {

                Person p = new Person();
                p.setFirstName("Name" + i);
                p.setLastName("LastName" + i);
                p.setPhoneNumber(String.valueOf(ThreadLocalRandom.current().nextInt()));
                p.setAge(ThreadLocalRandom.current().nextInt(100));

                department.getPeople().add(p);
            }

            departmentService.save(department);

            List<Department> allDepartments = departmentService.findAll();

            assertEquals(1, allDepartments.size());

            Department departmentFromDb = allDepartments.get(0);
            Set<Person> people = departmentFromDb.getPeople();
            for (Person p : people) {
                Assertions.assertNotNull(p.getId());
            }
        }
    }

    @Test
    void testSaveEntityWithHibernateMultithreaded() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(MultithreadedHibernateSaveEntityTest.class)
                        .withMethodToRecord(MethodMatcher.parse("**.DepartmentService.save"))
                        .withInstrumentedPackages()
                        .withRecordConstructors()
        );

        List<Recording> recordings = recordingResult.recordings();

        assertEquals(20, recordings.size());
        for (Recording recording : recordings) {
            assertThat(recording.getRoot().getSubtreeSize(), greaterThan(20000));
        }
    }

    public static class MultithreadedHibernateSaveEntityTest {

        public static void main(String[] args) throws Exception {
            ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
            DepartmentService departmentService = context.getBean(DepartmentService.class);

            int threads = 4;
            int deptsPerThread = 5;
            ExecutorService executorService = Executors.newFixedThreadPool(threads);
            try {
                List<Future<?>> futures = new ArrayList<>();

                for (int i = 0; i < threads; i++) {
                    futures.add(executorService.submit(
                            () -> {
                                for (int deptI = 0; deptI < deptsPerThread; deptI++) {
                                    Department department = new Department();
                                    for (int personIndex = 0; personIndex < 5; personIndex++) {

                                        Person p = new Person();
                                        p.setFirstName("Name" + personIndex);
                                        p.setLastName("LastName" + personIndex);
                                        p.setPhoneNumber(String.valueOf(ThreadLocalRandom.current().nextInt()));
                                        p.setAge(ThreadLocalRandom.current().nextInt(100));

                                        department.getPeople().add(p);
                                    }

                                    departmentService.save(department);
                                }
                            }
                    ));
                }

                for (Future<?> future : futures) {
                    future.get(1, TimeUnit.MINUTES);
                }
            } finally {
                executorService.shutdownNow();
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            }

            List<Department> allDepartments = departmentService.findAll();

            assertEquals(deptsPerThread * threads, allDepartments.size());
        }
    }
}
