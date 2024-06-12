package com.ulyp.storage.search;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;

public interface SearchResultListener {

    void onStart();

    void onMatch(int recordingId, RecordedEnterMethodCall enterMethodCall);

    void onMatch(int recordingId, RecordedExitMethodCall exitMethodCall);

    void onEnd();
}
