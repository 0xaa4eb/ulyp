package com.agent.tests.recorders.java;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.basic.FileRecord;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FileRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldRecordFileObject() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord("returnFile")
        );

        FileRecord value = (FileRecord) root.getReturnValue();

        assertThat(value.getPath(), is(new File("./a/b/somepath").getPath()));
    }

    @Test
    void shouldRecordPathObject() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
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
