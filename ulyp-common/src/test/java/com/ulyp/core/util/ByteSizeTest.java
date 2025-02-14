package com.ulyp.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ByteSizeTest {

    @Test
    void shouldNotAllowNegativeBytes() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> new ByteSize(-1),
            "ByteSize should not accept negative values"
        );
        assertEquals("Byte size can't be negatie", exception.getMessage());
    }

    @Test
    void shouldAllowZeroBytes() {
        ByteSize size = new ByteSize(0);
        assertEquals("0 Bytes", size.toString());
    }

    @ParameterizedTest(name = "{0} bytes should be formatted as {1}")
    @MethodSource("provideByteSizeTestCases")
    void shouldFormatByteSizesCorrectly(long bytes, String expected) {
        assertEquals(expected, ByteSize.toHumanReadable(bytes),
            "Incorrect human readable format for " + bytes + " bytes");
    }

    private static Stream<Arguments> provideByteSizeTestCases() {
        return Stream.of(
            Arguments.of(0L, "0 Bytes"),
            Arguments.of(1L, "1 Bytes"),
            Arguments.of(500L, "500 Bytes"),
            Arguments.of(1024L, "1 KB"),
            Arguments.of(1500L, "1.46 KB"),
            Arguments.of(1024L * 1024L, "1 MB"),
            Arguments.of(1024L * 1024L * 1024L, "1 GB"),
            Arguments.of(1024L * 1024L * 1024L * 1024L, "1 TB"),
            Arguments.of(1024L * 1024L * 1024L * 1024L * 1024L, "1 PB"),
            Arguments.of(1024L * 1024L * 1024L * 1024L * 1024L * 1024L, "1 EB")
        );
    }

    @Test
    void shouldGetByteSize() {
        long bytes = 1234L;
        ByteSize size = new ByteSize(bytes);
        assertEquals(bytes, size.getByteSize(),
            "getByteSize should return the original byte count");
    }

    @Test
    void shouldAddBytes() {
        ByteSize initial = new ByteSize(1024); // 1KB
        ByteSize result = initial.addBytes(1024); // Add 1KB

        assertEquals(2048L, result.getByteSize(),
            "addBytes should correctly sum the bytes");
        assertEquals("2 KB", result.toString(),
            "toString should reflect the added bytes");
    }

    @Test
    void shouldHandleMaxLongValue() {
        ByteSize size = new ByteSize(Long.MAX_VALUE);
        assertDoesNotThrow(() -> size.toString(),
            "Should handle maximum long value without throwing");
        assertTrue(size.toString().contains("EB"),
            "Maximum long value should be represented in EB");
    }

    @Test
    void toStringShouldMatchToHumanReadable() {
        ByteSize size = new ByteSize(1234567L);
        assertEquals(ByteSize.toHumanReadable(1234567L), size.toString(),
            "toString should match toHumanReadable output");
    }
} 