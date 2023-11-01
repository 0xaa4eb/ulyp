package com.ulyp.storage;

import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;

public interface R {

    RecordedEnterMethodCall readEnterMethodCall(long address);

    /**
     *
     */
    RecordedExitMethodCall readExitMethodCall(long address);
}
