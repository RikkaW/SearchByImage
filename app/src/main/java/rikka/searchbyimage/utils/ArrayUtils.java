package rikka.searchbyimage.utils;

/**
 * Created by Rikka on 2016/8/28.
 */
public class ArrayUtils {

    public static <T> T[] add(T[] source, T obj) {
        return add(source, obj, source.length);
    }

    public static <T> T[] add(T[] source, T obj, int index) {
        Object[] result = new Object[source.length + 1];
        System.arraycopy(source, 0, result, 0, index);
        source[index] = obj;
        System.arraycopy(source, index + 1, result, index + 1, source.length - index);
        return (T[]) result;
    }

    public static <T> T[] remove(T[] source, int index) {
        Object[] result = new Object[source.length - 1];
        System.arraycopy(source, 0, result, 0, index);
        if (source.length != index) {
            System.arraycopy(source, index + 1, result, index, source.length - index - 1);
        }
        return (T[]) result;
    }
}
