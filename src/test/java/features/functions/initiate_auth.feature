Feature: Initiate auth

  Scenario: Get secret_block
    * Utils.sleep(3000)
    Given url "https://mobile.api.qa.io.pe/v1/auth"
    And configure headers = null
    And header content-type = "application/x-amz-json-1.1"
    And header x-amz-target = "AWSCognitoIdentityProviderService.InitiateAuth"
    And header cache-control = "no-store"
    And header x-amz-user-agent = "aws-amplify/6.0.27 auth/4 framework/201"
    * def srpA = SrpCalculator.getSrpA()
    * json jsonRequest = read("this:resources/initiate_auth.json")
    And request jsonRequest
    When method POST
    Then status 200
    And match response == "#notnull"
    * def salt = response.ChallengeParameters.SALT
    * def secretBlock = response.ChallengeParameters.SECRET_BLOCK
    * def srpB = response.ChallengeParameters.SRP_B
    And match salt == "#notnull"
    And match secretBlock == "#notnull"
    And match srpB == "#notnull"
