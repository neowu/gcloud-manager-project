package core.infra.command;

import core.infra.gcloud.SecretClient;
import core.infra.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author neo
 */
public class MigrateDBSecretLabelCommand {
    private final Logger logger = LoggerFactory.getLogger(MigrateDBSecretLabelCommand.class);
    private final SecretClient secretClient = new SecretClient();
    private final DBConfig config;

    public MigrateDBSecretLabelCommand(Path configPath) throws IOException {
        logger.info("load db config, config={}", configPath.toAbsolutePath());
        config = JSON.fromJSON(DBConfig.class, Files.readString(configPath));
        config.validate();
    }

    public void migrate() {
        logger.info("migrate db secret label");
        secretClient.updateEnvLabel(config.project, config.rootSecret, config.env);
        for (DBConfig.User user : config.users) {
            secretClient.updateEnvLabel(config.project, user.secret, config.env);
        }
    }
}
