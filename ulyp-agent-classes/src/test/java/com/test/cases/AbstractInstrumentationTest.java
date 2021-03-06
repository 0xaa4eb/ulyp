package com.test.cases;

import com.test.cases.util.*;
import com.ulyp.core.CallRecord;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AbstractInstrumentationTest {

    @NotNull
    protected CallRecord runSubprocessWithUi(TestSettingsBuilder settings) {
        return new RecordingResult(runSubprocessWithUiAndReturnProtoRequest(settings)).getSingleRoot();
    }

    @NotNull
    protected RecordingResult runSubprocess(TestSettingsBuilder settings) {
        return new RecordingResult(runSubprocessWithUiAndReturnProtoRequest(settings));
    }

    protected void assertNoRecording(TestSettingsBuilder settings) {
        Assert.assertThat(runSubprocessWithUiAndReturnProtoRequest(settings), Matchers.empty());
    }

    protected List<TCallRecordLogUploadRequest> runSubprocessWithUiAndReturnProtoRequest(TestSettingsBuilder settings) {
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