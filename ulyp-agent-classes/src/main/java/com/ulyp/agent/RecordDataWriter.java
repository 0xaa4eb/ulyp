package com.ulyp.agent;

import java.util.concurrent.atomic.AtomicInteger;

import com.ulyp.core.CallRecordBuffer;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import com.ulyp.core.RecordingMetadata;
import com.ulyp.core.Type;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.ConcurrentArrayList;
import com.ulyp.storage.StorageWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * Gathers all necessary recording data like types, calls, methods and passes it to the storage layer.
 * It also tracks what methods and types have already been written to the underlying storage by maintaining watermarks.
 * Sadly, moving that logic to storage level is not feasible right now
 */
@Slf4j
public class RecordDataWriter {

    private final StorageWriter storageWriter;
    private final MethodRepository methodRepository;
    private final AtomicInteger lastIndexOfMethodWritten = new AtomicInteger(-1);
    private final AtomicInteger lastIndexOfMethodToRecordWritten = new AtomicInteger(-1);
    private final AtomicInteger lastIndexOfTypeWritten = new AtomicInteger(-1);

    public RecordDataWriter(StorageWriter storageWriter, MethodRepository methodRepository) {
        this.storageWriter = storageWriter;
        this.methodRepository = methodRepository;
    }

    public void write(TypeResolver typeResolver, RecordingMetadata recordingMetadata, CallRecordBuffer callRecordBuffer) {

        MethodList methodsList = new MethodList();

        ConcurrentArrayList<Method> methods = methodRepository.getMethods();
        int upToExcluding = methods.size() - 1;
        int startFrom = lastIndexOfMethodWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Method method = methods.get(i);
            log.debug("Will write {} to storage", method);
            methodsList.add(method);
        }
        if (methodsList.size() > 0) {
            storageWriter.write(methodsList);
            for (;;) {
                int currentIndex = lastIndexOfMethodWritten.get();
                if (currentIndex < upToExcluding) {
                    if (lastIndexOfMethodWritten.compareAndSet(currentIndex, upToExcluding)) {
                        break;
                    }
                } else {
                    // Someone else must have written methods already
                    break;
                }
            }
        }

        methodsList = new MethodList();
        methods = methodRepository.getRecordingStartMethods();
        upToExcluding = methods.size() - 1;
        startFrom = lastIndexOfMethodToRecordWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Method method = methods.get(i);
            log.debug("Will write {} to storage", method);
            methodsList.add(method);
        }
        if (methodsList.size() > 0) {
            storageWriter.write(methodsList);
            for (;;) {
                int currentIndex = lastIndexOfMethodToRecordWritten.get();
                if (currentIndex < upToExcluding) {
                    if (lastIndexOfMethodToRecordWritten.compareAndSet(currentIndex, upToExcluding)) {
                        break;
                    }
                } else {
                    // Someone else must have written methods already
                    break;
                }
            }
        }

        TypeList typesList = new TypeList();
        ConcurrentArrayList<Type> types = typeResolver.getAllResolvedAsConcurrentList();
        upToExcluding = types.size() - 1;
        startFrom = lastIndexOfTypeWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Type type = types.get(i);
            log.debug("Will write {} to storage", type);
            typesList.add(type);
        }
        if (typesList.size() > 0) {
            storageWriter.write(typesList);
            for (;;) {
                int currentIndex = lastIndexOfTypeWritten.get();
                if (currentIndex < upToExcluding) {
                    if (lastIndexOfTypeWritten.compareAndSet(currentIndex, upToExcluding)) {
                        break;
                    }
                } else {
                    // Someone else must have written methods already
                    break;
                }
            }
        }

        storageWriter.write(recordingMetadata);
        storageWriter.write(callRecordBuffer.getRecordedCalls());
    }
}
