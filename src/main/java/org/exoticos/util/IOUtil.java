package org.exoticos.util;

import org.exoticos.exception.PackageNotFoundException;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IOUtil {
    private final StringUtil stringUtil;

    public IOUtil() {
        this.stringUtil = new StringUtil();
    }

    public String getFileContent(String path) throws IOException {
        return getFileContent(new File(path));
    }

    public String getFileContent(File file) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        final StringBuilder sbData = new StringBuilder();

        final char[] buffer = new char[(int) Math.pow(1024, 2)];

        int readed;
        while ((readed = bufferedReader.read(buffer)) != -1) {
            sbData.append(buffer, 0, readed);
        }

        bufferedReader.close();
        return sbData.toString();
    }

    public Process executeProcess(String... cmdArray) throws IOException {
        return Runtime.getRuntime().exec(cmdArray);
    }

    public BufferedReader getProcessOutput(String... cmdArray) throws IOException, InterruptedException, PackageNotFoundException {
        final Process process = executeProcess(cmdArray);
        final int exitCode = process.waitFor();

//        System.out.println(Arrays.toString(cmdArray) + " process exit code:  " + exitCode);
        if (exitCode == 0) {
            return process.inputReader();
        }
        else {
            throw new PackageNotFoundException(cmdArray[2]);
        }
    }

    public String getProcessOutputContent(String... cmdArray) throws IOException, InterruptedException, PackageNotFoundException {
        final BufferedReader bufferedReader = getProcessOutput(cmdArray);
        final String content = bufferedReader.lines()
                .collect(Collectors.joining("\n"));

        bufferedReader.close();
        return content;
    }

    public String getProcessFirstLine(String... cmdArray) throws IOException, InterruptedException, PackageNotFoundException {
        final BufferedReader bufferedReader = getProcessOutput(cmdArray);
        String firstLine = bufferedReader.readLine();

        bufferedReader.close();
        return firstLine;
    }

    public File runPkgbuildDownloadCmdSequence(String baseHelper, String packageName) throws IOException, InterruptedException, PackageNotFoundException {
        // los paquetes que no tengan pkgbuild deben omitirse, son alias de otros, como es el caso de pipewire-zeroconf

//        System.out.println("Downloading " + packageName + " PKGBUILD...");
        final String downloadPkgbuildOut = getProcessFirstLine(baseHelper, "-G", packageName, "--color=never");
        final String downloadFolderName = stringUtil.getCleanedPkgName(downloadPkgbuildOut);

        Process rmGitFolderProcess = executeProcess("rm", "-rf", downloadFolderName + "/.git*");
        rmGitFolderProcess.waitFor();

        final long currentTime = System.nanoTime();
        final String destFolder = "/tmp/" + currentTime;

//        System.out.println("Downloaded " + downloadFolderName + " PKGBUILD, moving to " + destFolder + "...");

        Process moveToTmpProc = executeProcess("mv", downloadFolderName, destFolder);
        moveToTmpProc.waitFor();

        return new File(destFolder);
    }

    public boolean hasPkgbuildFile(File pkgFolder) {
        return new File(pkgFolder, "PKGBUILD").exists();
    }

}
