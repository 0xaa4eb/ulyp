package com.agent.tests.util;

import com.ulyp.storage.CallRecord;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RecordingResult {

    private final StorageReader reader;

    public RecordingResult(StorageReader reader) {
        this.reader = reader;
    }

    public List<Recording> aggregateByRecordings() {
        return reader.getRecordings();
    }

    public List<Recording> recordings() {
        return new ArrayList<>(reader.getRecordings());
    }

    public CallRecord getSingleRoot() throws StorageException {
        assertHasSingleRecording();

        return aggregateByRecordings().iterator().next().getRoot();
    }

    public void assertHasRecordings() {
        List<Recording> recordings = aggregateByRecordings();
        Assert.assertTrue(
            "Expect to have at least one recording session, but got " + recordings.size(),
            !recordings.isEmpty()
        );
    }

    public void assertHasSingleRecording() {
        List<Recording> recordings = aggregateByRecordings();
        Assert.assertEquals(
                "Expect single recording session, but got " + recordings.size(),
                1, recordings.size()
        );
    }

    public void assertNoRecordings() {
        List<Recording> recordings = aggregateByRecordings();
        Assert.assertEquals(
            "Expect no recording sessions, but got " + recordings.size(),
            0, recordings.size()
        );
    }

    public void assertRecordingSessionCount(int count) {
        List<Recording> recordings = aggregateByRecordings();
        Assert.assertEquals(
                "Expect " + count + " recording session, but got " + recordings.size(),
                count, recordings.size()
        );
    }

    public void assertIsEmpty() {
        Assert.assertNull(reader.getProcessMetadataFuture().getNow(null));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Recording recording : recordings()) {
            builder.append("Recording ").append(recording.getId()).append(":\n");
            builder.append(DebugCallRecordTreePrinter.printTree(getSingleRoot()));
        }
        return builder.toString();
    }
}
