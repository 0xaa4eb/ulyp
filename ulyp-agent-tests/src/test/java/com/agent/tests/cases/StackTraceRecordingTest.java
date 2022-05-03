package com.test.cases;

import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.StorageReader;
import org.junit.Assert;
import org.junit.Test;

public class StackTraceRecordingTest extends AbstractInstrumentationTest {

/*
    @Test
    public void shouldRecordStackTraceOfMethodWhenRecordingStarted() {
        StorageReader reader = runForkProcessWithUiAndReturnProtoRequest(
                new ForkProcessBuilder()
                        .setMainClassName(StackTraceTestCase.class)
                        .setMethodToRecord(MethodMatcher.parse("X.foo"))
        );

        TStackTrace stackTrace = reader.get(0).getRecordingInfo().getStackTrace();

        TStackTraceElement firstElement = stackTrace.getElementList().get(0);

        Assert.assertEquals(firstElement.getDeclaringClass(), "com.test.cases.StackTraceRecordingTest$X");
        Assert.assertEquals(firstElement.getMethodName(), "foo");
        Assert.assertEquals(firstElement.getFileName(), "StackTraceRecordingTest.java");

        TStackTraceElement secondElement = stackTrace.getElementList().get(1);

        Assert.assertEquals(secondElement.getDeclaringClass(), "com.test.cases.StackTraceRecordingTest$StackTraceTestCase");
        Assert.assertEquals(secondElement.getMethodName(), "main");
        Assert.assertEquals(secondElement.getFileName(), "StackTraceRecordingTest.java");
    }
    */

    public static class X {

        public void bar() {

        }

        public void foo() {
            bar();
        }
    }

    public static class StackTraceTestCase {

        public static void main(String[] args) {
            new X().foo();
        }
    }
}
