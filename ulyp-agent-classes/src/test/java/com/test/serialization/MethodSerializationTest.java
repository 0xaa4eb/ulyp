package com.test.serialization;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.MethodInfoList;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import com.ulyp.transport.TMethodInfoDecoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MethodSerializationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldMinimizeAmountMethodDescriptions() {

        List<TCallRecordLogUploadRequest> requests = runForkProcessWithUiAndReturnProtoRequest(
                new ForkProcessBuilder()
                        .setMainClassName(X.class)
                        .setMethodToRecord(MethodMatcher.parse("X.main"))
        );

        MethodInfoList methodDescriptions = new MethodInfoList(requests.get(0).getMethodDescriptionList().getData());

        for (TMethodInfoDecoder methodInfo : methodDescriptions) {
            System.out.println(methodInfo);
        }

        Assert.assertEquals(1, methodDescriptions.size());
    }

    static class X {
        public static void main(String[] args) {
        }
    }
}
