package com.agent.tests.libs;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.DebugCallRecordTreePrinter;
import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.CallRecord;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.ExcerptTailer;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ChronicleRecordingTest extends AbstractInstrumentationTest {

    @Test
    public void testChronicleLibraryWithSingleMessage() {

        RecordingResult recordingResult = runForkProcess(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord(MethodMatcher.parse("**.ChronicleRecordingTest.TestCase.main"))
                        .setInstrumentedPackages()
        );

        CallRecord singleRoot = recordingResult.getSingleRoot();

        assertThat(
                DebugCallRecordTreePrinter.printTree(singleRoot),
                singleRoot.getSubtreeSize(),
                greaterThan(400)
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
                appender.writeUTF("Hello chronicle");
                appender.writeInt(4324);
                appender.writeLong(54563463L);
                appender.writeDouble(234324.43);
                appender.finish();
            }

            ExcerptTailer tailer = chronicle.createTailer();
            while (tailer.nextIndex()) {
                System.out.println(tailer.readUTF());
                System.out.println(tailer.readInt());
                System.out.println(tailer.readLong());
                System.out.println(tailer.readDouble());
            }
            tailer.finish();
        }
    }
}
