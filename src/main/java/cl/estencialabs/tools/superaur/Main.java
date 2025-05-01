package cl.estencialabs.tools.superaur;

import cl.estencialabs.tools.superaur.exception.SuperAurException;
import cl.estencialabs.tools.superaur.model.Package;
import cl.estencialabs.tools.superaur.pkg.PackageManager;
import cl.estencialabs.tools.superaur.pkg.YayAurHelper;

import java.util.List;

public class Main {
    public static void main(String[] args) throws SuperAurException {
        String pkgName =
                "vkd3d-proton-git"
//                "git"
                ;

        PackageManager packageManager = new PackageManager(new YayAurHelper());
//        List<String> listDeps = packageManager.executeDepNamesCommand(pkgName);
//        listDeps.forEach(System.out::println);

        Package pkg = packageManager.analyze(pkgName);
        System.out.println(pkg);
    }
}
