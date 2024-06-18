package utilities.signature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Sign AWS Requests with Signature Version 4 Signing Process.
 */
public class AWSSignatureV4Generator {

    private final Logger LOGGER = LoggerFactory.getLogger(AWSSignatureV4Generator.class);
    private String accessKeyID;
    private String secretAccessKey;
    private String regionName;
    private String serviceName;
    private String httpMethodName;
    private String canonicalURI;
    private TreeMap<String, String> queryParametes;
    private TreeMap<String, String> awsHeaders;
    private String payload;
    // Other variables
    private String signedHeaderString;
    private String xAmzDate;
    private String currentDate;
    private String amzTarget;

    private AWSSignatureV4Generator(Builder builder) {
        accessKeyID = builder.accessKeyID;
        secretAccessKey = builder.secretAccessKey;
        regionName = builder.regionName;
        serviceName = builder.serviceName;
        httpMethodName = builder.httpMethodName;
        canonicalURI = builder.canonicalURI;
        queryParametes = builder.queryParametes;
        awsHeaders = builder.awsHeaders;
        payload = builder.payload;
        amzTarget = builder.amzTarget;

        // Get current timestamp value.(GTM)
        xAmzDate = getTimeStamp();
        currentDate = getDate();
    }

    /**
     * Task 1: Create a Canonical Request for Signature Version 4.
     *
     * @return Canonical Request.
     */
    private String prepareCanonicalRequest() throws Exception {

        StringBuilder canonicalURL = new StringBuilder("");

        // Step 1.1 Start with the HTTP request method (GET, PUT, POST, etc.), followed by a newline character.
        canonicalURL.append(httpMethodName).append("\n");

        // Step 1.2 Add the canonical URI parameter, followed by a newline character.
        canonicalURI = canonicalURI == null || canonicalURI.trim().isEmpty() ? "/" : canonicalURI;
        canonicalURL.append(canonicalURI).append("\n");

        // Step 1.3 Add the canonical query string, followed by a newline character.
        canonicalURL = addCanonicalQueryString(canonicalURL);

        // Step 1.4 Add the canonical headers, followed by a newline character.
        StringBuilder signedHeaders = new StringBuilder("");
        if (awsHeaders != null && !awsHeaders.isEmpty()) {
            for (Map.Entry<String, String> entrySet : awsHeaders.entrySet()) {
                String key = entrySet.getKey();
                signedHeaders.append(key.toLowerCase()).append(";");
                canonicalURL.append(key.toLowerCase()).append(":").append(entrySet.getValue()).append("\n");
            }
            /* Note: Each individual header is followed by a newline character, meaning the complete list ends with
             a newline character. */
            canonicalURL.append("\n");
        } else {
            canonicalURL.append("\n");
        }

        // Step 1.5 Add the signed headers, followed by a newline character.
        signedHeaderString = signedHeaders.substring(0, signedHeaders.length() - 1); // Remove last ";"
        canonicalURL.append(signedHeaderString).append("\n");

        /* Step 1.6 Use a hash (digest) function like SHA256 to create a hashed value from the payload in the body of
        the HTTP or HTTPS. */
        if (payload == null) {
            payload = "";
            canonicalURL.append(hash(payload));
        } else {
            canonicalURL.append(payload);
        }
        LOGGER.debug("Canonical Request: " + canonicalURL.toString());
        LOGGER.debug("Canonical Request: ==================================================");
        return canonicalURL.toString();
    }

    /**
     * Add the canonical query string, followed by a newline character.
     *
     * @param canonicalURL Canonical URL
     * @return Updated canonicalURL
     */
    private StringBuilder addCanonicalQueryString(StringBuilder canonicalURL) throws Exception {

        StringBuilder queryString = new StringBuilder("");
        if (queryParametes != null && !queryParametes.isEmpty()) {
            for (Map.Entry<String, String> entrySet : queryParametes.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                queryString.append(key).append("=").append(encodeParameter(value)).append("&");
            }

            queryString.deleteCharAt(queryString.lastIndexOf("&"));
            queryString.append("\n");
        } else {
            queryString.append("\n");
        }

        canonicalURL.append(queryString);
        return canonicalURL;
    }

    /**
     * Task 2: Create a String to Sign for Signature Version 4.
     */
    private String prepareStringToSign(String canonicalURL) throws Exception {

        String stringToSign;

        // Step 2.1 Start with the algorithm designation, followed by a newline character.
        stringToSign = "AWS4-HMAC-SHA256" + "\n";

        // Step 2.2 Append the request date value, followed by a newline character.
        stringToSign += xAmzDate + "\n";

        // Step 2.3 Append the credential scope value, followed by a newline character.
        stringToSign += currentDate + "/" + regionName + "/" + serviceName + "/" + "aws4_request" + "\n";

        /* Step 2.4 Append the hash of the canonical request that you created in Task 1: Create a Canonical Request
        for Signature Version 4. */
        String hashCanonicalURL = hash(canonicalURL);
        stringToSign += hashCanonicalURL;

        LOGGER.debug("String to sign, hashCanonicalURL  MMM  {}", hashCanonicalURL);
        LOGGER.debug("String to sign, canonicalURL  MMM  {}", canonicalURL);
        LOGGER.debug("String to sign1: =================================================");
        LOGGER.debug("String to sign: " + stringToSign);
        LOGGER.debug("String to sign2: =================================================");
        return stringToSign;
    }

    /**
     * Task 3: Calculate the AWS Signature Version 4.
     */
    private String calculateSignature(String stringToSign) throws Exception {

        try {
            // Step 3.1 Derive your signing key.
            LOGGER.debug("calculateSignature, secretAccessKey: " + secretAccessKey);
            LOGGER.debug("calculateSignature, currentDate: " + currentDate);
            LOGGER.debug("calculateSignature, regionName: " + regionName);
            LOGGER.debug("calculateSignature, serviceName: " + serviceName);
            byte[] signatureKey = getSignatureKey(secretAccessKey, currentDate, regionName, serviceName);

            // Step 3.2 Calculate the signature.
            byte[] signature = hmacSHA256(signatureKey, stringToSign);

            // Step 3.2.1 Encode signature (byte[]) to Hex.
            String sr = bytesToHex(signature);
            LOGGER.debug("xxxxxxxxxx calculateSignature, signatureStr: {}", sr);
            return sr;
        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException ex) {
            throw new Exception("Error while calculating the signature." + ex);
        }
    }

    public void setAmzDate(String date) {
        xAmzDate = date;
    }

    /*public void setAmzDate(String date) {
        xAmzDate = date;
    }*/

    /**
     * Task 4: Add the Signing Information to the Request. We'll return Map of
     * all headers put this headers in your request.
     *
     * @return Headers.
     */
    public Map<String, String> getHeaders(String hostHeader, String contentType) throws Exception {

        awsHeaders.put("X-Amz-Date", xAmzDate);

        // Execute Task 1: Create a Canonical Request for Signature Version 4.
        String canonicalURL = prepareCanonicalRequest();

        // Execute Task 2: Create a String to Sign for Signature Version 4.
        String stringToSign = prepareStringToSign(canonicalURL);

        // Execute Task 3: Calculate the AWS Signature Version 4.
        String signature = calculateSignature(stringToSign);

        Map<String, String> header = new HashMap<>(0);

        // accept;content-type;host;x-amz-content-sha256;x-amz-date;x-amz-security-token;x-amz-target;x-mock-response-name
        TreeMap<String, String> awsHeaders = new TreeMap<>();
        header.put("Host", hostHeader);
        if (!amzTarget.equals("AWSAppSync_20170331.createNotification")) {
            header.put("Content-Type", contentType);
        }
        header.put("X-Amz-Content-Sha256", payload);
        header.put("X-Amz-Date", xAmzDate);
        header.put("X-Amz-Target", amzTarget);
        header.put("Authorization", buildAuthorizationString(signature));
        //LOGGER.debug("##Signature:\n" + signature);
        //LOGGER.debug("##Header:");
        //for (Map.Entry<String, String> entrySet : header.entrySet()) {
        //    LOGGER.debug(entrySet.getKey() + " = " + entrySet.getValue());
        //}
        //LOGGER.debug("================================");
        return header;
    }

    /**
     * Build string for Authorization header.
     *
     * @param strSignature Signature value.
     * @return Authorization String.
     */
    private String buildAuthorizationString(String strSignature) {
        return "AWS4-HMAC-SHA256" + " "
                + "Credential=" + accessKeyID + "/" + getDate() + "/" + regionName + "/" + serviceName + "/"
                + "aws4_request" + ","
                + "SignedHeaders=" + signedHeaderString + ","
                + "Signature=" + strSignature;
    }

    /**
     * Hashes the string contents (assumed to be UTF-8) using the SHA-256 algorithm.
     *
     * @param data text to be hashed
     * @return SHA-256 hashed text
     */
    private String hash(String data) throws Exception {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new Exception("Error while hashing the string contents." + e);
        }
    }

    /**
     * Provides the HMAC SHA 256 encoded value(using the provided key) of the given data.
     *
     * @param data to be encoded.
     * @param key  to use for encoding.
     * @return HMAC SHA 256 encoded byte array.
     * @throws UnsupportedEncodingException The Character Encoding is not supported.
     * @throws InvalidKeyException          This is the exception for invalid Keys (invalid encoding, wrong length,
     *                                      uninitialized, etc).
     * @throws NoSuchAlgorithmException     When a particular cryptographic algorithm that is requested is not available.
     */
    private byte[] hmacSHA256(byte[] key, String data) throws UnsupportedEncodingException, InvalidKeyException,
            NoSuchAlgorithmException {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF-8"));
    }

    /**
     * Generate AWS signature key.
     *
     * @param key         Key to be used for signing.
     * @param date        Current date stamp.
     * @param regionName  Region name of AWS cloud directory.
     * @param serviceName Name of the service being addressed.
     * @return Signature key.
     * @throws UnsupportedEncodingException The Character Encoding is not supported.
     * @throws InvalidKeyException          This is the exception for invalid Keys (invalid encoding, wrong length,
     *                                      uninitialized, etc).
     * @throws NoSuchAlgorithmException     When a particular cryptographic algorithm that is requested is not available.
     */
    private byte[] getSignatureKey(String key, String date, String regionName, String serviceName)
            throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        //LOGGER.debug("================================ key: " + key);
        //LOGGER.debug("================================ date: " + date);
        //LOGGER.debug("================================ regionName: " + regionName);
        //LOGGER.debug("================================ serviceName: " + serviceName);

        byte[] kSecret = ("AWS4" + key).getBytes("UTF-8");
        byte[] kDate = hmacSHA256(kSecret, date);
        byte[] kRegion = hmacSHA256(kDate, regionName);
        byte[] kService = hmacSHA256(kRegion, serviceName);
        return hmacSHA256(kService, "aws4_request");
    }

    /**
     * Convert byte array to Hex.
     *
     * @param bytes bytes to be hex encoded.
     * @return hex encoded String of the given byte array.
     */
    private String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }

    /**
     * Get timestamp with yyyyMMdd'T'HHmmss'Z' format.
     *
     * @return Time stamp value.
     */
    private String getTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));// Set the server time as GTM
        return dateFormat.format(new Date());
    }

    /**
     * Get date with yyyyMMdd format.
     *
     * @return Date.
     */
    private String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));// Set the server time as GTM
        return dateFormat.format(new Date());
    }

    /**
     * Encode string value.
     *
     * @param param String value that need to be encode.
     * @return encoded string.
     */
    private String encodeParameter(String param) throws Exception {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Exception("Character encoding is not supported" + e);
        }
    }

    public static class Builder {

        private String accessKeyID;
        private String secretAccessKey;
        private String regionName;
        private String serviceName;
        private String httpMethodName;
        private String canonicalURI;
        private TreeMap<String, String> queryParametes;
        private TreeMap<String, String> awsHeaders;
        private String payload;
        private String amzTarget;

        public Builder(String accessKeyID, String secretAccessKey) {
            this.accessKeyID = accessKeyID;
            this.secretAccessKey = secretAccessKey;
        }

        public Builder regionName(String regionName) {
            this.regionName = regionName;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder httpMethodName(String httpMethodName) {
            this.httpMethodName = httpMethodName;
            return this;
        }

        public Builder canonicalURI(String canonicalURI) {
            this.canonicalURI = canonicalURI;
            return this;
        }

        public Builder queryParametes(TreeMap<String, String> queryParametes) {
            this.queryParametes = queryParametes;
            return this;
        }

        public Builder awsHeaders(TreeMap<String, String> awsHeaders) {
            this.awsHeaders = awsHeaders;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder amzTarget(String amzTarget) {
            this.amzTarget = amzTarget;
            return this;
        }

        public AWSSignatureV4Generator build() {
            return new AWSSignatureV4Generator(this);
        }
    }
}