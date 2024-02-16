package com.ulyp.agent.bootstrap;

import com.ulyp.core.metrics.Metrics;
import com.ulyp.storage.writer.RecordingDataWriter;

import java.nio.file.Paths;

public class RecordingDataWriterFactory {

    public RecordingDataWriter build(String filePath, Metrics metrics) {
        if (filePath.isEmpty()) {
            return RecordingDataWriter.blackhole();
        } else {
            return RecordingDataWriter.async(
                    RecordingDataWriter.statsRecording(
                            metrics,
                            RecordingDataWriter.forFile(Paths.get(filePath).toFile())
                    )
            );
        }
    }
}
