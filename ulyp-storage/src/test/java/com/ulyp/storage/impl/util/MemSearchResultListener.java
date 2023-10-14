package com.ulyp.storage.impl.util;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.RecordedMethodCall;
import com.ulyp.storage.SearchResultListener;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MemSearchResultListener implements SearchResultListener {

    private final List<RecordedMethodCall> matchedCalls = new ArrayList<>();

    @Override
    public void onStart() {

    }

    @Override
    public void onMatch(RecordedEnterMethodCall enterMethodCall) {
        matchedCalls.add(enterMethodCall);
    }

    @Override
    public void onMatch(RecordedExitMethodCall exitMethodCall) {
        matchedCalls.add(exitMethodCall);
    }

    @Override
    public void onEnd() {

    }
}
