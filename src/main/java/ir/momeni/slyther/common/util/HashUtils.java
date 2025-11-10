package ir.momeni.slyther.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


/**
 * Utility class for hashing operations.
 * <p>
 * Currently provides:
 * <ul>
 *     <li>SHA-256 hash generation with hexadecimal output</li>
 * </ul>
 * <p>
 * Usage example:
 * <pre>
 *     String hash = HashUtils.sha256Hex("my-secret");
 * </pre>
 */
public class HashUtils {

    /**
     * Hashes a given input string using SHA-256 and returns a lowercase hexadecimal string.
     *
     * @param input the string to hash; must not be null
     * @return a 64-character hex-encoded SHA-256 hash
     * @throws IllegalStateException if SHA-256 is not supported (extremely rare on JVM)
     */
    public static String sha256Hex(String input) {
        try {

            // Initialize SHA-256 message digest
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Convert input string to UTF-8 bytes and compute hash
            byte[] d = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert byte array into lowercase hex string
            StringBuilder sb = new StringBuilder(d.length * 2);
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            // Should never happen on a standard Java runtime
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
