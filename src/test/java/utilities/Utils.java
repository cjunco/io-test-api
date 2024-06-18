package utilities;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    public static String generateUUID() {
        UUID randomUUID = UUID.randomUUID();
        return randomUUID.toString();
    }

    public static String generateDocumentNumber() {
        int randomNumber = (int) (Math.random() * 10000000);
        return "6" + String.format("%07d", randomNumber);
    }

    public static String generatePhoneNumber() {
        long randomNumber = (long) (Math.random() * 100000000);
        return "9" + String.format("%08d", randomNumber);
    }

    public static Long generateDateTime(){
        Date date = new Date();
        return date.getTime();
    }

    public static String getTimestamp() {
        ZonedDateTime utcNow = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss 'UTC' yyyy", Locale.ENGLISH);
        return utcNow.format(formatter);
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getOTPCode(String json, String phoneCustom) {
        ObjectMapper objectMapper = new ObjectMapper();
        String latestOtpCode = null;
        long latestIngestionTime = Long.MIN_VALUE;

        try {
            JsonNode rootNode = objectMapper.readTree(json);
            ArrayNode events = (ArrayNode) rootNode.get("events");

            for (JsonNode event : events) {
                String message = event.get("message").asText();

                if (message != null && message.contains(phoneCustom) && phoneCustom.startsWith("+")) {
                    long ingestionTime = event.get("ingestionTime").asLong();

                    if (ingestionTime > latestIngestionTime) {
                        latestIngestionTime = ingestionTime;
                        String otpData = event.get("message").asText();
                        otpData = otpData.replace("\\\"", "\"");
                        latestOtpCode = extractOTPCode(otpData);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return latestOtpCode;
    }

    private static String extractOTPCode(String message) {
        int startIndex = message.indexOf("\"otpCode\":\"") + 11; // Length of "\"otpCode\":\""
        int endIndex = message.indexOf("\"", startIndex);
        if (startIndex != -1 && endIndex != -1) {
            return message.substring(startIndex, endIndex);
        }
        return null;
    }

}
