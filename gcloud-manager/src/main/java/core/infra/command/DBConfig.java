package core.infra.command;

import java.util.List;

/**
 * @author neo
 */
public class DBConfig {
    public String project;
    public String env;
    public String instance;
    public Kube kube;
    public String rootSecret;
    public List<String> dbs;
    public List<User> users;
    public List<Endpoint> endpoints;

    public void validate() {
        if (env == null) throw new Error("env must not be null");

        for (User user : users) {
            if (user.name.length() > 32) {
                throw new Error("db user name must be no longer than 32, user=" + user.name);
            }
        }
    }

    public List<String> dbs(User user) {
        if (user.role == Role.REPLICATION) return List.of("*");   // for REPLICATION, scope is global, otherwise "ERROR 1221 (HY000): Incorrect usage of DB GRANT and GLOBAL PRIVILEGES"
        if (user.db == null) return dbs;
        return List.of(user.db);
    }

    public enum Auth {
        IAM, PASSWORD
    }

    public enum Role {
        APP, MIGRATION, VIEWER, REPLICATION
    }

    public static class Kube {
        public String name;
        public String zone;
    }

    public static class User {
        public String name;
        public Auth auth;
        public String secret;
        public String db;
        public Role role;

        public List<String> privileges() {
            return switch (role) {
                case APP -> List.of("SELECT", "INSERT", "UPDATE", "DELETE");
                case MIGRATION -> List.of("CREATE", "DROP", "INDEX", "ALTER", "EXECUTE", "SELECT", "INSERT", "UPDATE", "DELETE");
                case VIEWER -> List.of("SELECT");
                case REPLICATION -> List.of("REPLICATION SLAVE", "SELECT", "RELOAD", "REPLICATION CLIENT", "LOCK TABLES", "EXECUTE");
            };
        }
    }

    public static class Endpoint {
        public String name;
        public String ns;
    }
}
