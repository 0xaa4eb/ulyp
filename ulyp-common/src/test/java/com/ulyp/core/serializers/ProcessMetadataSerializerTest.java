package com.ulyp.core.serializers;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.bytes.BufferBytesOut;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProcessMetadataSerializerTest {

    @Test
    public void test() {

        ProcessMetadataSerializer serializer = new ProcessMetadataSerializer();

        BufferBytesOut out = new BufferBytesOut(new UnsafeBuffer(new byte[32 * 1024]));

        List<String> classpath = new ArrayList<>();
        classpath.add("/tmp/a/test.jar");
        classpath.add("/tmp/a/test2.jar");
        classpath.add("/tmp/a/test3.jar");

        ProcessMetadata testObject = ProcessMetadata.builder()
                .mainClassName("a.b.c.ZXCxczxc")
                .classpath(classpath)
                .pid(5435L)
                .build();

        serializer.serialize(out, testObject);

        ProcessMetadata deserialized = serializer.deserialize(out.flip());

        Assert.assertEquals(deserialized, testObject);
    }
}