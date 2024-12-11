package com.ulyp.agent.util;

/**
 * Copied from fastutil library to avoid adding a dependency to the agent.
 * Source: https://github.com/vigna/fastutil
 * License: Apache 2.0
 */
public class HashCommon {

    protected HashCommon() {};

    public final static int murmurHash3( int x ) {
        x ^= x >>> 16;
        x *= 0x85ebca6b;
        x ^= x >>> 13;
        x *= 0xc2b2ae35;
        x ^= x >>> 16;
        return x;
    }

    /** Return the least power of two greater than or equal to the specified value.
     *
     * <p>Note that this function will return 1 when the argument is 0.
     *
     * @param x a long integer smaller than or equal to 2<sup>62</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    public static long nextPowerOfTwo( long x ) {
        if ( x == 0 ) return 1;
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return ( x | x >> 32 ) + 1;
    }


    /** Returns the maximum number of entries that can be filled before rehashing.
     *
     * @param n the size of the backing array.
     * @param f the load factor.
     * @return the maximum number of entries before rehashing.
     */
    public static int maxFill( final int n, final float f ) {
        return (int)Math.ceil( n * f );
    }

    /** Returns the maximum number of entries that can be filled before rehashing.
     *
     * @param n the size of the backing array.
     * @param f the load factor.
     * @return the maximum number of entries before rehashing.
     */
    public static long maxFill( final long n, final float f ) {
        return (long)Math.ceil( n * f );
    }

    /** Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than or equal to <code>Math.ceil( expected / f )</code>.
     *
     * @param expected the expected number of elements in a hash table.
     * @param f the load factor.
     * @return the minimum possible size for a backing array.
     * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
     */
    public static int arraySize( final int expected, final float f ) {
        final long s = nextPowerOfTwo( (long)Math.ceil( expected / f ) );
        if ( s > (1 << 30) ) throw new IllegalArgumentException( "Too large (" + expected + " expected elements with load factor " + f + ")" );
        return (int)s;
    }
}