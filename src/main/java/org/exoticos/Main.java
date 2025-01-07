package org.exoticos;

import org.exoticos.exception.PackageNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, PackageNotFoundException {
        test();
    }

    private static void test() throws IOException, InterruptedException, PackageNotFoundException {
        //final String packageName = "hyprland-git";
//        final String packageName = "webkit2gtk";
        final String packageName = "rsync";

        SuperAurHelper superAurHelper = new SuperAurHelper();
        superAurHelper.scanDependencies(packageName, 1);
        superAurHelper.getPackageManager().deleteDuplicated();

        Map<Integer, List<SwPackage>> mapDeps = superAurHelper.getPackageManager().getMapPackageLevels();

        mapDeps.forEach((level, dependencies) -> {
            System.out.println("-----------------------------------");
            System.out.println("Level => " + level);
            System.out.println("-----------------------------------");

            dependencies.forEach(System.out::println);
        });

    }
}