package com.ulyp.core.util;

import org.agrona.collections.LongArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BitUtilTest {

    @ParameterizedTest(name = "log2({0}) should return {1}")
    @MethodSource("providePowerOfTwoValues")
    void shouldCalculateLog2ForPowerOfTwo(int input, int expected) {
        assertEquals(expected, BitUtil.log2(input),
            "log2 calculation incorrect for power of 2 value: " + input);
    }

    private static Stream<Arguments> providePowerOfTwoValues() {
        return Stream.of(
            Arguments.of(1, 0),
            Arguments.of(2, 1),
            Arguments.of(4, 2),
            Arguments.of(8, 3),
            Arguments.of(16, 4),
            Arguments.of(32, 5),
            Arguments.of(64, 6),
            Arguments.of(128, 7),
            Arguments.of(256, 8)
        );
    }

    @ParameterizedTest(name = "log2({0}) should throw IllegalArgumentException")
    @MethodSource("provideNonPowerOfTwoValues")
    void shouldThrowForNonPowerOfTwo(int input) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> BitUtil.log2(input),
            "Should throw IllegalArgumentException for non-power of 2: " + input);
        assertEquals("is not power of 2: " + input, exception.getMessage());
    }

    private static Stream<Arguments> provideNonPowerOfTwoValues() {
        return Stream.of(
            Arguments.of(0),
            Arguments.of(3),
            Arguments.of(5),
            Arguments.of(7),
            Arguments.of(9),
            Arguments.of(10)
        );
    }

    @Test
    void shouldConvertIntToBytes() {
        int value = 0x12345678;
        byte[] bytes = new byte[4];
        BitUtil.intToBytes(value, bytes, 0);

        assertEquals(0x12, bytes[0] & 0xFF, "First byte incorrect");
        assertEquals(0x34, bytes[1] & 0xFF, "Second byte incorrect");
        assertEquals(0x56, bytes[2] & 0xFF, "Third byte incorrect");
        assertEquals(0x78, bytes[3] & 0xFF, "Fourth byte incorrect");
    }

    @Test
    void shouldConvertBytesToLong() {
        byte[] bytes = new byte[]{
            0x12, 0x34, 0x56, 0x78, (byte)0x9A, (byte)0xBC, (byte)0xDE, (byte)0xF0
        };
        long value = BitUtil.bytesToLong(bytes, 0);
        assertEquals(0x123456789ABCDEF0L, value, "Bytes to long conversion incorrect");
    }

    @Test
    void shouldConvertBytesToInt() {
        byte[] bytes = new byte[]{0x12, 0x34, 0x56, 0x78};
        int value = BitUtil.bytesToInt(bytes, 0);
        assertEquals(0x12345678, value, "Bytes to int conversion incorrect");
    }

    @Test
    void shouldConvertBytesToLongs() {
        byte[] bytes = new byte[]{
            0x12, 0x34, 0x56, 0x78, (byte)0x9A, (byte)0xBC, (byte)0xDE, (byte)0xF0,
            (byte)0xFE, (byte)0xDC, (byte)0xBA, (byte)0x98, 0x76, 0x54, 0x32, 0x10
        };

        LongArrayList longs = BitUtil.bytesToLongs(bytes);
        assertEquals(2, longs.size(), "Should convert to 2 longs");
        assertEquals(0x123456789ABCDEF0L, longs.getLong(0), "First long conversion incorrect");
        assertEquals(0xFEDCBA9876543210L, longs.getLong(1), "Second long conversion incorrect");
    }

    @Test
    void shouldCombineTwoIntsIntoLong() {
        int x = 0x12345678;
        int y = 0x9ABCDEF0;
        long result = BitUtil.longFromInts(x, y);
        assertEquals(0x123456789ABCDEF0L, result, "Combining ints into long incorrect");
    }

    @Test
    void shouldHandleOffsetCorrectly() {
        byte[] bytes = new byte[12];  // larger array to test offset
        long value = 0x123456789ABCDEF0L;
        BitUtil.longToBytes(value, bytes, 4);  // write at offset 4

        // Verify bytes before offset are untouched
        assertEquals(0, bytes[0], "Byte before offset should be untouched");
        assertEquals(0, bytes[3], "Byte before offset should be untouched");

        // Verify bytes at offset
        assertEquals(0x12, bytes[4] & 0xFF, "First byte at offset incorrect");
        assertEquals(0xF0, bytes[11] & 0xFF, "Last byte at offset incorrect");
    }
} 