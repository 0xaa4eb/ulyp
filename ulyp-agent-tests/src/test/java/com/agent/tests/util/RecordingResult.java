package com.agent.tests.util;

import com.ulyp.storage.CallRecord;
import com.ulyp.storage.Recording;
import com.ulyp.storage.StorageException;
import com.ulyp.storage.StorageReader;
import org.junit.Assert;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecordingResult {

    private final StorageReader reader;

    public RecordingResult(StorageReader reader) {
        this.reader = reader;
    }

    /*
    public Map<Integer, Recording> aggregateByThread() throws StorageException {
        Map<Integer, CallRecordDatabase> recordingIdToRequest = new HashMap<>();

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

        for (TCallRecordLogUploadRequest request : reader) {
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
                                    new ObjectRecorder[] { ObjectRecorderType.IDENTITY_RECORDER.getInstance()},
                                    Thread.currentThread(),
                                    new Object[]{}
                            );
                            CallExitRecordList exitRecords = new CallExitRecordList();
                            newDatabase.persistBatch(enterRecords, exitRecords);
                            return newDatabase;
                        } catch (StorageException e) {
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
    */

    public Map<Integer, Recording> aggregateByRecordings() {
        return reader.availableRecordings().stream().collect(Collectors.toMap(Recording::getId, Function.identity()));
    }

    public List<Recording> recordings() {
        return reader.availableRecordings().stream().collect(Collectors.toList());
    }

    public CallRecord getSingleRoot() throws StorageException {
        assertSingleRecordingSession();

        return aggregateByRecordings().entrySet().iterator().next().getValue().getRoot();
    }

    public void assertSingleRecordingSession() {
        Map<Integer, Recording> request = aggregateByRecordings();
        Assert.assertEquals("Expect single recording session, but got " + request.size(), 1, request.size());
    }

    public void assertRecordingSessionCount(int count) {
        Map<Integer, Recording> request = aggregateByRecordings();
        Assert.assertEquals("Expect " + count + " recording session, but got " + request.size(), count, request.size());
    }

    public void assertIsEmpty() {
        Assert.assertNull(reader.getProcessMetadata().getNow(null));
    }
}
