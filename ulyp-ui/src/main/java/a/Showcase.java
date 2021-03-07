package a;

import com.sun.javafx.image.AlphaType;

public class Showcase {

    public static void throwRuntimeException() {
        throw new RuntimeException("Some message");
    }

    public static void throwNpe() {
        throw new NullPointerException();
    }

    public static AlphaType returnEnum() {
        return AlphaType.NONPREMULTIPLIED;
    }

    public static void main(String[] args) {

        System.out.println(returnEnum());

        try {
            throwRuntimeException();
        } catch (Throwable e) {
            // NOP
        }

        try {
            throwNpe();
        } catch (Throwable e) {
            // NOP
        }
    }
}
