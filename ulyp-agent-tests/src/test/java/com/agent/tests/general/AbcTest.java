package com.agent.tests.general;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Ignore;
import org.junit.Test;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;

public class AbcTest extends AbstractInstrumentationTest {

    @Test
    public void shouldWithstandLongRecording() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(XCVA.class)
                        .withMethodToRecord(MethodMatcher.parse("*.*"))
                        .withExcludeClassesProperty("com.agent.tests.general.AbcTest$XCVA")
        );

        System.out.println(recordingResult);
    }

    public static class CustomLogger extends PrintStream {

        public CustomLogger(@NotNull OutputStream out) {
            super(out, true);
        }

        @Override
        public void println(@Nullable String x) {
            super.println("ABAIUSDOAYSDA: " + x);
        }
    }

    public static class X {

        public static String foo() {
            return "ABC";
        }
    }

    public static class XCVA {



        public static void main(String[] args) throws InterruptedException {
            System.setOut(new CustomLogger(System.out));
            X.foo();
        }
    }
}