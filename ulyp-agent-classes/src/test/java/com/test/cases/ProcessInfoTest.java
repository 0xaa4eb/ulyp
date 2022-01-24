/*
package com.test.cases;

import com.google.protobuf.ProtocolStringList;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.transport.ProcessInfo;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProcessInfoTest extends AbstractInstrumentationTest {

    @Test
    public void shouldSendValidProcessInfo() {
        List<TCallRecordLogUploadRequest> requests = runForkProcessWithUiAndReturnProtoRequest(
                new ForkProcessBuilder().setMainClassName(X.class)
        );

        ProcessInfo processInfo = requests.get(0).getRecordingInfo().getProcessInfo();

        ProtocolStringList classpath = processInfo.getClasspathList();

        assertThat(classpath.size(), greaterThan(0));

        assertThat(processInfo.getMainClassName(), is("com.test.cases.ProcessInfoTest$X"));
    }

    public static class X {

        public static void main(String[] args) {

        }
    }
}
*/
