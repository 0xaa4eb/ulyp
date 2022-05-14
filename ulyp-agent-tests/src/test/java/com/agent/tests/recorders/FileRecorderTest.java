package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.FileRecord;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileRecorderTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordFileObject() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnFile")
        );

        FileRecord value = (FileRecord) root.getReturnValue();

        assertThat(value.getPath(), is(new File("./a/b/somepath").getPath()));
    }

    @Test
    public void shouldRecordPathObject() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord("returnPath")
        );

        FileRecord value = (FileRecord) root.getReturnValue();

        assertThat(value.getPath(), is(Paths.get("a", "b", "somefile").toString()));
    }

    public static class TestCase {

        public static void main(String[] args) {
            System.out.println(returnFile());
            System.out.println(returnPath());
        }

        public static File returnFile() {
            return new File("./a/b/somepath");
        }

        public static Path returnPath() {
            return Paths.get("a", "b", "somefile");
        }
    }
}
