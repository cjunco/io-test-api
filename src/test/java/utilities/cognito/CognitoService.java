package utilities.cognito;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CognitoService {

    private static final String CLIENT_ID = "3ubgt6n7n7j1cj8eu1imv3t318";
    private static final String CLIENT_SECRET = "1d3nbvm1ame65uom9g17moqo0reogdice2eigb1dvfhfslbm36dn";

    public static Map<String, Object> generateSignUpRequest(String userName, String dni, String password, String email, String phone, String referralCode, String device, String sessionToken) {
        Map<String, String> emailAttribute = new HashMap<>();
        emailAttribute.put("Name", "email");
        emailAttribute.put("Value", email);

        Map<String, String> phoneAttribute = new HashMap<>();
        phoneAttribute.put("Name", "phone_number");
        phoneAttribute.put("Value", phone);

        Map<String, String> documentNumberAttribute = new HashMap<>();
        documentNumberAttribute.put("Name", "custom:document_number");
        documentNumberAttribute.put("Value", dni);

        Map<String, String> referralCodeAttribute = new HashMap<>();
        referralCodeAttribute.put("Name", "custom:referralCode");
        referralCodeAttribute.put("Value", referralCode != null ? referralCode : "");

        List<Map<String, String>> userAttributes = new ArrayList<>();
        userAttributes.add(emailAttribute);
        userAttributes.add(phoneAttribute);
        userAttributes.add(documentNumberAttribute);
        userAttributes.add(referralCodeAttribute);

        Map<String, Object> clientMetadata = new HashMap<>();
        clientMetadata.put("device", device);
        clientMetadata.put("session", sessionToken);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Username", userName);
        requestBody.put("Password", password);
        requestBody.put("UserAttributes", userAttributes);
        requestBody.put("ClientMetadata", clientMetadata);
        requestBody.put("ClientId", CLIENT_ID);

        return requestBody;
    }

    private static String calculateSecretHash(String clientId, String clientSecret, String username) {
        final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
        SecretKeySpec signingKey = new SecretKeySpec(clientSecret.getBytes(StringUtils.UTF8), HMAC_SHA256_ALGORITHM);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(signingKey);
            mac.update(username.getBytes(StringUtils.UTF8));
            byte[] rawHmac = mac.doFinal(clientId.getBytes(StringUtils.UTF8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating secret hash", e);
        }
    }

    public static Map<String, Object> generateConfirmSignUpRequest(String username, String confirmationCode) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Username", username);
        requestBody.put("ConfirmationCode", confirmationCode);
        requestBody.put("ClientId", CLIENT_ID);

        return requestBody;
    }

    public static Map<String, Object> generateInitiateAuthRequest(String username, String password, String sessionToken, String device) throws NoSuchAlgorithmException {
        Map<String, String> srpParameters = SRPUtils.generateSRPParameters(username, password);

        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("SRP_A", srpParameters.get("SRP_A"));

        Map<String, Object> clientMetadata = new HashMap<>();
        clientMetadata.put("session", sessionToken);
        clientMetadata.put("device", device);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("AuthFlow", "USER_SRP_AUTH");
        requestBody.put("AuthParameters", authParameters);
        requestBody.put("ClientMetadata", clientMetadata);
        requestBody.put("ClientId", CLIENT_ID);

        return requestBody;
    }

}

