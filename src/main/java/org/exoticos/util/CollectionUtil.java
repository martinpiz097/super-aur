package org.exoticos.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtil {
    public static <E> List<E> newList(int... capacity) {
        if (capacity != null && capacity.length > 0) {
            return new ArrayList<>(capacity[0]);
        }
        else {
            return new ArrayList<>(10);
        }
    }

    public static <E> List<E> newList(E... elements) {
        List<E> list;
        final int elementsCount = elements != null ? elements.length : 0;
        if (elementsCount > 0) {
            list = newList(elementsCount);
            for (int i = 0; i < elementsCount; i++) {
                list.add(elements[i]);
            }
        }
        else {
            list = newList(10);
        }

        return list;
    }
}
