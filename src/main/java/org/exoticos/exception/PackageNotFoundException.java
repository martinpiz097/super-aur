package org.exoticos.exception;

public class PackageNotFoundException extends Exception {
    public PackageNotFoundException(String packageName) {
        super("Package " + packageName + " not found neither in ABS nor in AUR");
    }
}
