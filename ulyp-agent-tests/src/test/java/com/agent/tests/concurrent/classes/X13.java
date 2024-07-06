package com.agent.tests.concurrent.classes;

// Automatically generated and only used for instrumentation tests
@SuppressWarnings("unused")
public class X13 {
    public int m1(int a, int b, int c) {
        System.out.println("1");
        return a + b + c;
    }

    public float m2(float a, float b, float c) {
        System.out.println("1");
        return a + b + c;
    }

    public Object m3() {
        System.out.println("1");
        return new Object();
    }

    public float m4(float a, float b, float c, float d) {
        System.out.println("1");
        return a * b * c * d;
    }

    public int m5(short a, short b, short c) {
        System.out.println("1");
        return a + b + c;
    }

    public String m8(Integer x, Long y, Long z) {
        System.out.println("1");
        return String.valueOf(y + z);
    }

    public String m9(Integer x, Long y, Long z, String suffix) {
        System.out.println("1");
        return y + z + suffix;
    }

    public String m10(String prefix, Integer x, Long y, Long z, String suffix) {
        System.out.println("1");
        return prefix + y + z + suffix;
    }

    public String m11(Integer x, Long y, Long z) {
        System.out.println("1");
        return String.valueOf(y + z);
    }

    public String m12(Integer x, Long y, Long z) {
        System.out.println("1");
        return String.valueOf(y + z);
    }

    public void m13() {
        System.out.println("hello");
    }

    public void m14() {
        System.out.println("2");
    }

    public void m15() {
        System.out.println("3");
    }

    public void m6() {
        System.out.println("4");
    }

    public void m7() {
        System.out.println("1");
    }
}
