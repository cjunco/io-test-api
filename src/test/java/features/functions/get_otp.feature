Feature: Get OTP

  @get_otp
  Scenario: Get OTP for the user based on phone number
    * Utils.sleep(15000)
    * def amzTarget = "Logs_20140328.DescribeLogStreams"
    * string payload = karate.read("this:resources/log_streams.json")
    * print payload
    Given url logUrl
    And configure headers = null
    * json headersJson = Headers.getHeaders(amzTarget, user.accessKey, user.secretKey, user.token, region, payload)
    And headers headersJson
    And header Accept = "application/json"
    And header X-Amz-Security-Token = user.token
    Given request payload
    When method POST
    Then status 200
    * def logStreams = response.logStreams

    * def amzTarget = "Logs_20140328.GetLogEvents"
    * def otplogStreams = logStreams[0].logStreamName
    * print otplogStreams
    * string payload = karate.read("this:resources/get_otp.json")
    Given url logUrl
    And configure headers = null
    * json headersJson = Headers.getHeaders(amzTarget, user.accessKey, user.secretKey, user.token, region, payload)
    And headers headersJson
    And header X-Amz-Security-Token = user.token
    Given request payload
    When method POST
    Then status 200
    * string response = response
    * def otpCode_0 = Utils.getOTPCode(response, phoneCognito)

    * def otplogStreams = logStreams[1].logStreamName
    * print otplogStreams
    * string payload = karate.read("this:resources/get_otp.json")
    Given url logUrl
    And configure headers = null
    * json headersJson = Headers.getHeaders(amzTarget, user.accessKey, user.secretKey, user.token, region, payload)
    And headers headersJson
    And header X-Amz-Security-Token = user.token
    Given request payload
    When method POST
    Then status 200
    * string response = response
    * def otpCode_1 = Utils.getOTPCode(response, phoneCognito)

    * def otplogStreams = logStreams[2].logStreamName
    * print otplogStreams
    * string payload = karate.read("this:resources/get_otp.json")
    Given url logUrl
    And configure headers = null
    * json headersJson = Headers.getHeaders(amzTarget, user.accessKey, user.secretKey, user.token, region, payload)
    And headers headersJson
    And header X-Amz-Security-Token = user.token
    Given request payload
    When method POST
    Then status 200
    * string response = response
    * def otpCode_2 = Utils.getOTPCode(response,phoneCognito)

    * def otpCode = otpCode_0 == null ? (otpCode_1 == null ? (otpCode_2 == null ? "OTP no existe" : otpCode_2 ) : otpCode_1  ) : otpCode_0
