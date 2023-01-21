package com.agent.tests.libs;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;
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
import com.ulyp.storage.CallRecord;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class HibernateRecordingTest extends AbstractInstrumentationTest {

    @Test
    public void testSaveEntityWithHibernate() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(HibernateSaveEntityTest.class)
                        .withMethodToRecord(MethodMatcher.parse("**.HibernateSaveEntityTest.main"))
                        .withInstrumentedPackages()
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(
                DebugCallRecordTreePrinter.printTree(singleRoot),
                singleRoot.getSubtreeSize(),
                greaterThan(3000)
        );
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
        }
    }
}
