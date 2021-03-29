package core.infra.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author neo
 */
public class Shell {
    public static Result execute(String... commands) {
        return execute(new Input(commands, null));
    }

    public static Result execute(Input input) {
        try {
            Process process = new ProcessBuilder().command(input.commands).start();
            if (input.input != null) {
                OutputStream output = process.getOutputStream();
                output.write(input.input.getBytes(StandardCharsets.UTF_8));
                output.flush();
            }
            int status = process.waitFor();
            InputStream inputStream = process.getInputStream();
            InputStream error = process.getErrorStream();
            return new Result(status, new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), new String(error.readAllBytes(), StandardCharsets.UTF_8));

        } catch (IOException | InterruptedException e) {
            throw new Error("failed to execute command, error=" + e.getMessage(), e);
        }
    }

    public static class Input {
        public final String[] commands;
        public final String input;

        public Input(String[] commands, String input) {
            this.commands = commands;
            this.input = input;
        }
    }

    public static class Result {
        public final int status;
        public final String output;
        public final String error;

        public Result(int status, String output, String error) {
            this.status = status;
            this.output = output;
            this.error = error;
        }

        public boolean success() {
            return status == 0;
        }
    }
}
