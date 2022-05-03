package com.agent.tests.util;

import com.agent.tests.util.ForkProcessBuilder;
import com.agent.tests.util.RecordingResult;
import com.agent.tests.util.TestUtil;
import com.ulyp.storage.CallRecord;
import com.ulyp.storage.StorageReader;
import junit.framework.AssertionFailedError;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public class AbstractInstrumentationTest {

    @NotNull
    protected CallRecord run(ForkProcessBuilder settings) {
        try {
            return new RecordingResult(runForkProcessWithUiAndReturnProtoRequest(settings)).getSingleRoot();
        } catch (Exception e) {
            throw new AssertionFailedError(e.getMessage());
        }
    }

    @NotNull
    protected RecordingResult runForkProcess(ForkProcessBuilder settings) {
        return new RecordingResult(runForkProcessWithUiAndReturnProtoRequest(settings));
    }

    protected void assertNoRecording(ForkProcessBuilder settings) {
        Assert.assertThat(runForkProcessWithUiAndReturnProtoRequest(settings).availableRecordings(), Matchers.empty());
    }

    protected StorageReader runForkProcessWithUiAndReturnProtoRequest(ForkProcessBuilder settings) {
        TestUtil.runClassInSeparateJavaProcess(settings);
        if (settings.getOutputFile() == null) {
            return StorageReader.empty();
        } else {
            StorageReader reader = settings.getOutputFile().toReader();
            System.out.println("Got " + reader.availableRecordings().size() + " recordings");
            return reader;
        }
    }
}