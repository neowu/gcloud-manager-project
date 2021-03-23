package core.infra.ui;

import core.infra.db.SyncDBCommand;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class Console {
    private final String command;
    private final Map<String, String> params = new HashMap<>();

    public Console(String[] args) {
        command = args[0];
        for (int i = 1; i < args.length; i++) {
            // arg is in --name=value
            String arg = args[i];
            int index = arg.indexOf('=');
            params.put(arg.substring(2, index), arg.substring(index + 1));
        }
    }

    public void execute() throws Exception {
        switch (command) {
            case "db" -> syncDB();
            default -> throw new Error("unknown command, command=" + command);
        }
    }

    private void syncDB() throws Exception {
        Path config = Path.of(params.get("conf"));
        new SyncDBCommand(config).sync();
    }
}
