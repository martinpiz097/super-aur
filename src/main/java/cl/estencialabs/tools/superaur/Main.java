package cl.estencialabs.tools.superaur;

import cl.estencialabs.tools.superaur.exception.SuperAurException;
import cl.estencialabs.tools.superaur.pkg.PackageManager;

import java.util.List;

public class Main {
    public static void main(String[] args) throws SuperAurException {
        PackageManager packageManager = new PackageManager();
        List<String> listDeps = packageManager.executeDepNamesCommand("vkd3d-proton-git");
        listDeps.forEach(System.out::println);
    }
}
