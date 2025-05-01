package cl.estencialabs.tools.superaur.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CollectionUtil {

    private static final short MIN_CAPACITY = 128;
    private static final byte MIN_EXTRA = 11;

    public static <T> List<T> newFastList(int capacity) {
        return new ArrayList<>(capacity + MIN_EXTRA);
    }

    public static <T> List<T> newFastList() {
        return newFastList(MIN_CAPACITY);
    }

    public static <T> List<T> newFastList(T initialElement, int capacity) {
        final List<T> fastList = newFastList(capacity);
        fastList.add(initialElement);

        return fastList;
    }

    public static <T> List<T> newFastListFromElements(Collection<T> collection) {
        final List<T> fastList = newFastList(collection.size());
        fastList.addAll(collection);

        return fastList;
    }

    public static <T> List<T> newFastListFromElements(Stream<T> stream) {
        return newFastListFromElements(stream.toList());
    }

    public static <T> List<T> newFastListFromElements(T... elements) {
        if (elements == null || elements.length == 0) {
            return newFastList(0);
        }

        final int elementsLength = elements.length;
        final List<T> fastList = newFastList(elementsLength);

        for (int i = 0; i < elementsLength; i++) {
            fastList.add(elements[i]);
        }

        return fastList;
    }

}
