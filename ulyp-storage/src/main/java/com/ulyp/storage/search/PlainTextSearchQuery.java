package com.ulyp.storage.search;

import com.ulyp.core.Method;
import com.ulyp.core.RecordedEnterMethodCall;
import com.ulyp.core.RecordedExitMethodCall;
import com.ulyp.core.Type;
import com.ulyp.core.repository.ReadableRepository;
import com.ulyp.core.util.StringUtils;

public class PlainTextSearchQuery implements SearchQuery {

    private final String textToSearch;

    public PlainTextSearchQuery(String textToSearch) {
        this.textToSearch = textToSearch;
    }

    @Override
    public boolean matches(
            RecordedEnterMethodCall methodCall,
            ReadableRepository<Integer, Type> types,
            ReadableRepository<Integer, Method> methods) {
        return StringUtils.containsIgnoreCase(methodCall.getCallee().toString(), textToSearch)
                || methodCall.getArguments().stream().anyMatch(arg -> StringUtils.containsIgnoreCase(arg.toString(), textToSearch))
                || StringUtils.containsIgnoreCase(methods.get(methodCall.getMethodId()).getName(), textToSearch);
    }

    @Override
    public boolean matches(
            RecordedExitMethodCall methodCall,
            ReadableRepository<Integer, Type> typeResolver,
            ReadableRepository<Integer, Method> methods) {
        return StringUtils.containsIgnoreCase(methodCall.getReturnValue().toString(), textToSearch);
    }
}
