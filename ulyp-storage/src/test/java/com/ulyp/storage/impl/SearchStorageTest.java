package com.ulyp.storage.impl;

import com.ulyp.core.*;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.mem.TypeList;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.storage.*;
import com.ulyp.storage.impl.util.MemSearchResultListener;
import com.ulyp.storage.util.PlainTextSearchQuery;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

@Ignore
public class SearchStorageTest {

    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();
    private final Type type = typeResolver.get(T.class);
    private final Method method = Method.builder()
            .declaringType(type)
            .name("foo")
            .id(1000)
            .isConstructor(false)
            .isStatic(false)
            .returnsSomething(true)
            .build();
    private final TypeList types = new TypeList();
    private final MethodList methods = new MethodList();
    private RecordingDataReader reader;
    private RecordingDataWriter writer;

    @Before
    public void setUp() throws IOException {
        File file = Files.createTempFile(SearchStorageTest.class.getSimpleName(), "a").toFile();
        this.reader = new AsyncFileRecordingDataReader(ReaderSettings.builder().file(file).autoStartReading(true).build());
        this.writer = new FileRecordingDataWriter(file);

        types.add(type);
        methods.add(method);
    }

    @After
    public void tearDown() {
        reader.close();
    }

    @Test
    public void testSearchByMethodName() throws Exception {
        RecordedMethodCallList methodCalls = new RecordedMethodCallList(1);
        methodCalls.addEnterMethodCall(0, method, typeResolver, new T(), new Object[]{"ABC"});
        methodCalls.addExitMethodCall(0, typeResolver, false, "GHJ");

        writer.write(types);
        writer.write(methods);
        writer.write(RecordingMetadata.builder().id(1).build());
        writer.write(methodCalls);
        writer.close();

        reader.getFinishedReadingFuture().get(1, TimeUnit.MINUTES);

        assertEquals(1, search(new PlainTextSearchQuery("foo")).size());

        assertEquals(0, search(new PlainTextSearchQuery("fob")).size());
    }

    private List<RecordedMethodCall> search(SearchQuery searchQuery) throws Exception {
        MemSearchResultListener searchResultListener = new MemSearchResultListener();
        CompletableFuture<Void> searchFuture = reader.initiateSearch(searchQuery, searchResultListener);
        searchFuture.get(1, TimeUnit.MINUTES);
        return searchResultListener.getMatchedCalls();
    }

    public static class T {
        public String foo(String in) {
            return in;
        }
    }
}