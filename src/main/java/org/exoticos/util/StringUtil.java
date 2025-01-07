package org.exoticos.util;

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
}
