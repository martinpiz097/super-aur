package org.exoticos;

import lombok.Data;
import org.exoticos.exception.LevelCreationException;
import org.exoticos.exception.LevelInfoException;
import org.exoticos.util.CollectionUtil;

import java.util.*;

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
        try {
            return getPkgList(level);
        } catch (LevelInfoException e) {
            return createPkgList(level);
        }
    }

    public List<SwPackage> createPkgList(Integer level) {
        return createPkgList(level, CollectionUtil.newList(100));
    }

    public List<SwPackage> createPkgList(Integer level, List<SwPackage> listPkgs) {
        Objects.requireNonNull(level);

        level = normalizeLevel(level);
        final int topPackagesLevel = getTopPackagesLevel();

        if (level <= topPackagesLevel) {
            throw new LevelCreationException(level, true);
        }
        if (isGiantLevel(level, topPackagesLevel)) {
            throw new LevelCreationException(level, false);
        }

        final List<SwPackage> listPkg = CollectionUtil.newList();
        mapPackageLevels.put(level, listPkgs);
        return listPkg;
    }

    public List<SwPackage> getPkgList(Integer level) {
        Objects.requireNonNull(level);
        level = normalizeLevel(level);
        final int topPackagesLevel = getTopPackagesLevel();

        if (level > topPackagesLevel) {
            throw new LevelInfoException(level);
        }

        return mapPackageLevels.get(level);
    }

    public void insertInLevel(SwPackage swPackage, int level) {
        final List<SwPackage> packagesList = getOrCreatePkgList(level);

        if (packagesList.parallelStream().noneMatch(pkg -> pkg.getName().equals(swPackage.getName()))) {
            packagesList.add(swPackage);
        }
    }

    public void deleteDuplicatedDependencies() {
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

}
