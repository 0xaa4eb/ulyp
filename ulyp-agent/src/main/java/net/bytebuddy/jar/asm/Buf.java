package net.bytebuddy.jar.asm;

public class Buf {

    public static ThreadLocal<byte[]> buf1 = ThreadLocal.withInitial(() -> new byte[4 * 4096]);
    public static ThreadLocal<byte[]> buf2 = ThreadLocal.withInitial(() -> new byte[4 * 4096]);
}
