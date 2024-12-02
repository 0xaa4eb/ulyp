package com.agent.tests.recording;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.basic.ThrowableRecord;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import com.ulyp.storage.tree.Recording;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TypeInitializerRecordTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordTypeInitializer() {
        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(ClassWithStaticInitializer.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ClassWithStaticInitializer.*"))
                        .withInstrumentTypeInitializers(true)
        );

        List<Recording> recordings = recordingResult.recordings();

        Recording clinitInitializer = recordings.get(0);

        CallRecord root = clinitInitializer.getRoot();

        assertThat(root.getMethod().getName(), is("<clinit>"));
        assertThat(root.getMethod().returnsSomething(), is(false));
    }

    public static class ClassWithStaticInitializer {

        static {
            foo();
        }

        private static void foo() {
            System.out.println("42");
        }

        public static void main(String[] args) throws InterruptedException {

        }
    }

    @Test
    void shouldRecordAllMethods2() {
        RecordingResult recordingResult = runSubprocess(
            new ForkProcessBuilder()
                .withMain(ClassWhichUsesFailureTypeInitializer.class)
                .withMethodToRecord(MethodMatcher.parse("**.X.*"))
                .withInstrumentTypeInitializers(true)
        );

        List<Recording> recordings = recordingResult.recordings();

        CallRecord clinitInitializer = recordings.get(0).getRoot();
        assertThat(clinitInitializer.hasThrown(), is(true));

        ObjectRecord returnValue = recordings.get(0).getRoot().getReturnValue();
        assertThat(returnValue, Matchers.instanceOf(ThrowableRecord.class));
    }

    public static class X {
        static {
            if (true) {
                throw new NoClassDefFoundError("A");
            }
        }
    }

    public static class ClassWhichUsesFailureTypeInitializer {

        public static void main(String[] args) throws InterruptedException {
            try {
                System.out.println(new X());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
