package core.infra.kube;

import core.infra.util.ClasspathResources;
import core.infra.util.Encodings;
import core.infra.util.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class KubeClient {
    private final Logger logger = LoggerFactory.getLogger(KubeClient.class);

    public void switchContext(String project, String cluster, String zone) {
        logger.info("switch kube context, cluster={}", cluster);
        Shell.Result result = Shell.execute("gcloud", "--project=" + project, "container", "clusters", "get-credentials", cluster, "--zone=" + zone);
        if (!result.success()) throw new Error("failed to switch kube context, error=" + result.error);
    }

    public void createUserPasswordSecret(String ns, String secret, String user, String password) {
        logger.info("create kube secret, ns={}, secret={}", ns, secret);
        String resources = String.format(ClasspathResources.text("kube/secret-template.yaml"), ns, secret, Encodings.base64(user), Encodings.base64(password));
        apply(resources);
    }

    public void createEndpoint(String ns, String name, String ip) {
        logger.info("create endpoint, ns={}, name={}, ip={}", ns, name, ip);
        String resources = String.format(ClasspathResources.text("kube/endpoint-template.yaml"), ns, name, ip);
        apply(resources);
    }

    public void createNamespace(String ns) {
        logger.info("create namespace, ns={}", ns);
        Shell.Result result = Shell.execute(new Shell.Input(new String[]{"kubectl", "create", "ns", ns}, ""));
        if (!result.success()) {
            if (result.error.contains("already exists")) {
                logger.info("namespace exists, ns={}", ns);
            } else {
                throw new Error("failed to create namespace, error=" + result.error);
            }
        }
    }

    private void apply(String resources) {
        Shell.Result result = Shell.execute(new Shell.Input(new String[]{"kubectl", "apply", "-f", "-"}, resources));
        if (!result.success()) throw new Error("failed to apply kube resources, error=" + result.error);
    }
}
