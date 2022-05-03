package com.agent.tests.cases;

import com.agent.tests.cases.util.ForkProcessBuilder;
import com.agent.tests.cases.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TypeTest extends AbstractInstrumentationTest {

    @Test
    public void shouldProvideArgumentTypes() {
        RecordingResult recordingResult = runForkProcess(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord(MethodMatcher.parse("**.FooImpl.bar"))
        );

        recordingResult.assertRecordingSessionCount(1);

        CallRecord root = recordingResult.getSingleRoot();

        assertThat(root.getMethod().getImplementingType().getName(), is("com.agent.tests.cases.TypeTest$FooImpl"));
        assertThat(root.getMethod().getDeclaringType().getName(), is("com.agent.tests.cases.TypeTest$FooImpl"));
    }

    public interface Foo {
        void bar();
    }

    public static class FooImpl implements Foo {

        @Override
        public void bar() {
            System.out.println("ABC");
        }
    }

    public static class TestCase {

        public static void main(String[] args) {
            new FooImpl().bar();
        }
    }
}
