Feature: validacion

  Scenario: validacion
    Given url "https://mobile.api.qa.io.pe/v1/me/onboarding/document/upload-intent"
    And header x-api-key = "poQdDPfL7V3r6IMF4xSot3XRja3KzJsD3ISvdwDn"
    And header Accept = "application/json"
    And header Authorization = 'Bearer ' + accessToken
    When method POST
    Then status 200
    And match response == "#notnull"
    * def frontSignedUrl = response.frontSignedUrl
    And match frontSignedUrl == "#notnull"
    * def reverseSignedUrl = response.reverseSignedUrl
    And match reverseSignedUrl == "#notnull"
    * print response
    * print frontSignedUrl
    * print reverseSignedUrl