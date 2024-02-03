package io.dmcs.common.utils;

public class Print {

    private Print() {
    }

    public static <T> T println(T t) {
        System.out.println(t);
        return t;
    }
}
