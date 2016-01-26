package com.liferay.docs.command;

/**
 * Created by carlos on 18/01/16.
 */
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    property = {
        "osgi.command.scope=liffey",
        "osgi.command.function=liffey"
    },
    service = Object.class
)
public class Liffey {

    public Object liffey(String ... args) {
        Command command = getCommand();
        return command.command(args);
    }

    public Command getCommand() {
        return _command;
    }

    @Reference
    public void setCommand(Command command) {
        _command = command;
    }

    private Command _command;

}
