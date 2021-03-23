package core.infra.gcloud;

import core.infra.util.JSON;
import core.infra.util.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class SQLInstanceClient {
    private final Logger logger = LoggerFactory.getLogger(SQLInstanceClient.class);

    public DescribeSQLResponse describe(String project, String instance) {
        logger.info("describe sql instance, instance={}", instance);
        Shell.Result result = Shell.execute("gcloud", "--project=" + project, "sql", "instances", "describe", instance, "--format=json");
        if (!result.success()) throw new Error("failed to describe sql instance, error=" + result.error);
        return JSON.fromJSON(DescribeSQLResponse.class, result.output);
    }

    public void changeRootPassword(String project, String instance, String password) {
        logger.info("change sql instance root password, instance={}", instance);
        Shell.Result result = Shell.execute("gcloud", "--project=" + project, "sql", "users", "set-password", "root", "--instance=" + instance, "--host=%", "--password=" + password);
        if (!result.success()) throw new Error("failed to set root password, error=" + result.error);
    }
}
