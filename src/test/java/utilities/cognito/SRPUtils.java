package utilities.cognito;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

public class SRPUtils {

    private static final SecureRandom random = new SecureRandom();
    private static final SRP6GroupParameters params = SRP6StandardGroups.rfc5054_2048;
    private static final BigInteger N = params.getN();
    private static final BigInteger g = params.getG();

    public static Map<String, String> generateSRPParameters(String username, String password) throws NoSuchAlgorithmException {
        BigInteger a = new BigInteger(256, random); // Private ephemeral value
        BigInteger A = g.modPow(a, N); // Public ephemeral value

        // Use a cryptographic hash function to generate x (SHA-256)
        String combined = username + ":" + password;
        BigInteger x = new BigInteger(1, sha256(combined));

        BigInteger S = A.multiply(g.modPow(x, N)).mod(N); // SRP session key (simplified)

        Map<String, String> parameters = new HashMap<>();
        parameters.put("USERNAME", username);
        parameters.put("SRP_A", A.toString(16));
        // Include other parameters required by AWS Cognito SRP authentication flow
        // For example: parameters.put("CLIENT_ID", "your_client_id_here");

        return parameters;
    }

    public static String calculatePasswordClaimSignature(String username, String password, String secretBlock, String timestamp) {
        try {
            // Use a cryptographic hash function to generate x (SHA-256)
            String combined = username + ":" + password;
            BigInteger x = new BigInteger(1, sha256(combined));

            // Derive the SRP session key (S) and calculate the HMAC
            BigInteger S = new BigInteger(secretBlock, 16);
            String message = Base64.getEncoder().encodeToString(secretBlock.getBytes()) + timestamp;
            byte[] hmac = hmacSha256(S.toByteArray(), message.getBytes());

            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating PASSWORD_CLAIM_SIGNATURE", e);
        }
    }

    private static byte[] sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input.getBytes());
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key);
        return digest.digest(data);
    }
}
