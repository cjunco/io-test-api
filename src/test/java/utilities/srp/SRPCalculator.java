package utilities.srp;

import org.bouncycastle.crypto.agreement.srp.SRP6StandardGroups;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SRPCalculator {

    private static final SecureRandom random = new SecureRandom();
    private static final SRP6GroupParameters params = SRP6StandardGroups.rfc5054_1024;
    private static final BigInteger N = params.getN();
    private static final BigInteger g = params.getG();

    public static String getSrpA(){
        return generateRandomSecret().toString();
    }

    public static BigInteger generateRandomSecret() {
        return new BigInteger(N.bitLength(), random).mod(N);
    }

    private static BigInteger hashToBigInteger(byte[] data) {
        SHA256Digest digest = new SHA256Digest();
        digest.update(data, 0, data.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return new BigInteger(1, result);
    }

    private static BigInteger calculateX(String salt, String password) {
        return hashToBigInteger((salt + password).getBytes(StandardCharsets.UTF_8));
    }

    private static BigInteger calculateU(BigInteger A, BigInteger B) {
        return hashToBigInteger((A.toString(16) + B.toString(16)).getBytes(StandardCharsets.UTF_8));
    }

    private static BigInteger calculateK() {
        return hashToBigInteger((N.toString(16) + g.toString(16)).getBytes(StandardCharsets.UTF_8));
    }

    public static String calculateHmacSHA256(String data, byte[] key) {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(key));
        byte[] resBuf = new byte[hmac.getMacSize()];
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        hmac.update(dataBytes, 0, dataBytes.length);
        hmac.doFinal(resBuf, 0);
        return Base64.getEncoder().encodeToString(resBuf);
    }

    public static String generateSignature(String srpA, String srpB, String salt, String password, String secretBlock, String timestamp) {
        BigInteger A = new BigInteger(srpA, 16);
        BigInteger B = new BigInteger(srpB, 16);

        BigInteger x = calculateX(salt, password);
        BigInteger u = calculateU(A, B);
        BigInteger k = calculateK();
        BigInteger gX = g.modPow(x, N);
        BigInteger uX = u.multiply(x);
        BigInteger a = generateRandomSecret();

        BigInteger SUser = B.subtract(k.multiply(gX)).modPow(a.add(uX), N);
        byte[] KUser = hashToBigInteger(SUser.toByteArray()).toByteArray();

        String dataToSign = secretBlock + timestamp;
        return calculateHmacSHA256(dataToSign, KUser);
    }

}
