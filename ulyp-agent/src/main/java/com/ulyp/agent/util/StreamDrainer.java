package com.ulyp.agent.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StreamDrainer {

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    private static final int END_OF_STREAM = -1;
    private static final int FROM_BEGINNING = 0;

    private final int bufferSize;

    public StreamDrainer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    public StreamDrainer(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public byte[] drain(InputStream inputStream) throws IOException {
        List<byte[]> previousBytes = new ArrayList<byte[]>();
        byte[] currentArray = new byte[bufferSize];
        int currentIndex = 0;
        int currentRead;
        do {
            currentRead = inputStream.read(currentArray, currentIndex, bufferSize - currentIndex);
            currentIndex += currentRead > 0 ? currentRead : 0;
            if (currentIndex == bufferSize) {
                previousBytes.add(currentArray);
                currentArray = new byte[bufferSize];
                currentIndex = 0;
            }
        } while (currentRead != END_OF_STREAM);
        byte[] result = new byte[previousBytes.size() * bufferSize + currentIndex];
        int arrayIndex = 0;
        for (byte[] previousByte : previousBytes) {
            System.arraycopy(previousByte, FROM_BEGINNING, result, arrayIndex++ * bufferSize, bufferSize);
        }
        System.arraycopy(currentArray, FROM_BEGINNING, result, arrayIndex * bufferSize, currentIndex);
        return result;
    }

    public int getAvailableBytesCount(InputStream inputStream) throws IOException {
        int totalBytes = 0;
        byte[] buf = new byte[bufferSize];
        int read;
        while ((read = inputStream.read(buf)) >= 0) {
            totalBytes += read;
        }
        return totalBytes;
    }
}
