Feature: Respond to auth challenge

  Scenario:
    * Utils.sleep(3000)
    Given url "https://mobile.api.qa.io.pe/v1/auth"
    And configure headers = null
    And header content-type = "application/x-amz-json-1.1"
    And header x-amz-target = "AWSCognitoIdentityProviderService.RespondToAuthChallenge"
    And header cache-control = "no-store"
    And header x-amz-user-agent = "aws-amplify/6.0.27 framework/201"
    * def timestamp = Utils.getTimestamp()
    * def signature = SrpCalculator.generateSignature(srpA, srpB, salt, password, secretBlock, timestamp)
    * json jsonRequest = read("this:resources/respond_auth_challenge.json")
    * print jsonRequest
    And request jsonRequest
    When method POST
    Then status 200
    * print response
