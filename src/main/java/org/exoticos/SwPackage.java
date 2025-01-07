package org.exoticos;

import lombok.Data;
import org.exoticos.util.CollectionUtil;

import java.util.List;

@Data
public class SwPackage {
    private final String name;
    private final List<SwPackage> dependenciesList;

    public SwPackage(String name) {
        this.name = name;
        this.dependenciesList = CollectionUtil.newList(100);

        if (this.name == null) {
            throw new NullPointerException("SwPackage name can not be null");
        }
    }

    public boolean isRoot() {
        return dependenciesList.isEmpty();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return name.equals(obj);
    }

    @Override
    public String toString() {
        return name;
    }
}
