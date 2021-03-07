package a;

import com.sun.javafx.image.AlphaType;

import java.util.Arrays;
import java.util.List;

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

    public static List<String> returnListOfString() {
        return Arrays.asList("acczxczx", "dsadasdasd", "dsadfxzc");
    }

    public static String[] returnArrayOfString() {
        return new String[] {"asdasdas", "zxfjzhgxcsa", "asd24234"};
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

        System.out.println(returnListOfString());
        System.out.println(returnArrayOfString());
    }
}
