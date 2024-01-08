package com.agent.tests.util;

import com.ulyp.core.util.ByteSize;
import com.ulyp.storage.tree.CallRecord;
import com.ulyp.storage.tree.CallRecordTree;
import com.ulyp.storage.tree.CallRecordTreeBuilder;

import junit.framework.AssertionFailedError;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AbstractInstrumentationTest {

    @NotNull
    protected CallRecord runSubprocessAndReadFile(ForkProcessBuilder settings) {
        return new RecordingResult(runProcess(settings)).getSingleRoot();
    }

    @NotNull
    protected RecordingResult runSubprocess(ForkProcessBuilder settings) {
        return new RecordingResult(runProcess(settings));
    }

    protected CallRecordTree runProcess(ForkProcessBuilder settings) {
        TestUtil.runClassInSeparateJavaProcess(settings);
        if (settings.getOutputFile() == null) {
            return null;
        } else {
            System.out.println("Recording file " + ByteSize.toHumanReadable(settings.getOutputFile().getFile().toPath().toFile().length()));
            CallRecordTree tree = new CallRecordTreeBuilder(settings.getOutputFile().toReader())
                .setReadInfinitely(false)
                .build();
            try {
                tree.getCompleteFuture().get(200, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Assert.fail("Thread is interrupted");
            } catch (ExecutionException ee) {
                throw new RuntimeException("Failed", ee);
            } catch (TimeoutException e) {
                Assert.fail("Timed out waiting for process to finish");
            }
            System.out.println("Got " + tree.getRecordings().size() + " recordings");
            return tree;
        }
    }
}