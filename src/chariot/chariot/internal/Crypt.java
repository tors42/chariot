package chariot.internal;

import java.util.Random;

public class Crypt {

    public record Result(char[] data, int key) {}

    public static Result encrypt(char[] data) {
        var key = new Random().nextInt(5000) + 1;
        var enc = caesar(data, key);
        return new Result(enc, key);
    }

    public static char[] decrypt(char[] data, int key) {
        return caesar(data, -key);
    }

    private static char[] caesar(char[] data, int distance) {
        char[] copy = new char[data.length];
        for(int i = 0; i < data.length; i++) {
            copy[i] = (char) (data[i] + distance);
        }
        return copy;
    }

}
