package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.OptionalRecord;
import com.ulyp.core.recorders.StringObjectRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class OptionalRecorderTest extends AbstractInstrumentationTest {

    @Test
    void testReturnOptionalWithSomeString() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnStringOptional")
        );


        ObjectRecord returnValue = root.getReturnValue();
        assertThat(returnValue, instanceOf(OptionalRecord.class));

        OptionalRecord optional = (OptionalRecord) returnValue;
        assertThat(optional.isEmpty(), is(false));
        assertThat(optional.getValue(), instanceOf(StringObjectRecord.class));

        StringObjectRecord string = (StringObjectRecord) optional.getValue();
        assertThat(string.value(), is("ABC"));
    }

    @Test
    void testReturnEmptyOptional() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase2.class)
                        .withMethodToRecord("returnStringOptional")
        );


        ObjectRecord returnValue = root.getReturnValue();
        assertThat(returnValue, instanceOf(OptionalRecord.class));

        OptionalRecord optional = (OptionalRecord) returnValue;
        assertThat(optional.isEmpty(), is(true));
        assertThat(optional.getValue(), nullValue());
    }

    static class TestCase {

        public static Optional<String> returnStringOptional() {
            return Optional.of("ABC");
        }

        public static void main(String[] args) {
            System.out.println(returnStringOptional());
        }
    }

    static class TestCase2 {

        public static Optional<String> returnStringOptional() {
            return Optional.empty();
        }

        public static void main(String[] args) {
            System.out.println(returnStringOptional());
        }
    }
}
