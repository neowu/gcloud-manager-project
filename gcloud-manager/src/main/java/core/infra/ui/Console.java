package core.infra.ui;

import core.infra.command.ApplyKubeCommand;
import core.infra.command.SyncDBCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author neo
 */
public class Console {
    private final Logger logger = LoggerFactory.getLogger(Console.class);
    private final String command;
    private final Map<String, String> params;

    public Console(String[] args) {
        command = args[0];
        params = new HashMap<>(args.length - 1);
        for (int i = 1; i < args.length; i++) {
            // arg is in --name=value
            String arg = args[i];
            int index = arg.indexOf('=');
            params.put(arg.substring(2, index), arg.substring(index + 1));
        }
    }

    public void execute() throws Exception {
        logger.info("gcloud manager 0.01");
        switch (command) {
            case "db" -> syncDB();
            case "kube" -> applyKube();
            default -> throw new Error("unknown command, command=" + command);
        }
    }

    private void applyKube() {
        new ApplyKubeCommand().apply();
    }

    private void syncDB() throws Exception {
        Path config = Path.of(params.get("conf"));
        // TODO: 1. if params not have conf, use "db", 2. if it's dir, apply all json under it, 3. validate
        new SyncDBCommand(config).sync();
    }
}
