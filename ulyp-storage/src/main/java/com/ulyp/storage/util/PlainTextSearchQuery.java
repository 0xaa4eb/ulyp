package com.ulyp.storage.util;

import com.ulyp.core.Method;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.util.StringUtils;
import com.ulyp.storage.SearchQuery;

public class PlainTextSearchQuery implements SearchQuery {

    private final String searchQuery;

    public PlainTextSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    public boolean matches(
            RecordedEnterMethodCall recordedEnterMethodCall,
            ReadableRepository<Integer, Type> types,
            ReadableRepository<Integer, Method> methods) {
        return StringUtils.containsIgnoreCase(recordedEnterMethodCall.getCallee().toRecord(types).toString(), searchQuery)
                || recordedEnterMethodCall.getArguments().stream().anyMatch(arg -> StringUtils.containsIgnoreCase(arg.toRecord(types).toString(), searchQuery))
                || StringUtils.containsIgnoreCase(methods.get(recordedEnterMethodCall.getMethodId()).getName(), searchQuery);
    }

    @Override
    public boolean matches(
            RecordedExitMethodCall recordedExitMethodCall,
            ReadableRepository<Integer, Type> typeResolver,
            ReadableRepository<Integer, Method> methods) {
        return StringUtils.containsIgnoreCase(recordedExitMethodCall.getReturnValue().toRecord(typeResolver).toString(), searchQuery);
    }
}
