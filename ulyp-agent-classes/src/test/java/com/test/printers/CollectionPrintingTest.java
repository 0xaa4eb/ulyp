package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.*;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CollectionPrintingTest extends AbstractInstrumentationTest {

    @Test
    public void shouldRecordSimpleItemsProperly() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnArrayListOfString")
                        .recordCollections(CollectionsRecordingMode.ALL)
        );

        CollectionRepresentation collection = (CollectionRepresentation) root.getReturnValue();

        List<ObjectRepresentation> items = collection.getRecordedItems();

        StringObjectRepresentation firstItemRepr = (StringObjectRepresentation) items.get(0);
        Assert.assertEquals("a", firstItemRepr.value());

        StringObjectRepresentation secondItemRepr = (StringObjectRepresentation) items.get(1);
        Assert.assertEquals("b", secondItemRepr.value());
    }

    @Test
    public void shouldRecordSimpleListIfAllCollectionsAreRecorded() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnArrayListOfString")
                        .recordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRepresentation.class));
    }

    @Test
    public void shouldRecordSimpleListIfJavaCollectionsAreRecorded() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnArrayListOfString")
                        .recordCollections(CollectionsRecordingMode.JAVA)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRepresentation.class));
    }

    @Test
    public void shouldNotRecordAnythingIfSpecifiedToRecordOnlyJavaCollection() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnCustomList")
                        .recordCollections(CollectionsRecordingMode.JAVA)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRepresentation.class));
    }

    @Test
    public void shouldRecordCustomListIfSpecifiedAllCollectionsToRecord() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnCustomList")
                        .recordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(CollectionRepresentation.class));
    }

    @Test
    public void shouldFallbackToIdentityIfRecordingFailed() {

        CallRecord root = runForkWithUi(
                new ForkProcessBuilder()
                        .setMainClassName(TestCase.class)
                        .setMethodToRecord("returnThrowingOnIteratorList")
                        .recordCollections(CollectionsRecordingMode.ALL)
        );

        Assert.assertThat(root.getReturnValue(), Matchers.instanceOf(IdentityObjectRepresentation.class));
    }

    static class TestCase {

        public static List<String> returnArrayListOfString() {
            return Arrays.asList("a", "b");
        }

        public static List<String> returnCustomList() {
            return new ArrayList<String>() {
                {
                    add("a");
                    add("b");
                }
            };
        }

        public static List<String> returnThrowingOnIteratorList() {
            return new ArrayList<String>() {
                {
                    add("a");
                    add("b");
                }

                @NotNull
                @Override
                public Iterator<String> iterator() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        public static void main(String[] args) {
            System.out.println(returnArrayListOfString());
            System.out.println(returnCustomList());
            List<String> strings = returnThrowingOnIteratorList();
            System.out.println(System.identityHashCode(strings));
        }
    }
}
