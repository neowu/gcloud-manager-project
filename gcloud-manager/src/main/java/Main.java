import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class Main {
    public static void main(String[] args) {
        new Main(args).execute();
    }

    private final Logger messageLogger = LoggerFactory.getLogger("message");
    private final String[] args;

    private Main(String... args) {
        this.args = args;
    }

    private void execute() {
        messageLogger.info("hello world");
    }
}
