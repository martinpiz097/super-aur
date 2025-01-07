package org.exoticos;

import lombok.Data;
import lombok.extern.java.Log;
import org.exoticos.exception.ContentNotFoundException;
import org.exoticos.exception.PackageNotFoundException;
import org.exoticos.util.IOUtil;
import org.exoticos.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Log
@Data
public class SuperAurHelper {

    private final PackageManager packageManager;
    private final String baseHelper;

    private static final String DEFAULT_BASE_HELPER = "yay";

    public SuperAurHelper() {
        this.packageManager = new PackageManager();
        this.baseHelper = DEFAULT_BASE_HELPER;
    }

    public File downloadPkgbuild(String packageName) throws InterruptedException, IOException, PackageNotFoundException {
        final IOUtil ioUtil = new IOUtil();

        final File pkgTempFolder = ioUtil.runPkgbuildDownloadCmdSequence(baseHelper, packageName);
        final File pkgbuild = new File(pkgTempFolder, "PKGBUILD");

        if (!pkgbuild.exists()) {
            throw new FileNotFoundException("El PKGBUILD del paquete " + packageName
                    + " no existe en la ruta " + pkgbuild.getPath());
        }

//        System.out.println(packageName + " PKGBUILD, moved to " + pkgTempFolder.getPath() + " successfully");
        return pkgbuild;
    }

    public String getPkgbuildContent(String packageName) throws InterruptedException, IOException, PackageNotFoundException {
        return new IOUtil().getFileContent(downloadPkgbuild(packageName));
    }

    // READ MAKEDEPS TAMBIEN, ES IMPORTANTE

    /*
    * 3 opciones
    * 1 (nombres)
    * 2 {
    * nombres en cada linea, pueden ir con o sin comillas, tanto simples como dobles
    * */

    public Set<String> getDependencyNames(String packageName) throws IOException, InterruptedException, PackageNotFoundException {
        final String pkgbuildContent = getPkgbuildContent(packageName);
        final StringUtil stringUtil = new StringUtil();

        final String dependenciesSubstring;
        try {
            dependenciesSubstring = stringUtil.subStringOf(
                    pkgbuildContent, "depends", "\\(", "\\)");
            if (dependenciesSubstring == null) {
                throw new ContentNotFoundException("");
            }
        } catch (ContentNotFoundException e) {
            System.out.println(e.getMessage());
            return new HashSet<>(2);
        }

        return stringUtil.getDependencyNamesFromText(dependenciesSubstring);
    }

    public SwPackage scanDependencies(String packageName, int currentLevel) throws IOException, InterruptedException, PackageNotFoundException {
        System.out.println("["+currentLevel+"] (DEPENDENCIES SCAN) PKG=" + packageName);
        final SwPackage currentSwPackage = new SwPackage(packageName);
        final List<SwPackage> dependenciesSet = currentSwPackage.getDependenciesList();
        final Set<String> dependencyNamesSet = getDependencyNames(packageName);

        // esto perfectamente puede ser un parametro para ahorrarse la busqueda, mientras
        // quedara asi para validar funcionalidad

        packageManager.insertInLevel(currentSwPackage, currentLevel);

        dependencyNamesSet.stream()
                .map(depName -> {
                    try {
                        return scanDependencies(depName, currentLevel + 1);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (PackageNotFoundException e) {
                        System.out.println(e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(dependenciesSet::add);

        return currentSwPackage;
    }

    public SwPackage analizePackage(String packageName) {
        return new SwPackage("test");
    }

    public void analizePackages(List<String> listPackageNames) {

    }

    public SwPackage newSwPackage(String name) throws IOException, InterruptedException {
        final SwPackage swPackage = new SwPackage(name);





        return swPackage;
    }
}
