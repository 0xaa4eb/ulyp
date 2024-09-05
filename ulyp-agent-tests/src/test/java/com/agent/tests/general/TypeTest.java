package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TypeTest extends AbstractInstrumentationTest {

    @Test
    void shouldProvideArgumentTypes() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.FooImpl.bar"))
        );

        recordingResult.assertRecordingSessionCount(1);

        CallRecord root = recordingResult.getSingleRoot();

        assertThat(root.getMethod().getType().getName(), is("com.agent.tests.general.TypeTest$FooImpl"));
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
