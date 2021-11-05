package com.perf.agent.benchmarks;

import com.perf.agent.benchmarks.impl.SpringHibernateSmallBenchmark;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.ulyp.core.CallEnterRecordList;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.transport.TCallEnterRecordDecoder;
import com.ulyp.transport.TCallRecordLogUploadRequest;
import org.HdrHistogram.Histogram;
import org.agrona.concurrent.UnsafeBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Prints out total bytes serialized by each printer
 */
public class RecorderMetricsBenchmarksMain {

    public static void main(String[] args) throws Exception {

        runBench(SpringHibernateSmallBenchmark.class);
    }

    private static void runBench(Class<? extends Benchmark> benchmarkClazz) throws Exception {
        Benchmark benchmark = benchmarkClazz.newInstance();

        for (BenchmarkProfile profile : benchmark.getProfiles()) {
            if (!profile.shouldWriteRecording()) {
                // If nothing is sent to UI, then there is nothing to measure
                continue;
            }

            TCallRecordLogUploadRequest request = run(benchmarkClazz, profile);
            CallEnterRecordList enterRecordDecoders = new CallEnterRecordList(request.getRecordLog().getEnterRecords());

            System.out.println("Total size: "  + request.getRecordLog().getSerializedSize());

            Map<Byte, Long> sizeMap = new HashMap<>();
            Map<Byte, Long> countMap = new HashMap<>();

            for (TCallEnterRecordDecoder decoder : enterRecordDecoders) {
                TCallEnterRecordDecoder.ArgumentsDecoder arguments = decoder.arguments();
                while (arguments.hasNext()) {
                    arguments = arguments.next();

                    UnsafeBuffer buffer = new UnsafeBuffer();
                    arguments.wrapValue(buffer);

                    sizeMap.put(arguments.printerId(), sizeMap.getOrDefault(arguments.printerId(), 0L) + buffer.capacity());
                    countMap.put(arguments.printerId(), countMap.getOrDefault(arguments.printerId(), 0L) + 1);
                }
            }

            sizeMap.forEach(
                    (k, v) -> {
                        double totalSizeDivided = (v * 1.0 / 1000.0);
                        long count = countMap.getOrDefault(k, 0L);

                        System.out.println(
                                ObjectBinaryPrinterType.printerForId(k).toString() + "    ->    " +
                                        "total size = " + totalSizeDivided + " / count = " + countMap.get(k) +
                                        " ~ avg " + (v * 1.0 / count));
                    }
            );

//            CallExitRecordList exitRecordDecoders = new CallExitRecordList(recordLog.getExitRecords());
//            MethodDescriptionList methodDescriptions = new MethodDescriptionList(request.getMethodDescriptionList().getData());
//            List<TClassDescription> classDescriptions = request.getDescriptionList();
//
//            Long2ObjectMap<TMethodInfoDecoder> methodDescriptionMap = new Long2ObjectOpenHashMap<>();
//            Iterator<TMethodInfoDecoder> iterator = methodDescriptions.copyingIterator();
//            while (iterator.hasNext()) {
//                TMethodInfoDecoder methodDescription = iterator.next();
//                methodDescriptionMap.put(methodDescription.id(), methodDescription);
//            }
        }
    }

    private static TCallRecordLogUploadRequest run(Class<?> benchmarkClazz, BenchmarkProfile profile) {
        BenchmarkProcessRunner.runClassInSeparateJavaProcess(benchmarkClazz, profile);
        return profile.getOutputFile().read().get(0);
    }

    private static Histogram emptyHistogram() {
        return new Histogram(1, 1_000_000_000, 2);
    }
}
