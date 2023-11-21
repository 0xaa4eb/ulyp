package com.perf.agent.benchmarks.tree;

import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import com.perf.agent.benchmarks.BenchmarkScenarioBuilder;
import com.perf.agent.benchmarks.proc.BenchmarkProcessRunner;
import com.perf.agent.benchmarks.proc.OutputFile;
import com.perf.agent.benchmarks.recording.SpringHibernateMediumBenchmark;
import com.ulyp.core.util.ByteSize;
import com.ulyp.core.util.MethodMatcher;
import com.ulyp.storage.reader.FileRecordingDataReader;
import com.ulyp.storage.reader.FileRecordingDataReaderBuilder;
import com.ulyp.storage.tree.CallRecordTree;
import com.ulyp.storage.tree.CallRecordTreeBuilder;
import com.ulyp.storage.tree.RocksdbIndex;

public class BuildTreeBenchmark {

    public static void main(String[] args) throws Exception {

        OutputFile recordingFile = new OutputFile("ulyp-benchmark", "dat");
        System.out.println("Recording output file is " + recordingFile);

        BenchmarkProcessRunner.runClassInSeparateJavaProcess(
            SpringHibernateMediumBenchmark.class,
            new BenchmarkScenarioBuilder()
                .withMethodToRecord(new MethodMatcher(SpringHibernateMediumBenchmark.class, "main"))
                .withOutputFile(recordingFile)
                .build()
        );

        System.out.println("Size of recording file is " + new ByteSize(recordingFile.size()));

        RocksdbIndex rocksdbIndex = new RocksdbIndex(Files.createTempDirectory("ulyp-bench-index"));

        long startTime = System.currentTimeMillis();
        CallRecordTree callRecordTree;
        try (FileRecordingDataReader reader = new FileRecordingDataReaderBuilder(recordingFile.getFile().toFile()).build()) {
            callRecordTree = new CallRecordTreeBuilder(reader)
                .setIndexSupplier(() -> rocksdbIndex)
                .setReadInfinitely(false)
                .build();
            callRecordTree.getCompleteFuture().get(120, TimeUnit.SECONDS);

            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println(callRecordTree.getRecordings().size());
            System.out.println("Took " + elapsed / 1000 + " sec");
        }

    }
}
