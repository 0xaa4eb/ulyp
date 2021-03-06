package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.SafeCaller;
import com.test.cases.util.TestSettingsBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.NullObjectRepresentation;
import com.ulyp.core.printers.ThrowableRepresentation;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class InstrumentationCodeTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordMainMethod() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(MainMethodCase.class)
                        .setMethodToRecord("main")
        );

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is("com.test.printers.InstrumentationCodeTest$MainMethodCase"));
        assertThat(root.getChildren(), hasSize(1));
    }

    @Test
    public void shouldTraceStaticMethodCall() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(SimpleTestCases.class)
                        .setMethodToRecord("staticMethod")
        );

        assertThat(root.getMethodName(), is("staticMethod"));
        assertThat(root.getArgTexts(), empty());
    }

    @Test
    public void shouldBeValidForStringReturningMethodWithEmptyArgs() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(SimpleTestCases.class)
                        .setMethodToRecord("returnStringWithEmptyParams")
        );

        assertThat(root.getChildren(), is(empty()));
        assertThat(root.getArgTexts(), is(empty()));
        assertThat(root.getReturnValue().getPrintedText(), is("asdvdsa2"));
        assertThat(root.getSubtreeNodeCount(), is(1L));
        assertThat(root.getClassName(), is("com.test.printers.InstrumentationCodeTest$SimpleTestCases"));
        assertThat(root.getMethodName(), is("returnStringWithEmptyParams"));
    }

    @Test
    public void shouldBeValidForNullReturningMethodWithEmptyArgs() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(SimpleTestCases.class)
                        .setMethodToRecord("returnNullObjectWithEmptyParams")
        );

        assertThat(root.getChildren(), is(empty()));
        assertThat(root.getArgTexts(), is(empty()));
        assertThat(root.getReturnValue(), is(NullObjectRepresentation.getInstance()));
        assertThat(root.getSubtreeNodeCount(), is(1L));
        assertThat(root.getClassName(), is("com.test.printers.InstrumentationCodeTest$SimpleTestCases"));
        assertThat(root.getMethodName(), is("returnNullObjectWithEmptyParams"));
    }

    @Test
    public void shouldBeValidForIntReturningMethodWithEmptyArgs() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(SimpleTestCases.class)
                        .setMethodToRecord("returnIntWithEmptyParams")
        );

        assertThat(root.getChildren(), is(empty()));
        assertThat(root.getArgTexts(), is(empty()));
        assertThat(root.getReturnValue().getPrintedText(), is("124234232"));
        assertThat(root.getSubtreeNodeCount(), is(1L));
        assertThat(root.getClassName(), is("com.test.printers.InstrumentationCodeTest$SimpleTestCases"));
        assertThat(root.getMethodName(), is("returnIntWithEmptyParams"));
    }

    @Test
    public void shouldBeValidIfMethodThrowsException() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(SimpleTestCases.class)
                        .setMethodToRecord("throwsRuntimeException")
        );

        assertThat(root.getChildren(), is(empty()));
        assertThat(root.getArgTexts(), is(empty()));

        ThrowableRepresentation returnValue = (ThrowableRepresentation) root.getReturnValue();

        assertEquals("some exception message", returnValue.getMessage());
        assertThat(root.getSubtreeNodeCount(), is(1L));
        assertThat(root.getClassName(), is("com.test.printers.InstrumentationCodeTest$SimpleTestCases"));
        assertThat(root.getMethodName(), is("throwsRuntimeException"));
    }

    @Test
    public void shouldBeValidForTwoMethodCalls() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(SeveralMethodsTestCases.class)
                        .setMethodToRecord("callTwoMethods")
        );

        assertThat(root.getChildren(), is(hasSize(2)));
        assertThat(root.getArgTexts(), is(empty()));
        assertThat(root.getReturnValue().getPrintedText(), is("null"));
        assertThat(root.isVoidMethod(), is(Boolean.TRUE));
        assertThat(root.getSubtreeNodeCount(), is(3L));
        assertThat(root.getMethodName(), is("callTwoMethods"));
        assertThat(root.getClassName(), is("com.test.printers.InstrumentationCodeTest$SeveralMethodsTestCases"));

        CallRecord call1 = root.getChildren().get(0);

        assertThat(call1.getChildren(), is(empty()));
        assertThat(call1.getArgs(), is(empty()));
        assertThat(call1.getReturnValue().getPrintedText(), is("null"));
        assertThat(call1.isVoidMethod(), is(Boolean.TRUE));
        assertThat(call1.getSubtreeNodeCount(), is(1L));
        assertThat(call1.getMethodName(), is("method1"));

        CallRecord call2 = root.getChildren().get(1);

        assertThat(call2.getChildren(), is(empty()));
        assertThat(call2.getArgs(), is(empty()));
        assertThat(call2.getReturnValue().getPrintedText(), is("null"));
        assertThat(call2.isVoidMethod(), is(Boolean.TRUE));
        assertThat(call2.getSubtreeNodeCount(), is(1L));
        assertThat(call2.getMethodName(), is("method2"));
    }

    @Test
    public void shouldBeValidForIntArgument() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(SimpleTestCases.class)
                        .setMethodToRecord("consumesInt")
        );

        assertThat(root.getChildren(), is(empty()));
        assertThat(root.getArgTexts(), is(Collections.singletonList("45324")));
    }

    public static class MainMethodCase {

        public static void a() {

        }

        public static void main(String[] args) {
            a();
        }
    }

    public static class SimpleTestCases {

        public static void staticMethod() {
        }

        public static void main(String[] args) {
            SafeCaller.call(() -> new SimpleTestCases().returnIntWithEmptyParams());
            SafeCaller.call(() -> new SimpleTestCases().returnStringWithEmptyParams());
            SafeCaller.call(() -> new SimpleTestCases().returnNullObjectWithEmptyParams());
            SafeCaller.call(() -> new SimpleTestCases().throwsRuntimeException());
            SafeCaller.call(() -> new SimpleTestCases().consumesInt(45324));
            SafeCaller.call(() -> new SimpleTestCases().consumesIntAndString(45324, "asdasd"));
            staticMethod();
        }

        public String returnStringWithEmptyParams() {
            return "asdvdsa2";
        }

        public String returnNullObjectWithEmptyParams() {
            return null;
        }

        public int returnIntWithEmptyParams() {
            return 124234232;
        }

        public int throwsRuntimeException() {
            throw new RuntimeException("some exception message");
        }

        public void consumesInt(int v) {
        }

        public void consumesIntAndString(int v, String s) {
        }

        public static class TestObject {
        }
    }

    public static class SeveralMethodsTestCases {

        public static void main(String[] args) {
            SafeCaller.call(() -> new SeveralMethodsTestCases().callTwoMethods());
        }

        public void callTwoMethods() {
            method1();
            method2();
        }

        public void method1() {
            System.out.println("b");
        }

        public void method2() {
            System.out.println("c");
        }
    }
}
