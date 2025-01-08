package org.exoticos;

import lombok.extern.java.Log;
import org.exoticos.exception.PackageNotFoundException;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Log
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, PackageNotFoundException {
        test();
    }

    private static void test() throws IOException, InterruptedException, PackageNotFoundException {
        //final String packageName = "hyprland-git";
//        final String packageName = "webkit2gtk";

        final String packageName = "rsync";

        SuperAurHelper superAurHelper = new SuperAurHelper();
        superAurHelper.scanDependencies(packageName);

        Map<Integer, List<SwPackage>> mapDeps = superAurHelper.getPackageManager().getMapPackageLevels();

        mapDeps.forEach((level, dependencies) -> {
            log.info("-----------------------------------");
            log.info("Level => " + level);
            log.info("-----------------------------------");

            dependencies.forEach(System.out::println);
        });

    }
}