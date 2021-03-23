package core.infra.db;

import core.infra.gcloud.DescribeSQLResponse;
import core.infra.gcloud.SQLInstanceClient;
import core.infra.gcloud.SecretClient;
import core.infra.kube.KubeClient;
import core.infra.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/**
 * @author neo
 */
public class SyncDBCommand {
    private final Logger logger = LoggerFactory.getLogger(SyncDBCommand.class);
    private final SQLInstanceClient sqlInstance = new SQLInstanceClient();
    private final SecretClient secretClient = new SecretClient();
    private final KubeClient kubeClient = new KubeClient();
    private final DBConfig config;

    public SyncDBCommand(Path configPath) throws IOException {
        logger.info("sync db, config={}", configPath.toAbsolutePath());
        config = JSON.fromJSON(DBConfig.class, Files.readString(configPath));
    }

    public void sync() throws Exception {
        DescribeSQLResponse instance = sqlInstance.describe(config.project, config.instance);
        String rootPassword = secretClient.getOrCreateSecret(config.project, config.rootSecret);
        sqlInstance.changeRootPassword(config.project, config.instance, rootPassword);
        kubeClient.switchContext(config.project, config.kube.name, config.kube.zone);
        try (MySQLClient client = new MySQLClient(instance.publicIP(), "root", rootPassword)) {
            for (String db : config.dbs) {
                client.createDB(db);
            }
            for (DBConfig.User user : config.users) {
                String password = createDBUser(client, user);
                if (user.kube != null) createKubeSecret(user, password);
            }
            for (DBConfig.Endpoint endpoint : config.endpoints) {
                kubeClient.createEndpoint(endpoint.ns, endpoint.name, instance.privateIP());
            }
        }
    }

    private String createDBUser(MySQLClient client, DBConfig.User user) throws SQLException {
        String password = secretClient.getOrCreateSecret(config.project, user.secret);
        switch (user.type) {
            case "MIGRATION" -> client.createUser(user.name, password, "*", List.of("CREATE", "DROP", "INDEX", "ALTER", "EXECUTE", "SELECT", "INSERT", "UPDATE", "DELETE"));
            case "APP" -> client.createUser(user.name, password, user.db, List.of("SELECT", "INSERT", "UPDATE", "DELETE"));
            case "READ_ONLY" -> client.createUser(user.name, password, "*", List.of("SELECT"));
            default -> throw new Error("unknown user type, type=" + user.type);
        }
        return password;
    }

    private void createKubeSecret(DBConfig.User user, String password) {
        kubeClient.createUserPasswordSecret(user.kube.ns, user.kube.secret, user.name, password);
    }
}
