Feature: Prueba de Cognito

    @prueba
    Scenario: Sign Up de usuario
        * def docNumber = "71813923"
        * def password = "Test1234."
        * def correo = "carlos.junco.matrix@gmail.com"
        * def phone = "+51989445875"
        * def referralCode = '123'
        * def signUpRequest = CognitoService.generateSignUpRequest(uuid, docNumber, password, correo, phone, referralCode, 'someDevice', 'someSessionToken')
        * print signUpRequest

        * def confirmSignUpRequest = CognitoService.generateConfirmSignUpRequest(uuid, '123456')
        * print confirmSignUpRequest

        * def initiateAuthRequest = CognitoService.generateInitiateAuthRequest(uuid, 'TestPassword123!', 'sessionToken', 'device')
        * print initiateAuthRequest