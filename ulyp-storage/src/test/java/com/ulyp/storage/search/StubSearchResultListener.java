package com.ulyp.storage.search;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.RecordedMethodCall;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StubSearchResultListener implements SearchResultListener {

    private final List<RecordedMethodCall> matchedCalls = new ArrayList<>();

    @Override
    public void onStart() {

    }

    @Override
    public void onMatch(int recordingId, RecordedEnterMethodCall enterMethodCall) {
        matchedCalls.add(enterMethodCall);
    }

    @Override
    public void onMatch(int recordingId, RecordedExitMethodCall exitMethodCall) {
        matchedCalls.add(exitMethodCall);
    }

    @Override
    public void onEnd() {

    }
}
