package core.infra.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author neo
 */
public class Encodings {
    public static String base64(String value) {
        return base64(value.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    public static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value);
    }
}
