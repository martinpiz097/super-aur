package cl.estencialabs.tools.superaur.cmd;

import java.util.stream.Stream;

public record CommandResult(String output, int exitCode, Throwable throwable) {
    public boolean isSucessful() {
        return throwable == null;
    }

}
