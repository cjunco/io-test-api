Feature: Create cognito user

  @happy_path_create_user
  Scenario: Create cognito user
    Given url cognitoUrl
    And configure headers = null
    And header Content-Type = "application/x-amz-json-1.1"
    And header X-Amz-Target = "AWSCognitoIdentityProviderService.SignUp"
    * json jsonRequest = read("this:resources/create_cognito_user_request.json")
    And request jsonRequest
    When method POST
    Then status 200
    And match response == read("this:resources/create_cognito_user_response.json")

  @unhappy_path_create_user
  Scenario Outline: Scenario: <Scenario>
    Given url cognitoUrl
    And configure headers = null
    And header Content-Type = "application/x-amz-json-1.1"
    And header X-Amz-Target = "AWSCognitoIdentityProviderService.SignUp"
    And request
    """
    {
      "ClientId":"3csdsiggpur9fci9ren88vt383",
      "Username":"#(uuid)",
      "Password":"Test1234.",
      "ClientMetadata":
      {
        "password": "Test1234."
      },
      "UserAttributes":[
        {
          "Name":"email",
          "Value":"<Email>"
        },
        {
          "Name":"phone_number",
          "Value":"<PhoneNumber>"
        },
        {
          "Name": "custom:referralCode",
          "Value": ""
        },
        {
          "Name":"custom:document_number",
          "Value":"<DocumentNumber>"
        }
      ]
    }
    """
    When method POST
    Then status 400
    And match response.message == "<Message>"
    Examples:
      |Scenario                                    | Email                           | PhoneNumber     | DocumentNumber | Message     |
      |Email en blanco                             |                                 | +51987654321    | 42365813       | PreSignUp failed with error Invalid email.                                                                                                                                                                                                                 |
      |Email con caracteres extraños               | carlos.ju&co.matrix@gmail.com   | +51987654322    | 42365813       | PreSignUp failed with error Invalid email.                                                                                                                                                                                                                 |
      |Número de telefono en blanco                | carlos.junco.matrix@gmail.com   |                 | 42365813       | PreSignUp failed with error Error user with phone registered.                                                                                                                                                                                              |
      |Número de telefono con letras               | carlos.junco.matrix@gmail.com   | +5198765432a    | 42365813       | Invalid phone number format.                                                                                                                                                                                                                               |
      |Número de telefono con caracteres extraños  | carlos.junco.matrix@gmail.com   | +5198765432%    | 42365813       | Invalid phone number format.                                                                                                                                                                                                                               |
      |Número de documento en blanco               | carlos.junco.matrix@gmail.com   | +51987654323    |                | PreSignUp failed with error One or more parameter values are not valid. A value specified for a secondary index key is not supported. The AttributeValue for a key attribute cannot contain an empty string value. IndexName: by-dni-index, IndexKey: dni. |
      |Número de documento sin lead                | carlos.junco.matrix@gmail.com   | +51967283342    | 00000000       | PreSignUp failed with error Error user with phone registered.                                                                                                                                                                                              |
      |Número de documento con letras              | carlos.junco.matrix@gmail.com   | +51981276332    | 4236581a       | PreSignUp failed with error Error user with phone registered.                                                                                                                                                                                              |
      |Número de documento con caracteres extraños | carlos.junco.matrix@gmail.com   | +51912312414    | 4236581%       | PreSignUp failed with error Error user with phone registered.                                                                                                                                                                                              |
      |Número de telefono ya registrado            | carlos.junco.matrix@gmail.com   | +51980039400    | 42365813       | PreSignUp failed with error Error user with phone registered.                                                                                                                                                                                              |
      |Usuario ya registrado                       | carlos.junco.matrix@gmail.com   | +51909022101    | 71813923       | PreSignUp failed with error Error user with phone registered.                                                                                                                                                                                                      |