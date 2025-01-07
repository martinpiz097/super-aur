package org.exoticos.util;

import org.exoticos.exception.ContentNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringUtil {

    public String   getCleanedPkgName(String downloadCmdOutput) {
        // si el paquete contiene ABS, indica que es descargado desde pacman
        // :: [1m(1/1) PKGBUILD Descargado : hyprland-git[0m

        return downloadCmdOutput
                .substring(0, downloadCmdOutput.length() - 4)
                .replace("::", "")
                .split(":")[1].trim();
    }

    public String getCleanedDepName(String line) {
        String depData = line.trim()
                .replace("(", "")
                .replace(")", "")
                .replace("\"", "")
                .replace("'", "")
                .trim();

        if (depData.contains("<=")) {
            depData = depData.split("<=")[0].trim();
        }
        else if (depData.contains(">=")) {
            depData = depData.split(">=")[0].trim();
        }
        else if (depData.contains("<")) {
            depData = depData.split("<")[0].trim();
        }
        else if (depData.contains(">")) {
            depData = depData.split(">")[0].trim();
        }

        return depData;
    }

    public String subStringOf(String str, String startLineFilter) throws IOException, ContentNotFoundException {
        final BufferedReader bufferedReader = new BufferedReader(new StringReader(str), str.length());

        String line;
        final StringBuilder sbSubstring = new StringBuilder();

        boolean foundLine = false;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith(startLineFilter)) {
                foundLine = true;
                line = line.replace(startLineFilter, "");
            }
            if (foundLine) {
                sbSubstring.append(line).append('\n');
            }
        }

        bufferedReader.close();

        if (!foundLine) {
            throw new ContentNotFoundException(startLineFilter);
        }

        final int sbLength = sbSubstring.length();
        if (!sbSubstring.isEmpty() && sbSubstring.charAt(sbLength - 1) == '\n') {
            sbSubstring.deleteCharAt(sbLength - 1);
        }

        return sbSubstring.toString();
    }

    public String subStringOf(String str, String startLineFilter, String start, String end) throws IOException, ContentNotFoundException {
        final String initialString = subStringOf(str, startLineFilter);

        final String[] subtextSplit = initialString.split(start);

        // evaluar uso de str.indexOf() mas adelante para optimizar funcion
        if (subtextSplit.length <= 1) {
            return null;
        }

        final String contentFind = subtextSplit[1];
        final String[] contentFindSplit = contentFind.split(end);

        if (contentFindSplit.length < 1) {
            return null;
        }

        return contentFindSplit[0];
    }

    public String normalizeDependsString(String dependsString) {
        return dependsString
                .replace("\"", "")
                .replace("'", "")
                .replace("<=", "")
                .replace(">=", "")
                .replace("<", "")
                .replace(">", "")
                .replace("\n", " ")
                .replace("\t", " ")
                .trim();
    }

    public Set<String> getDependencyNamesFromText(String depsText) {
        depsText = normalizeDependsString(depsText);

        if (depsText.isEmpty()) {
            return new HashSet<>();
        }

        // puede ser 0 si hay un solo elemento
        final String[] namesSplit = depsText.split(" ");
        final int splitLength = namesSplit.length;
        final Set<String> depNamesSet = new HashSet<>(splitLength + 2);

        if (splitLength > 0) {
            String depName;
            for (int i = 0; i < splitLength; i++) {
                depName = namesSplit[i].trim();
                if (!depName.isEmpty()) {
                    depNamesSet.add(depName);
                }
            }
        }
        else {
            depNamesSet.add(depsText);
        }

        return depNamesSet;
    }
}
