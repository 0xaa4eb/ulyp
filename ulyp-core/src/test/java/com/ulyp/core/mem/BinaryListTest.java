package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.transport.BinaryDataDecoder;
import com.ulyp.transport.BinaryDataEncoder;
import org.agrona.MutableDirectBuffer;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class BinaryListTest {

    private final BinaryList binaryList = new BinaryList();
    private final MutableDirectBuffer underlyingBuffer = binaryList.getBuffer();
    private final byte[] buf = new byte[1024];

    @Test
    public void testListAddAndIterate() {
        assertEquals(0, binaryList.size());

        binaryList.add(
                encoder -> {
                    encoder.id(6555L);
                    encoder.putValue("ABC".getBytes(StandardCharsets.UTF_8), 0, "ABC".getBytes(StandardCharsets.UTF_8).length);
                }
        );

        assertEquals(1, binaryList.size());

        binaryList.add(
                encoder -> {
                    encoder.id(99441L);
                    encoder.putValue("CDEF".getBytes(StandardCharsets.UTF_8), 0, "CDEF".getBytes(StandardCharsets.UTF_8).length);
                }
        );

        assertEquals(2, binaryList.size());
    }

    @Test
    public void testAddressAccess() {
        binaryList.add(
                encoder -> {
                    encoder.id(6555L);
                    encoder.putValue("ABC".getBytes(StandardCharsets.UTF_8), 0, "ABC".getBytes(StandardCharsets.UTF_8).length);
                }
        );

        binaryList.add(
                encoder -> {
                    encoder.id(99441L);
                    encoder.putValue("CDEF".getBytes(StandardCharsets.UTF_8), 0, "CDEF".getBytes(StandardCharsets.UTF_8).length);
                }
        );

        AddressableItemIterator<BinaryDataDecoder> iterator = binaryList.iterator();
        assertTrue(iterator.hasNext());
        iterator.next();

/*
        BinaryDataDecoder value = iterator.next();

        int valLength = value.getValue(buf, 0, buf.length);
        assertEquals("ABC", new String(buf, 0, valLength, StandardCharsets.UTF_8));
*/

        BinaryDataDecoder decoder = new BinaryDataDecoder();
        decoder.wrap(underlyingBuffer, (int) iterator.address(), BinaryDataEncoder.BLOCK_LENGTH, 0);
        assertEquals(3, decoder.valueLength());
        int valLength = decoder.getValue(buf, 0, buf.length);
        assertEquals("ABC", new String(buf, 0, valLength, StandardCharsets.UTF_8));

        assertTrue(iterator.hasNext());
        iterator.next();

        decoder = new BinaryDataDecoder();
        decoder.wrap(underlyingBuffer, (int) iterator.address(), BinaryDataEncoder.BLOCK_LENGTH, 0);
        assertEquals(4, decoder.valueLength());
        valLength = decoder.getValue(buf, 0, buf.length);
        assertEquals("CDEF", new String(buf, 0, valLength, StandardCharsets.UTF_8));
    }
}