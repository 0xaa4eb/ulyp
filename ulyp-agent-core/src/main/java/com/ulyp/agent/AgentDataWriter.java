package com.ulyp.agent;

import com.ulyp.core.*;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedRecordedMethodCallList;
import com.ulyp.core.mem.SerializedTypeList;
import com.ulyp.core.util.ConcurrentArrayList;
import com.ulyp.storage.writer.RecordingDataWriter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AgentDataWriter {

    private final RecordingDataWriter recordingDataWriter;
    @Getter
    private final MethodRepository methodRepository;
    private final AtomicInteger lastIndexOfMethodWritten = new AtomicInteger(-1);
    private final AtomicInteger lastIndexOfTypeWritten = new AtomicInteger(-1);

    public AgentDataWriter(RecordingDataWriter recordingDataWriter, MethodRepository methodRepository) {
        this.recordingDataWriter = recordingDataWriter;
        this.methodRepository = methodRepository;
    }

    public void write(TypeResolver typeResolver, RecordingMetadata recordingMetadata, SerializedRecordedMethodCallList recordedCalls) {

        SerializedMethodList methodsList = new SerializedMethodList();

        ConcurrentArrayList<Method> methods = methodRepository.getMethods();
        int upToExcluding = methods.size() - 1;
        int startFrom = lastIndexOfMethodWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Method method = methods.get(i);
            log.debug("Will write method {} to storage", method);
            methodsList.add(method);
        }
        if (methodsList.size() > 0) {
            recordingDataWriter.write(methodsList);
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

        SerializedTypeList typesList = new SerializedTypeList();
        ConcurrentArrayList<Type> types = typeResolver.values();
        upToExcluding = types.size() - 1;
        startFrom = lastIndexOfTypeWritten.get() + 1;

        for (int i = startFrom; i <= upToExcluding; i++) {
            Type type = types.get(i);
            log.debug("Will write type {} to storage", type);
            typesList.add(type);
        }
        if (typesList.size() > 0) {
            recordingDataWriter.write(typesList);
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

        recordingDataWriter.write(recordingMetadata);
        if (recordedCalls != null) {
            recordingDataWriter.write(recordedCalls);
        }
    }
}
