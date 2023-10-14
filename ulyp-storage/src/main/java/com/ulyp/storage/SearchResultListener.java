package com.ulyp.storage;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;

public interface SearchResultListener {

    void onStart();

    void onMatch(RecordedEnterMethodCall enterMethodCall);

    void onMatch(RecordedExitMethodCall exitMethodCall);

    void onEnd();
}
