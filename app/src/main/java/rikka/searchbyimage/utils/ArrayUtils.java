package rikka.searchbyimage.utils;

import java.lang.reflect.Array;

/**
 * Created by Rikka on 2016/8/28.
 */
public class ArrayUtils {

    public static <T> T[] add(T[] source, Class<T> cls, T obj) {
        return add(source, cls, obj, source.length);
    }

    public static <T> T[] add(T[] source, Class<T> cls, T obj, int index) {
        T[] result = (T[]) Array.newInstance(cls, source.length + 1);
        System.arraycopy(source, 0, result, 0, index);
        source[index] = obj;
        System.arraycopy(source, index + 1, result, index + 1, source.length - index);
        return result;
    }

    public static <T> T[] remove(T[] source, Class<T> cls, int index) {
        T[] result = (T[]) Array.newInstance(cls, source.length - 1);
        System.arraycopy(source, 0, result, 0, index);
        System.arraycopy(source, index + 1, result, index, source.length - index - 1);
        return result;
    }
}
