package utilities.signature;

import net.minidev.json.JSONObject;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class Headers {

    public static String getHeaders(String amzTarget, String accessKey, String secretKey, String token, String region, String payload) throws Exception {

        String accessKeyID = accessKey;
        String secretAccessKey = secretKey;
        String securityToken = token;
        String hostHeader = "";
        String contentType = "";
        String service = "";

        String payloadHash = hash(payload);

        switch (amzTarget) {
            case "DynamoDB_20120810.PutItem": {
                hostHeader = "dynamodb." + region + ".amazonaws.com";
                contentType = "application/x-amz-json-1.0; charset=UTF-8";
                service = "dynamodb";
                break;
            }
            case "Logs_20140328.DescribeLogStreams":
            case "Logs_20140328.GetLogEvents": {
                hostHeader = "logs." + region + ".amazonaws.com";
                contentType = "application/x-amz-json-1.1; charset=utf-8";
                service = "logs";
                break;
            }
        }

        String canonicalURI = "/";

        String xAmzDate = getTimeStamp();

        TreeMap<String, String> awsHeaders = new TreeMap<>();
        awsHeaders.put("Host", hostHeader);
        if(!contentType.isEmpty()) {
            awsHeaders.put("Content-Type", contentType);
        }
        awsHeaders.put("X-Amz-Content-Sha256", payloadHash);
        awsHeaders.put("X-Amz-Date", xAmzDate);
        if(!amzTarget.equals("AWSAppSync_20170331.createNotification")) {
            awsHeaders.put("X-Amz-Target", amzTarget);
        } else {
            awsHeaders.put("X-Amz-Security-Token", token);
            canonicalURI = "/graphql";
        }

        AWSSignatureV4Generator aWSV4Auth = new AWSSignatureV4Generator.Builder(accessKeyID, secretAccessKey)
                .regionName(region)
                .serviceName(service) // es - elastic search. use your service name
                .httpMethodName("POST") //GET, PUT, POST, DELETE, etc...
                .canonicalURI(canonicalURI) //end point
                //.queryParametes(queryParametes) //query parameters if any
                .awsHeaders(awsHeaders) //aws header parameters
                .payload(payloadHash) // payload if any
                .amzTarget(amzTarget)
                .build();

        aWSV4Auth.setAmzDate(xAmzDate);

        Map<String, String> header = aWSV4Auth.getHeaders(hostHeader, contentType);
        JSONObject json = new JSONObject();
        for (Map.Entry<String, String> entrySet : header.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            json.put(key,value);
        }
        return json.toString();
    }

    private static String hash(String data) throws Exception {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data.getBytes("UTF-8"));
            byte[] digest = messageDigest.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (Exception  e) {
            e.printStackTrace();
            throw new Exception("Error while hashing the string contents." + e);
        }
    }

    private static String getTimeStamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GTM"));
        return dateFormat.format(new Date());
    }
}
