package org.exoticos;

import lombok.Data;

import java.util.*;

@Data
public class PackageManager {
    private final Map<Integer, Set<SwPackage>> mapPackageLevels;

    private static final byte MIN_LEVEL = 1;

    public PackageManager() {
        this.mapPackageLevels = new TreeMap<>();
    }

    private boolean isGiantLevel(int level) {
        return isGiantLevel(level, getTopPackagesLevel());
    }

    private boolean isGiantLevel(int level, int topPackagesLevel) {
        return level - topPackagesLevel > 1;
    }

    private int normalizeLevel(int level) {
        return level < MIN_LEVEL ? MIN_LEVEL : level;
    }

    public int getTopPackagesLevel() {
        return mapPackageLevels.size();
    }

    // empezamos de level 1
    public Set<SwPackage> getOrCreatePkgSet(Integer level) {
        Objects.requireNonNull(level);

        level = normalizeLevel(level);
        final int topPackagesLevel = getTopPackagesLevel();

        if (level > topPackagesLevel) {
            // si yo quiero crear x + n siendo n > 1 no es correcto
            if (isGiantLevel(level, topPackagesLevel)) {
                throw new RuntimeException("Level to create giant than map top level");
            }

            // si el top level actual es x y yo quiero crear n siendo n igual a 1 es correcto
            else {
                final HashSet<SwPackage> hashSet = new HashSet<>(20);
                mapPackageLevels.put(level, hashSet);
                return hashSet;
            }
        }
        else {
            return mapPackageLevels.get(level);
        }
    }

    public void addPackageToLevel(Integer level, SwPackage swPackage) {
        getOrCreatePkgSet(level).add(swPackage);
    }

    public void movePackageToSupraLevel(SwPackage swPackage) {
        final Map.Entry<Integer, Set<SwPackage>> packageEntry = findPackage(swPackage.getName());

        if (packageEntry != null && packageEntry.getKey() < getTopPackagesLevel()) {
            Set<SwPackage> packagesSetOri = getOrCreatePkgSet(packageEntry.getKey());
            Set<SwPackage> packagesSetDest = getOrCreatePkgSet(packageEntry.getKey() + 1);

            packagesSetOri.remove(swPackage);
            packagesSetDest.add(swPackage);
        }
    }

    public Map.Entry<Integer, Set<SwPackage>> findPackage(String name) {
        return mapPackageLevels.entrySet().parallelStream()
                .filter(entry -> entry.getValue().parallelStream()
                        .anyMatch(pkg -> pkg.getName().equals(name)))
                .findFirst().orElse(null);
    }

    public List<SwPackage> getRootParentPackages(SwPackage swPackage) {
        swPackage.getDependenciesSet()
                .parallelStream()
                .map(dependency -> {
                    List<SwPackage> listDeps;
                    if (dependency.isRoot()) {
                        listDeps = new ArrayList<>(2);
                        listDeps.add(dependency);
                    }
                    else {
                        listDeps = getRootParentPackages(dependency);
                    }
                    return listDeps;
                });
        return null;
    }


}
