package com.agent.tests.util;

import com.ulyp.storage.tree.CallRecord;
import com.ulyp.storage.tree.CallRecordTree;
import com.ulyp.storage.tree.Recording;
import com.ulyp.storage.StorageException;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class RecordingResult {

    private final CallRecordTree tree;

    public RecordingResult(CallRecordTree tree) {
        this.tree = tree;
    }

    public List<Recording> aggregateByRecordings() {
        return tree.getRecordings();
    }

    public List<Recording> recordings() {
        return new ArrayList<>(tree.getRecordings());
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
