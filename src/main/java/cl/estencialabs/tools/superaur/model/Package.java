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

    public Package(String name) {
        this.name = name;
        this.dependencies = new HashMap<>();
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

    public void addDependency(Package pkg) {
        String pkgName = pkg.getName();
        Package existent = dependencies.get(pkgName);
        if (existent != null) {
            throw new DependencyAlreadyExistsException(pkgName);
        }

        dependencies.put(pkgName, pkg);
    }

    public boolean isRoot() {
        return dependencies.isEmpty();
    }

    public List<Dependency> getDependencyNames(int startLevel) {
        return dependencies.keySet()
                .stream()
                .sorted()
                .map(depName -> new Dependency(startLevel, depName))
                .toList();
    }

    public List<Dependency> getDependencyNames() {
        return getDependencyNames(0);
    }

    public List<Dependency> getDependencyNamesRecursive(int startLevel) {
        final List<Dependency> listDependencies = getRawDependencyList(startLevel);
        return getCleanedDepList(listDependencies);
    }

    public List<Dependency> getDependencyNamesRecursive() {
        return getDependencyNamesRecursive(0);
    }

    public List<Dependency> getDepTreeNamesRecursive(int startLevel) {
        final List<Dependency> listDependencies = getRawDependencyList(startLevel);
        getRawDependencyList(startLevel)
                .sort(Comparator.comparingInt(Dependency::level)
                        .thenComparing(Dependency::name));

        return listDependencies;
    }

    public List<Dependency> getDepTreeNamesRecursive() {
        return getDepTreeNamesRecursive(0);
    }
}
