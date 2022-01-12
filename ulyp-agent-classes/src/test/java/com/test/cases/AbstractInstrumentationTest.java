package com.test.cases;

import com.test.cases.util.*;
import com.ulyp.core.CallRecord;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import junit.framework.AssertionFailedError;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AbstractInstrumentationTest {

    @NotNull
    protected CallRecord runForkWithUi(ForkProcessBuilder settings) {
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
        Assert.assertThat(runForkProcessWithUiAndReturnProtoRequest(settings), Matchers.empty());
    }

    protected List<TCallRecordLogUploadRequest> runForkProcessWithUiAndReturnProtoRequest(ForkProcessBuilder settings) {
        TestUtil.runClassInSeparateJavaProcess(settings);
        if (settings.getOutputFile() == null) {
            return Collections.emptyList();
        } else {
            List<TCallRecordLogUploadRequest> requests = settings.getOutputFile().read();
            requests.sort(Comparator.comparingLong(r -> r.getRecordingInfo().getChunkId()));
            System.out.println("Got " + requests.size() + " chunks from process");
            return requests;
        }
    }
}