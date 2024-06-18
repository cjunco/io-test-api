Feature: Get session token

  @token
  Scenario: Get session token for user
    Given url "https://mobile.api.qa.io.pe/v1/auth/session"
    And configure headers = null
    And header Content-Type = "application/x-amz-json-1.1"
    And header X-Amz-Target = "AWSCognitoIdentityProviderService.RevokeToken"
    And header X-api-key = "poQdDPfL7V3r6IMF4xSot3XRja3KzJsD3ISvdwDn"
    And header Authorization = "Bearer null"
    * json jsonRequest = read("this:resources/session_token.json")
    And request jsonRequest
    When method POST
    Then status 200
    * def sessionToken = response.token