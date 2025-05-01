package cl.estencialabs.tools.superaur.pkg;

import cl.estencialabs.tools.superaur.cmd.CommandInterpreter;
import cl.estencialabs.tools.superaur.cmd.CommandResult;

public abstract class AurHelper {
    protected final CommandInterpreter commandInterpreter;
    protected final String name;
    protected final String packageInfoOption;

    protected AurHelper() {
        this.commandInterpreter = new CommandInterpreter();
        this.name = setupHelperName();
        this.packageInfoOption = setupHelperInfoOpt();
    }

    protected abstract String setupHelperName();
    protected abstract String setupHelperInfoOpt();

    public CommandResult execPackageInfoCommand(String pkgName) {
        return commandInterpreter.execute(name, packageInfoOption, pkgName);
    }
}
