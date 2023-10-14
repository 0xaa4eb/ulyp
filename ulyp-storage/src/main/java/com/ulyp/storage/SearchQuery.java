package com.ulyp.storage;

import com.ulyp.core.Method;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.repository.ReadableRepository;

public interface SearchQuery {

    boolean matches(
            RecordedEnterMethodCall recordedEnterMethodCall,
            ReadableRepository<Integer, Type> types,
            ReadableRepository<Integer, Method> methods
    );

    boolean matches(
            RecordedExitMethodCall recordedExitMethodCall,
            ReadableRepository<Integer, Type> typeResolver,
            ReadableRepository<Integer, Method> methods
    );
}
