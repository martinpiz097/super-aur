package cl.estencialabs.tools.superaur.exception;

public class DependencyAlreadyExistsException extends RuntimeException {
    public DependencyAlreadyExistsException(String depName) {
        super("Dependency " + depName + " already exists in collection");
    }
}
