package core.infra.gcloud;

import core.infra.util.Encodings;

import java.nio.charset.StandardCharsets;

/**
 * @author neo
 */
public class AccessSecretResponse {
    public String name;
    public Payload payload;

    public String data() {
        return new String(Encodings.decodeBase64(payload.data), StandardCharsets.UTF_8);
    }

    public static class Payload {
        public String data;
    }
}
