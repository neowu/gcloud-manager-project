package core.infra.command;

import java.util.List;

/**
 * @author neo
 */
public class DBConfig {
    public String project;
    public String instance;
    public Kube kube;
    public String rootSecret;
    public List<String> dbs;
    public List<User> users;
    public List<Endpoint> endpoints;

    public static class Kube {
        public String name;
        public String zone;
    }

    public static class User {
        public String name;
        public String secret;
        public KubeSecret kube;
        public String type;
        public String db;
    }

    public static class Endpoint {
        public String name;
        public String ns;
    }

    public static class KubeSecret {
        public String ns;
        public String secret;
    }
}
