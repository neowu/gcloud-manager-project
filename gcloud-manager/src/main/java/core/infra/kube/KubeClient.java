package core.infra.kube;

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
        String resources = String.format("""
                apiVersion: v1
                kind: Namespace
                metadata:
                  name: %1$s
                ---
                apiVersion: v1
                data:
                  user: %3$s
                  password: %4$s
                kind: Secret
                metadata:
                  name: %2$s
                  namespace: %1$s
                type: Opaque
                """, ns, secret, Encodings.base64(user), Encodings.base64(password));
        apply(resources);
    }

    public void createEndpoint(String ns, String name, String ip) {
        logger.info("create endpoint, ns={}, name={}, ip={}", ns, name, ip);
        String resources = String.format("""
                apiVersion: v1
                kind: Service
                metadata:
                  name: %2$s
                  namespace: %1$s
                spec:
                  clusterIP: None
                ---
                apiVersion: v1
                kind: Endpoints
                metadata:
                  name: %2$s
                  namespace: %1$s
                subsets:
                - addresses:
                  - ip: %3$s
                """, ns, name, ip);
        apply(resources);
    }

    private void apply(String resources) {
        String[] commands = {"kubectl", "apply", "-f", "-"};
        Shell.Result result = Shell.execute(new Shell.Input(commands, resources));
        if (!result.success()) throw new Error("failed to apply kube resources, error=" + result.error);
    }
}
