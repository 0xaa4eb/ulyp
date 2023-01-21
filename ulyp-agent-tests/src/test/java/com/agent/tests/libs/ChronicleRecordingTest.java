package com.agent.tests.libs;

import com.agent.tests.util.*;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.CallRecord;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.ExcerptAppender;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.agent.tests.util.RecordingMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ChronicleRecordingTest extends AbstractInstrumentationTest {

    @Test
    public void testChronicleLibraryWithSingleMessage() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ChronicleRecordingTest.TestCase.main"))
                        .withInstrumentedPackages()
// TODO fix this
//                        .withRecordConstructors()
        );

        CallRecord root = recordingResult.getSingleRoot();
        String errMsg = DebugCallRecordTreePrinter.printTree(root);

        assertThat(errMsg, root.getSubtreeSize(), greaterThan(100));

        assertThat(errMsg, root,
                allOf(
                        hasChildCall(hasMethod(hasName("startExcerpt"))),
                        hasChildCall(hasMethod(hasName("writeInt"))),
                        hasChildCall(hasMethod(hasName("readInt")))
                )
        );
    }

    @Test
    public void testChronicleLibraryWithSingleMessage2() {

        RecordingResult recordingResult = runSubprocess(
                new ForkProcessBuilder()
                        .withMainClassName(TestCase.class)
                        .withMethodToRecord(MethodMatcher.parse("**.ChronicleRecordingTest.TestCase.main"))
                        .withInstrumentedPackages()
                        .withSystemProp(SystemProp.builder().key("messageCount").value("6").build())
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(
                DebugCallRecordTreePrinter.printTree(singleRoot),
                singleRoot.getSubtreeSize(),
                greaterThan(300)
        );

        assertThat(
                DebugCallRecordTreePrinter.printTree(singleRoot),
                singleRoot,
                allOf(
                        hasChildCall(hasMethod(hasName("startExcerpt"))),
                        hasChildCall(hasMethod(hasName("writeInt"))),
                        hasChildCall(hasMethod(hasName("readInt")))
                )
        );
    }

    public static class TestCase {

        public static void main(String[] args) throws IOException {
            int msgCount = Integer.parseInt(System.getProperty("messageCount", "2"));

            File queueDir = Files.createTempDirectory("chronicle-queue").toFile();
            Chronicle chronicle = ChronicleQueueBuilder.indexed(queueDir).build();

            ExcerptAppender appender = chronicle.createAppender();

            for (int i = 0; i < msgCount; i++) {
                appender.startExcerpt();
                appender.writeInt(4324);
                appender.writeLong(54563463L);
                appender.writeDouble(234324.43);
                appender.finish();
            }

            ExcerptTailer tailer = chronicle.createTailer();
            while (tailer.nextIndex()) {
                System.out.println(tailer.readInt());
                System.out.println(tailer.readLong());
                System.out.println(tailer.readDouble());
            }
            tailer.finish();
        }
    }
}
