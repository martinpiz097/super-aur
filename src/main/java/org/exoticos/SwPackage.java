package org.exoticos;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class SwPackage {
    private final String name;
    private final Set<SwPackage> dependenciesSet;

    public SwPackage(String name) {
        this.name = name;
        this.dependenciesSet = new HashSet<>(100);

        if (this.name == null) {
            throw new NullPointerException("SwPackage name can not be null");
        }
    }

    public boolean isRoot() {
        return dependenciesSet.isEmpty();
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
