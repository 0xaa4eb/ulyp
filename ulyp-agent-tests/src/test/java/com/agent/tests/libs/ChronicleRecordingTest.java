package com.agent.tests.libs;

import com.agent.tests.util.*;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.tree.CallRecord;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.agent.tests.util.DebugCallRecordTreePrinter.printTree;
import static com.agent.tests.util.RecordingMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;

class ChronicleRecordingTest extends AbstractInstrumentationTest {

    @Test
    void testChronicleLibraryWithSingleMessage() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ChronicleRecordingTest.TestCase.main"))
                        .withInstrumentedPackages()
        );

        CallRecord root = recordingResult.getSingleRoot();
        String errMsg = printTree(root);

        assertThat(errMsg, root.getSubtreeSize(), greaterThan(100));

        assertThat(errMsg, root, allOf(
                hasChildCall(hasMethod(hasName("startExcerpt"))),
                hasChildCall(hasMethod(hasName("writeInt"))),
                hasChildCall(hasMethod(hasName("readInt")))
        ));
    }

    @Test
    void testChronicleLibraryWithSingleMessage2() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMain(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ChronicleRecordingTest.TestCase.main"))
                        .withInstrumentedPackages()
                        .withSystemProp(SystemProp.builder().key("messageCount").value("100").build())
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(printTree(singleRoot), singleRoot.getSubtreeSize(), greaterThan(3000));

        assertThat(printTree(singleRoot), singleRoot, allOf(
                hasChildCall(hasMethod(hasName("startExcerpt"))),
                hasChildCall(hasMethod(hasName("writeInt"))),
                hasChildCall(hasMethod(hasName("readInt")))
        ));
    }

    public static class TestCase {

        public static void main(String[] args) throws IOException {
            int msgCount = Integer.parseInt(System.getProperty("messageCount", "2"));

            File queueDir = Files.createTempDirectory("chronicle-queue").toFile();
            Chronicle chronicle = ChronicleQueueBuilder.indexed(queueDir).build();

            ExcerptAppender appender = chronicle.createAppender();

            for (int i = 0; i < msgCount; i++) {
                appender.startExcerpt();
                appender.writeInt(i);
                appender.writeLong(2L * i);
                appender.writeDouble(i * 5.0);
                appender.finish();
            }

            ExcerptTailer tailer = chronicle.createTailer();
            int i = 0;
            while (tailer.nextIndex()) {
                if (tailer.readInt() != i) {
                    throw new RuntimeException("consistency check");
                }
                if (tailer.readLong() != 2L * i) {
                    throw new RuntimeException("consistency check");
                }
                if (Math.abs(tailer.readDouble() - i * 5.0) > 1e-8) {
                    throw new RuntimeException("consistency check");
                }
                i++;
            }
            if (i != msgCount) {
                throw new RuntimeException("got " + i + " messages from tailer, expected " + msgCount);
            }
            tailer.finish();
        }
    }
}
