package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.test.cases.util.RecordingResult;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

        assertThat(root.getMethod().getImplementingType().getName(), is("com.test.cases.TypeTest$FooImpl"));
        assertThat(root.getMethod().getDeclaringType().getName(), is("com.test.cases.TypeTest$FooImpl"));
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
