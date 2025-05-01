package cl.estencialabs.tools.superaur.pkg;

import cl.estencialabs.tools.superaur.cmd.CommandInterpreter;
import cl.estencialabs.tools.superaur.cmd.CommandResult;
import cl.estencialabs.tools.superaur.config.PropertiesManager;
import cl.estencialabs.tools.superaur.exception.SuperAurException;
import cl.estencialabs.tools.superaur.model.Package;
import cl.estencialabs.tools.superaur.util.CollectionUtil;
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

public class PackageManager {
    private final PropertiesManager propertiesManager;
    private final AurHelper aurHelper;

    private static final String DEPS_LINE_FILTER = "depend";
    private static final String CONFLICT_LINE_FILTER = "conflict";
    private static final String DEPS_LINE_SPLIT_VALUE = ":";
    private static final byte DEPS_LINE_SPLIT_INDEX = 1;
    private static final byte PKG_SPLIT_INDEX = 0;
    private static final String DEPS_LINE_SPLIT_SPACE_VALUE = " ";
    private static final String DEPS_LINE_SPLIT_VALUE_NOPKG = "ninguno";
    private static final String DEPS_LINE_SPLIT_VALUE_NOPKG_ALT = "none";

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
                    .filter(i -> listLines.get(i).trim().toLowerCase()
                            .startsWith(DEPS_LINE_FILTER))
                    .findFirst().orElse(-1);

        });
    }

    private CompletableFuture<Integer> getLastDepsIndexExclusive(List<String> listLines) {
        return CompletableFuture.supplyAsync(() -> {
            final int lastIndex = listLines.size() - 1;

            return IntStream.iterate(lastIndex, i -> i > -1, i -> i - 1)
                    .filter(i -> listLines.get(i).trim().toLowerCase()
                            .startsWith(CONFLICT_LINE_FILTER))
                    .findFirst().orElse(-1);

        });
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
                listFilteredLines.add(listLines.get(i).trim());
            }

            return listFilteredLines;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getDepLinesList(String output) {
        return getDepLinesList(getLinesListFromOutput(output));
    }

    private List<String> removeIsolatedDepsLines(List<String> listDepLines) {
        return listDepLines.parallelStream()
                .filter(line -> line.contains(DEPS_LINE_SPLIT_VALUE))
                .collect(Collectors.toCollection(CollectionUtil::newFastList));
    }

    private List<String> getIsolatedDependencies(List<String> listDepLines) {
        return listDepLines.parallelStream()
                .filter(line -> !line.contains(DEPS_LINE_SPLIT_VALUE))
                .flatMap(line -> Arrays.stream(line.split(DEPS_LINE_SPLIT_SPACE_VALUE))
                        .filter(string -> !string.isBlank())
                        .map(String::trim))
                .collect(Collectors.toCollection(CollectionUtil::newFastList));
    }

    public List<String> executeDepNamesCommand(String pkgName) throws SuperAurException {
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
                .map(line -> line.split(DEPS_LINE_SPLIT_VALUE)[DEPS_LINE_SPLIT_INDEX].trim()
                        .split(DEPS_LINE_SPLIT_SPACE_VALUE))
                .filter(pkgSplit ->
                        !pkgSplit[PKG_SPLIT_INDEX].trim().equalsIgnoreCase(DEPS_LINE_SPLIT_VALUE_NOPKG)
                                && !pkgSplit[PKG_SPLIT_INDEX].equalsIgnoreCase(DEPS_LINE_SPLIT_VALUE_NOPKG_ALT))
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

    public Package analyze(String pkgName) {
        
    }
}
