package org.exoticos;

import lombok.Data;
import org.exoticos.util.CollectionUtil;

import java.util.*;
import java.util.stream.IntStream;

@Data
public class PackageManager {
    private final Map<Integer, List<SwPackage>> mapPackageLevels;

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
    public List<SwPackage> getOrCreatePkgList(Integer level) {
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
                final List<SwPackage> hashSet = CollectionUtil.newList(20);
                mapPackageLevels.put(level, hashSet);
                return hashSet;
            }
        }
        else {
            return mapPackageLevels.get(level);
        }
    }

    public void insertInLevel(SwPackage swPackage, int level) {
        final List<SwPackage> packagesList = getOrCreatePkgList(level);

        if (packagesList.parallelStream().noneMatch(pkg -> pkg.getName().equals(swPackage.getName()))) {
            packagesList.add(swPackage);
        }
    }

    public void deleteDuplicated() {
        // es valido hacer la limpieza con mas de dos niveles, sino no tiene sentido
        if (mapPackageLevels.size() > 2) {
            mapPackageLevels.entrySet()
                    .stream()
                    .sorted((o1, o2)
                            -> o2.getKey().compareTo(o1.getKey()))
                    .forEach(entry -> {
                        final Integer currentLevel = entry.getKey();
                        final List<SwPackage> listPackages = entry.getValue();
                        listPackages.parallelStream()
                                .forEach(currentPkg -> {
                                    List<SwPackage> listPkgs;
                                    for (int i = currentLevel - 1; i > 0; i--) {
                                        listPkgs = mapPackageLevels.get(i);
                                        listPkgs.removeIf(pkg ->
                                                pkg.getName().equals(currentPkg.getName()));
                                    }
                                });
                    });
        }
    }

    public void movePackageToSupraLevel(SwPackage swPackage) {
        final Map.Entry<Integer, List<SwPackage>> packageEntry = findPackage(swPackage.getName());

        if (packageEntry != null && packageEntry.getKey() < getTopPackagesLevel()) {
            List<SwPackage> packagesSetOri = getOrCreatePkgList(packageEntry.getKey());
            List<SwPackage> packagesSetDest = getOrCreatePkgList(packageEntry.getKey() + 1);

            packagesSetOri.remove(swPackage);
            packagesSetDest.add(swPackage);
        }
    }

    public Map.Entry<Integer, List<SwPackage>> findPackage(String name) {
        return mapPackageLevels.entrySet().parallelStream()
                .filter(entry -> entry.getValue().parallelStream()
                        .anyMatch(pkg -> pkg.getName().equals(name)))
                .findFirst().orElse(null);
    }

    public List<SwPackage> getRootParentPackages(SwPackage swPackage) {
        swPackage.getDependenciesList()
                .parallelStream()
                .map(dependency -> {
                    List<SwPackage> listDeps;
                    if (dependency.isRoot()) {
                        listDeps = CollectionUtil.newList(dependency);
                    }
                    else {
                        listDeps = getRootParentPackages(dependency);
                    }
                    return listDeps;
                });
        return null;
    }


}
