package com.test.cases.util;

import com.ulyp.core.*;
import com.ulyp.core.impl.LegacyFileBasedCallRecordDatabase;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.RecorderType;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.StoreException;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordingResult {

    private final List<TCallRecordLogUploadRequest> requests;

    public RecordingResult(List<TCallRecordLogUploadRequest> requests) {
        this.requests = requests;
    }

    public Map<Long, CallRecordDatabase> aggregateByThread() throws StoreException {
        Map<Long, CallRecordDatabase> recordingIdToRequest = new HashMap<>();

        MethodInfoDatabase methodInfoDatabase = new MethodInfoDatabase();
        TypeInfoDatabase typeInfoDatabase = new TypeInfoDatabase();
        TypeResolver typeResolver = new ReflectionBasedTypeResolver();

        MethodInfoList methodInfos = new MethodInfoList();
        Method threadRunMethod = Method.builder()
                .id(Integer.MAX_VALUE)
                .name("run")
                .returnsSomething(false)
                .isStatic(false)
                .isConstructor(false)
                .declaringType(typeResolver.get(Thread.class))
                .build();
        methodInfos.add(threadRunMethod);
        methodInfoDatabase.addAll(methodInfos);

        for (TCallRecordLogUploadRequest request : requests) {
            CallRecordDatabase database = recordingIdToRequest.computeIfAbsent(
                    request.getRecordingInfo().getThreadId(),
                    id -> {
                        CallRecordDatabase newDatabase = null;
                        try {
                            newDatabase = new LegacyFileBasedCallRecordDatabase(methodInfoDatabase, typeInfoDatabase);

                            CallEnterRecordList enterRecords = new CallEnterRecordList();
                            enterRecords.add(
                                    0,
                                    threadRunMethod.getId(),
                                    typeResolver,
                                    new ObjectRecorder[] { RecorderType.IDENTITY_RECORDER.getInstance()},
                                    Thread.currentThread(),
                                    new Object[]{}
                            );
                            CallExitRecordList exitRecords = new CallExitRecordList();
                            newDatabase.persistBatch(enterRecords, exitRecords);
                            return newDatabase;
                        } catch (StoreException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            methodInfoDatabase.addAll(new MethodInfoList(request.getMethodDescriptionList().getData()));
            typeInfoDatabase.addAll(request.getDescriptionList());

            database.persistBatch(new CallEnterRecordList(request.getRecordLog().getEnterRecords()), new CallExitRecordList(request.getRecordLog().getExitRecords()));
        }

        return recordingIdToRequest;
    }

    public Map<Long, CallRecordDatabase> aggregateByRecordings() {
        Map<Long, CallRecordDatabase> recordingIdToRequest = new HashMap<>();

        MethodInfoDatabase methodInfoDatabase = new MethodInfoDatabase();
        TypeInfoDatabase typeInfoDatabase = new TypeInfoDatabase();

        for (TCallRecordLogUploadRequest request : requests) {
            CallRecordDatabase database = recordingIdToRequest.computeIfAbsent(
                    request.getRecordingInfo().getRecordingId(),
                    id -> {
                        return new LegacyFileBasedCallRecordDatabase(methodInfoDatabase, typeInfoDatabase);
                    }
            );

            methodInfoDatabase.addAll(new MethodInfoList(request.getMethodDescriptionList().getData()));
            typeInfoDatabase.addAll(request.getDescriptionList());

            database.persistBatch(new CallEnterRecordList(request.getRecordLog().getEnterRecords()), new CallExitRecordList(request.getRecordLog().getExitRecords()));
        }

        return recordingIdToRequest;
    }

    public CallRecord getSingleRoot() {
        assertSingleRecordingSession();

        return aggregateByRecordings().entrySet().iterator().next().getValue().getRoot();
    }

    public void assertSingleRecordingSession() {
        Map<Long, CallRecordDatabase> request = aggregateByRecordings();
        Assert.assertEquals("Expect single recording session, but got " + request.size(), 1, request.size());
    }

    public void assertRecordingSessionCount(int count) {
        Map<Long, CallRecordDatabase> request = aggregateByRecordings();
        Assert.assertEquals("Expect " + count + " recording session, but got " + request.size(), count, request.size());
    }
}
