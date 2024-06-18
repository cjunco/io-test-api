Feature: Post OTP

  Scenario: Post OTP for signup confirmation
    * Utils.sleep(3000)
    Given url "https://mobile.api.qa.io.pe/v1/auth"
    And configure headers = null
    And header content-type = "application/x-amz-json-1.1"
    And header x-amz-target = "AWSCognitoIdentityProviderService.ConfirmSignUp"
    And header cache-control = "no-store"
    And header x-amz-user-agent = "aws-amplify/6.0.27 auth/2 framework/201"
    * json jsonRequest = read("this:resources/post_otp.json")
    And request jsonRequest
    When method POST
    Then status 200
    And match response == {}
