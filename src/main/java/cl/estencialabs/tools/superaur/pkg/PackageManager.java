package cl.estencialabs.tools.superaur.pkg;

import cl.estencialabs.tools.superaur.cmd.CommandResult;
import cl.estencialabs.tools.superaur.config.PropertiesManager;
import cl.estencialabs.tools.superaur.exception.SuperAurException;
import cl.estencialabs.tools.superaur.model.Package;
import cl.estencialabs.tools.superaur.util.CollectionUtil;
import lombok.extern.java.Log;
import lombok.val;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Log
public class PackageManager {
    private final PropertiesManager propertiesManager;
    private final AurHelper aurHelper;

    private static final String DEPEND = "depend";
    private static final String OPTIONAL = "optional";
    private static final String OPCIONAL = "opcional";
    private static final String CONFLICT = "conflict";
    private static final String COLON = ":";
    private static final String SPACE = " ";
    private static final String NADA = "nada";
    private static final String NINGUNO = "ninguno";
    private static final String NONE = "none";
    private static final String SUPER_SPACE = "      ";

    private static final byte DEPS_LINE_SPLIT_INDEX = 1;
    private static final byte PKGS_LIST_INDEX = 0;

    public PackageManager(AurHelper aurHelper) {
        this.propertiesManager = new PropertiesManager();
        this.aurHelper = aurHelper;
    }

    private List<String> getLinesListFromOutput(String output) {
        return output.trim().lines()
                .collect(Collectors.toCollection(CollectionUtil::newFastList));
    }

    private CompletableFuture<Integer> getFirstDepsIndex(List<String> listLines) {
        return CompletableFuture.supplyAsync(() -> {
            final int linesCount = listLines.size();

            return IntStream.range(0, linesCount)
                    .filter(i -> isDependenciesLine(listLines.get(i)))
                    .findFirst().orElse(-1);

        });
    }

    private CompletableFuture<Integer> getLastDepsIndexExclusive(List<String> listLines) {
        return CompletableFuture.supplyAsync(() -> {
            final int lastIndex = listLines.size() - 1;

            return IntStream.iterate(lastIndex, i -> i > -1, i -> i - 1)
                    .filter(i -> isConflictLine(listLines.get(i)))
                    .findFirst().orElse(-1);

        });
    }

    private boolean isDependenciesLine(String line) {
        if (!line.contains(COLON)) {
            return false;
        }

        final String[] depsSplit = line.split(COLON);
        return depsSplit[0].toLowerCase().contains(DEPEND);
    }

    private boolean isOptDependenciesLine(String line) {
        if (!line.contains(COLON)) {
            return false;
        }

        final String[] depsSplit = line.split(COLON);
        final String firstElementToLower = depsSplit[0].toLowerCase();

        return firstElementToLower.contains(OPTIONAL) || firstElementToLower.contains(OPCIONAL);
    }

    private boolean isIsolatedDependenciesLine(String line) {
        if (line.contains(COLON)) {
            final String firstElement = line.split(COLON)[0];
            return firstElement.startsWith(SUPER_SPACE)
                    && !firstElement.isBlank();
        }
        return !line.isBlank();
    }

    private boolean isConflictLine(String line) {
        if (!line.contains(COLON)) {
            return false;
        }

        final String[] depsSplit = line.split(COLON);
        return depsSplit[0].toLowerCase().contains(CONFLICT);
    }

    private List<String> getDepLinesList(List<String> listLines) {
        final CompletableFuture<Integer> firstDepsIndex = getFirstDepsIndex(listLines);
        final CompletableFuture<Integer> lastDepsIndex = getLastDepsIndexExclusive(listLines);

        try {
            CompletableFuture.allOf(firstDepsIndex, lastDepsIndex)
                    .get(10, TimeUnit.SECONDS);

            val firstIndex = firstDepsIndex.get();
            val lastIndex = lastDepsIndex.get();

            final List<String> listFilteredLines = CollectionUtil.newFastList();
            for (int i = firstIndex; i < lastIndex; i++) {
                listFilteredLines.add(listLines.get(i));
            }

            final int filteredListSize = listFilteredLines.size();
            final List<String> listLastFiltered = CollectionUtil.newFastList(filteredListSize);

            String line;
            boolean optDepLineFound = false;
            for (int i = 0; i < filteredListSize; i++) {
                line = listFilteredLines.get(i);
                if (!optDepLineFound && isOptDependenciesLine(line)) {
                    optDepLineFound = true;
                } else if (!isIsolatedDependenciesLine(line)) {
                    listLastFiltered.add(line);
                    if (optDepLineFound) {
                        optDepLineFound = false;
                    }
                }
            }

            return listLastFiltered;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getDepLinesList(String output) {
        return getDepLinesList(getLinesListFromOutput(output));
    }

    private List<String> removeIsolatedDepsLines(List<String> listDepLines) {
        return listDepLines.parallelStream()
                .filter(this::isDependenciesLine)
                .collect(Collectors.toCollection(CollectionUtil::newFastList));
    }

    private List<String> getIsolatedDependencies(List<String> listDepLines) {
        return listDepLines.parallelStream()
                .filter(this::isIsolatedDependenciesLine)
                .flatMap(line -> Arrays.stream(line.split(SPACE))
                        .filter(string -> !string.isBlank())
                        .map(String::trim))
                .collect(Collectors.toCollection(CollectionUtil::newFastList));
    }

    public List<String> getPackageDependencies(String pkgName) throws SuperAurException {
        if (pkgName == null || pkgName.isBlank()) {
            throw new SuperAurException("pkgName value is null or blank");
        }

        final CommandResult result = aurHelper.execPackageInfoCommand(pkgName);

        if (!result.isSucessful()) {
            return List.of();
        }

        final String output = result.output();
        if (output == null || output.isBlank()) {
            return List.of();
        }

        final List<String> listDepLines = getDepLinesList(output);
        final CompletableFuture<List<String>> isolatedDepsTask =
                CompletableFuture.supplyAsync(() ->
                        getIsolatedDependencies(listDepLines));

        final List<String> listNoIsolated = removeIsolatedDepsLines(listDepLines);
        final List<String> listBaseDeps = listNoIsolated.parallelStream()
                .map(line -> line.split(COLON)[DEPS_LINE_SPLIT_INDEX].trim()
                        .split(SPACE))
                .filter(pkgSplit -> {
                    final String trimmedSplit = pkgSplit[PKGS_LIST_INDEX].trim();
                    return !trimmedSplit.equalsIgnoreCase(NINGUNO)
                            && !trimmedSplit.equalsIgnoreCase(NONE)
                            && !trimmedSplit.equalsIgnoreCase(NADA);
                })
                .flatMap((Function<String[], Stream<String>>)
                        strings -> Arrays.stream(strings)
                                .filter(string -> !string.isBlank())
                                .map(String::trim))
                .distinct()
                .collect(Collectors.toCollection(CollectionUtil::newFastList));

        try {
            listBaseDeps.addAll(isolatedDepsTask.get(10, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listBaseDeps;
    }

    public Package analyze(String pkgName) throws SuperAurException {
        if (pkgName == null || pkgName.isBlank()) {
            return null;
        }

        log.info("Scanning " + pkgName + "...");
        final Package pkg = new Package(pkgName);
        final List<String> listDeps = getPackageDependencies(pkgName);

        listDeps.forEach(depName -> {
            try {
                pkg.addDependency(analyze(depName));
            } catch (SuperAurException e) {
                throw new RuntimeException(e);
            }
        });

        return pkg;
    }
}
