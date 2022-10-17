package core.infra.command;

import core.infra.db.MySQLClient;
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
import java.util.HashSet;
import java.util.Set;

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
        logger.info("load db config, config={}", configPath.toAbsolutePath());
        config = JSON.fromJSON(DBConfig.class, Files.readString(configPath));
        config.validate();
    }

    public void sync() throws Exception {
        logger.info("sync db");
        DescribeSQLResponse instance = sqlInstance.describe(config.project, config.instance);
        String rootPassword = secretClient.getOrCreateSecret(config.project, config.rootSecret, config.env);
        sqlInstance.changeRootPassword(config.project, config.instance, rootPassword);
        kubeClient.switchContext(config.project, config.kube.name, config.kube.zone);

        Set<String> namespaces = new HashSet<>();
        try (MySQLClient client = new MySQLClient(instance.publicIP(), "root", rootPassword)) {
            for (String db : config.dbs) {
                client.createDB(db);
            }
            for (DBConfig.User user : config.users) {
                switch (user.auth) {
                    case IAM -> {
                        client.grantUserPrivileges(user.name, config.dbs(user), user.privileges());
                    }
                    case PASSWORD -> {
                        String password = secretClient.getOrCreateSecret(config.project, user.secret, config.env);
                        client.createUser(user.name, password);
                        client.grantUserPrivileges(user.name, config.dbs(user), user.privileges());
                    }
                    default -> throw new Error("unknown auth, auth=" + user.auth);
                }
            }
            for (DBConfig.Endpoint endpoint : config.endpoints) {
                if (namespaces.add(endpoint.ns)) {
                    kubeClient.createNamespace(endpoint.ns);
                }
                kubeClient.createEndpoint(endpoint.ns, endpoint.name, instance.privateIP());
            }
        }
    }
}
