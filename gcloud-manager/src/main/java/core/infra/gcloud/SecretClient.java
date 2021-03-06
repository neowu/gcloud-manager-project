package core.infra.gcloud;

import core.infra.util.JSON;
import core.infra.util.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @author neo
 */
public class SecretClient {
    private final Logger logger = LoggerFactory.getLogger(SecretClient.class);

    public String getOrCreateSecret(String project, String secret) {
        logger.info("access secret, secret={}", secret);
        Shell.Result result = Shell.execute("gcloud", "--project=" + project, "secrets", "versions", "access", "latest", "--secret=" + secret, "--format=json");
        if (!result.success() && result.error.contains("NOT_FOUND")) {
            logger.info("can not find secret, create new one, secret={}", secret);
            createSecret(project, secret);
            return addSecretVersion(project, secret, UUID.randomUUID().toString());
        }
        return JSON.fromJSON(AccessSecretResponse.class, result.output).data();
    }

    private void createSecret(String project, String secret) {
        Shell.Result result = Shell.execute("gcloud", "--project=" + project, "secrets", "create", secret, "--format=json", "--replication-policy=automatic");
        if (result.success() || result.error.contains("already exists")) return;
        throw new Error("failed to create secret, secret=" + secret + ", error=" + result.error);
    }

    private String addSecretVersion(String project, String secret, String value) {
        String[] commands = {"gcloud", "--project=" + project, "secrets", "versions", "add", secret, "--format=json", "--data-file=-"};
        Shell.Result result = Shell.execute(new Shell.Input(commands, value));
        if (!result.success()) throw new Error("failed to add secret version, secret=" + secret + ", error=" + result.error);
        return value;
    }

    // used to import existing password
    public void createSecretVersion(String project, String secret, String value) {
        createSecret(project, secret);
        addSecretVersion(project, secret, value);
    }
}
