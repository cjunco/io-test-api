Feature: Create offer in dyn-{env}-matrix-lead-events-01 and dyn-{env}-matrix-leads-view-01

  @create_offer_complete
  Scenario: Create offer
    * string payload = read("this:resources/create_offer.json")
    Given url dynamoDbUrl
    And configure headers = null
    * json headersJson = Headers.getHeaders(amzPutAction, user.accessKey, user.secretKey, user.token, region, payload)
    And headers headersJson
    And header Accept = "application/json"
    And header X-Amz-Security-Token = user.token
    Given request payload
    When method POST
    Then status 200
    And match response == {}
    * Utils.sleep(200000)