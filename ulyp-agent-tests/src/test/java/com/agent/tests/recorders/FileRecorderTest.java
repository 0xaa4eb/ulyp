package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.FileRecord;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordFileObject() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnFile")
        );

        FileRecord value = (FileRecord) root.getReturnValue();

        assertThat(value.getPath(), is(new File("./a/b/somepath").getPath()));
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(returnFile());
        }

        public static File returnFile() {
            return new File("./a/b/somepath");
        }
    }
}
