package core.infra.ui;

import core.infra.command.ApplyKubeCommand;
import core.infra.command.SyncDBCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        logger.info("gcloud manager 0.03");
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
        List<Path> configPaths = dbConfigPaths();
        for (Path config : configPaths) {
            new SyncDBCommand(config).sync();
        }
    }

    private List<Path> dbConfigPaths() {
        String conf = params.get("conf");
        File dbDir;
        if (conf == null) { // assume current dir is env dir
            dbDir = Path.of("db").toFile();
        } else {
            File file = Path.of(conf).toFile();
            if (file.isDirectory()) {
                dbDir = file;
            } else {
                return List.of(file.toPath());
            }
        }
        if (!dbDir.exists()) throw new Error("db dir doesn't exist, dir=" + dbDir.toPath().toAbsolutePath());
        return Arrays.stream(dbDir.listFiles((dir, name) -> name.endsWith(".json"))).map(File::toPath).collect(Collectors.toList());
    }
}
