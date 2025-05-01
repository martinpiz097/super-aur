package cl.estencialabs.tools.superaur.cmd;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandInterpreter {

    private CommandResult getSucessfulResult(Process process) throws IOException {
        final byte[] processOutputBytes = process.getInputStream().readAllBytes();
        return new CommandResult(new String(processOutputBytes),
                process.exitValue(),
                null);
    }

    private CommandResult getErrorResult(Throwable throwable) {
        return new CommandResult(null, -1, throwable);
    }

    private CommandResult getProcessResult(Process process) {
        try {
            process.waitFor(5, TimeUnit.SECONDS);
            return getSucessfulResult(process);
        } catch (Exception e) {
            return getErrorResult(e);
        }
    }

    public CommandResult execute(List<String> listCmd) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(listCmd);
            Process process = processBuilder.start();
            return getProcessResult(process);
        } catch (Exception e) {
            return new CommandResult(null, -1, e);
        }
    }

    public CommandResult execute(String... cmd) {
       return execute(Arrays.asList(cmd));
    }

    public CommandResult execute(Command command) {
        return execute(command.toStringList());
    }
}
