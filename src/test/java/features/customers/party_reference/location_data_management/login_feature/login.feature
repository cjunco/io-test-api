Feature: Login

  @happy_path_login
  Scenario: Login
    * def payload = karate.read("this:resources/login_request.json")
    Given url cognitoUrl
    * configure headers = null
    And header Content-Type = "application/x-amz-json-1.1"
    And header X-Amz-Target = "AWSCognitoIdentityProviderService.InitiateAuth"
    Given request payload
    When method POST
    Then status 200
    And match response == "#notnull"
    * def accessToken = response.AuthenticationResult.AccessToken
    And match accessToken == "#notnull"