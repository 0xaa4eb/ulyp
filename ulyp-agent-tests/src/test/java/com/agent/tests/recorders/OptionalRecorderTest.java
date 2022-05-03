package com.agent.tests.recorders;

import com.agent.tests.cases.AbstractInstrumentationTest;
import com.agent.tests.cases.util.ForkProcessBuilder;
import com.ulyp.core.recorders.*;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class OptionalRecorderTest extends AbstractInstrumentationTest {

    static class TestCase {

        public static Optional<String> returnStringOptional() {
            return Optional.of("ABC");
        }

        public static void main(String[] args) {
            System.out.println(returnStringOptional());
        }
    }

    @Test
    public void testReturnOptionalWithSomeString() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnStringOptional")
        );


        ObjectRecord returnValue = root.getReturnValue();
        assertThat(returnValue, instanceOf(OptionalRecord.class));

        OptionalRecord optional = (OptionalRecord) returnValue;
        assertThat(optional.isEmpty(), is(false));
        assertThat(optional.getValue(), instanceOf(StringObjectRecord.class));

        StringObjectRecord string = (StringObjectRecord) optional.getValue();
        assertThat(string.value(), is("ABC"));
    }

    static class TestCase2 {

        public static Optional<String> returnStringOptional() {
            return Optional.empty();
        }

        public static void main(String[] args) {
            System.out.println(returnStringOptional());
        }
    }

    @Test
    public void testReturnEmptyOptional() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase2.class)
                        .setMethodToRecord("returnStringOptional")
        );


        ObjectRecord returnValue = root.getReturnValue();
        assertThat(returnValue, instanceOf(OptionalRecord.class));

        OptionalRecord optional = (OptionalRecord) returnValue;
        assertThat(optional.isEmpty(), is(true));
        assertThat(optional.getValue(), nullValue());
    }
}
