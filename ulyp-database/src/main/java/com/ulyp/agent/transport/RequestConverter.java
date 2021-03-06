package com.ulyp.agent.transport;

import com.ulyp.core.CallRecordLog;
import com.ulyp.core.MethodInfo;
import com.ulyp.core.MethodInfoList;
import com.ulyp.core.printers.TypeInfo;
import com.ulyp.core.util.StringUtils;
import com.ulyp.transport.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RequestConverter {

    public static TCallRecordLogUploadRequest convert(CallRecordTreeRequest request) {
        CallRecordLog recordLog = request.getRecordLog();

        TCallRecordLog log = TCallRecordLog.newBuilder()
                .setEnterRecords(recordLog.getEnterRecords().toByteString())
                .setExitRecords(recordLog.getExitRecords().toByteString())
                .build();

        MethodInfoList methodInfoList = new MethodInfoList();
        for (MethodInfo description : request.getMethods()) {
            methodInfoList.add(description);
        }

        TCallRecordLogUploadRequest.Builder requestBuilder = TCallRecordLogUploadRequest.newBuilder();

        for (TypeInfo typeInfo : request.getTypes()) {
            requestBuilder.addDescription(
                    TClassDescription.newBuilder().setId(typeInfo.getId()).setName(typeInfo.getName()).build()
            );
        }

        RecordingInfo recordingInfo = RecordingInfo.newBuilder()
                .setStackTrace(
                        TStackTrace.newBuilder()
                                .addAllElement(
                                        Arrays.stream(recordLog.getStackTrace())
                                                .map(stackTraceElement -> TStackTraceElement.newBuilder()
                                                        .setDeclaringClass(stackTraceElement.getClassName())
                                                        .setMethodName(stackTraceElement.getMethodName())
                                                        .setFileName(StringUtils.nullToEmpty(stackTraceElement.getFileName()))
                                                        .setLineNumber(stackTraceElement.getLineNumber())
                                                        .build())
                                                .collect(Collectors.toList())
                                )
                                .build()
                )
                .setThreadName(recordLog.getThreadName())
                .setThreadId(recordLog.getThreadId())
                .setProcessInfo(com.ulyp.transport.ProcessInfo.newBuilder()
                        .setMainClassName(request.getProcessInfo().getMainClassName())
                        .addAllClasspath(request.getProcessInfo().getClasspath().toList())
                        .build())
                .setRecordingId(request.getRecordLog().getRecordingSessionId())
                .setChunkId(request.getRecordLog().getChunkId())
                .setCreateEpochMillis(recordLog.getEpochMillisCreatedTime())
                .setLifetimeMillis(request.getEndLifetimeEpochMillis() - recordLog.getEpochMillisCreatedTime())
                .build();

        return requestBuilder
                .setRecordingInfo(recordingInfo)
                .setRecordLog(log)
                .setMethodDescriptionList(TMethodDescriptionList.newBuilder().setData(methodInfoList.toByteString()).build())
                .build();
    }
}
