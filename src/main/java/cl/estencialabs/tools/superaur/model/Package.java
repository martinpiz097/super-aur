package cl.estencialabs.tools.superaur.model;

import cl.estencialabs.tools.superaur.exception.DependencyAlreadyExistsException;
import cl.estencialabs.tools.superaur.util.CollectionUtil;
import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class Package {
    private final String name;
    private final Map<String, Package> dependencies;
//    private final Map<String, Package> children;

    public Package(String name) {
        this.name = name;
        this.dependencies = new HashMap<>();
//        this.children = new HashMap<>();
    }

    private List<Dependency> getCleanedDepList(List<Dependency> listDependencies) {
        final Map<String, List<Dependency>> mapGroupsByName = listDependencies.parallelStream().collect(
                Collectors.groupingBy(Dependency::name));

        return mapGroupsByName.values().parallelStream()
                .map(equalsDepsList ->
                        equalsDepsList.stream().max(
                                        Comparator.comparingInt(Dependency::level))
                                .orElse(null))
                .collect(Collectors.toCollection(
                        CollectionUtil::newFastList));
    }

    private List<Dependency> getRawDependencyList(int startLevel) {
        if (isRoot()) {
            return List.of();
        }

        final List<Dependency> listDependencies = getDependencyNames(startLevel);
        dependencies.values().stream()
                .map(depPkg ->
                        depPkg.getRawDependencyList(startLevel + 1))
                .forEach(listDependencies::addAll);

        return listDependencies;
    }

    public void addDependencyAndLink(Package pkg) {
        String pkgName = pkg.getName();
        Package existent = dependencies.isEmpty() ? null : dependencies.get(pkgName);
        if (existent == null) {
            dependencies.put(pkgName, pkg);
//            pkg.addChild(this);
        }
    }

//    public void addChild(Package pkg) {
//        String pkgName = pkg.getName();
//        Package existent = children.isEmpty() ? null : children.get(pkgName);
//        if (existent == null) {
//            children.put(pkgName, pkg);
//        }
//    }

//    public synchronized void upgradePackageLevel(String name) {
//        if (hasChild(name)) {
//            return;
//        }
//
//        children.entrySet().parallelStream()
//                .filter(entry -> {
//
//                })
//    }

    public boolean isRoot() {
        return dependencies.isEmpty();
    }

//    public boolean hasChild(String pkgName) {
//        return !children.isEmpty() && children.keySet()
//                .parallelStream()
//                .anyMatch(pkgNameKey -> pkgNameKey.equals(pkgName));
//    }
//
//    public Package getChild(String pkgName) {
//        return !children.isEmpty() ? children.entrySet()
//                .parallelStream()
//                .filter(entry -> entry.getKey().equals(pkgName))
//                .map(Map.Entry::getValue)
//                .findFirst()
//                .orElse(null) : null;
//    }

    public List<Dependency> getDependencyNames(int startLevel) {
        return dependencies.keySet()
                .stream()
                .sorted()
                .map(depName -> new Dependency(startLevel, depName))
                .collect(Collectors.toCollection(CollectionUtil::newFastList));
    }

    public List<Dependency> getDependencyNames() {
        return getDependencyNames(0);
    }

    public List<Dependency> getDepTreeNamesRecursive(int startLevel, boolean cleaned) {
        List<Dependency> listDependencies = getRawDependencyList(startLevel);
        if (!listDependencies.isEmpty()) {
            if (cleaned) {
                listDependencies = getCleanedDepList(listDependencies);
            }
            listDependencies.sort(Comparator.comparingInt(Dependency::level)
                    .thenComparing(Dependency::name));
        }

        return listDependencies;
    }

    public List<Dependency> getDepTreeNamesRecursive(boolean cleaned) {
        return getDepTreeNamesRecursive(0, cleaned);
    }

    @Override
    public Package clone() throws CloneNotSupportedException {
        Package clone = new Package(name);

        if (!isRoot()) {
            dependencies.forEach((name, pkg) -> {
                try {
                    clone.addDependencyAndLink(pkg.clone());
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        return clone;
    }
}
