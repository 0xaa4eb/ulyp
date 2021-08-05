package com.test.cases;

import com.test.cases.util.RecordingResult;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.CallRecordDatabase;
import com.ulyp.database.DatabaseException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ByThreadRecordingTest extends AbstractInstrumentationTest {

    @Test
    public void shouldAggregateByThread() throws DatabaseException {

        RecordingResult recordingResult = runForkProcess(new ForkProcessBuilder().setMainClassName(X.class).setMethodToRecord("*"));

        Map<Long, CallRecordDatabase> longCallRecordDatabaseMap = recordingResult.aggregateByThread();

        Assert.assertEquals(2, longCallRecordDatabaseMap.size());

        CallRecordDatabase callRecordDatabase = longCallRecordDatabaseMap.values().stream().filter(db -> db.countAll() == 3L).findFirst().orElseThrow(
                () -> new AssertionError("Could not find db with 3 call recods")
        );

        CallRecord root = callRecordDatabase.getRoot();

        Assert.assertThat(root.getMethodName(), is("run"));
        Assert.assertThat(root.getClassName(), is("java.lang.Thread"));
        Assert.assertThat(root.getChildren(), hasSize(2));
    }

    static class X {

        private static int a() {
            return 1;
        }

        private static int b() {
            return 2;
        }

        public static void main(String[] args) throws InterruptedException {
            Thread thread = new Thread(
                    () -> {
                        System.out.println(a());
                        System.out.println(b());
                    }
            );
            thread.start();
            thread.join();
        }
    }
}
