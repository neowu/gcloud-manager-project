package core.infra.command;

import core.infra.gcloud.SecretClient;
import core.infra.util.JSON;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author neo
 */
public class MigrateDBSecretLabelCommand {
    private final SecretClient secretClient = new SecretClient();
    private final DBConfig config;

    public MigrateDBSecretLabelCommand(Path configPath) throws IOException {
        LoggerFactory.getLogger(SyncDBCommand.class).info("migrate db secret, config={}", configPath.toAbsolutePath());
        config = JSON.fromJSON(DBConfig.class, Files.readString(configPath));
        config.validate();
    }

    public void migrate() {
        secretClient.updateEnvLabel(config.project, secretName(config.rootSecret), config.env);
        for (DBConfig.User user : config.users) {
            secretClient.updateEnvLabel(config.project, secretName(user.secret), config.env);
        }
    }

    private String secretName(String name) {
        return config.env + "-" + name;
    }
}
