function fn() {
    var Utils = Java.type('utilities.Utils');
    var Headers = Java.type('utilities.signature.Headers');
    var SrpCalculator = Java.type('utilities.srp.SRPCalculator');
    var CognitoService = Java.type('utilities.cognito.CognitoService');
    var CognitoService2 = Java.type('utilities.cognito.CognitoService');

    var env = karate.env; // get system property 'karate.env'
    karate.log('karate.env system property was:', env);

    if (!env) {
        env = 'dev';
    }
    var config = {
        env: env,
        amzPutAction: 'DynamoDB_20120810.PutItem',
        cognitoUrl: 'https://cognito-idp.us-east-1.amazonaws.com',
        dynamoDbUrl: 'https://dynamodb.us-east-1.amazonaws.com',
        logGroupName: '/aws/lambda/lmb-qa-matrix-user-cmd_on-send-recovery-code-01',
        logUrl: 'https://logs.us-east-1.amazonaws.com',
        region: 'us-east-1'
    }

    if (env == 'dev') {
        // customize
    } else if (env == 'e2e') {
        // customize
    }

    config.Utils = Utils;
    config.Headers = Headers;
    config.SrpCalculator = SrpCalculator;
    config.CognitoService = CognitoService;
    config.CognitoService2 = new CognitoService2();

    config.uuid = Utils.generateUUID();
    config.docNumber = Utils.generateDocumentNumber();
    config.phone = Utils.generatePhoneNumber();
    config.phoneCognito = "+51" + config.phone;
    config.email = "correo." + config.uuid + "@gmail.com";
    config.password = "Test1234.";
    config.currentDate = Utils.generateDateTime().toString();
    config.sourceLead = "lead/" + config.docNumber + "-creditcard";
    config.idLead = config.docNumber + "-creditcard";

    config.clientId = "3ubgt6n7n7j1cj8eu1imv3t318";

    config.user = karate.read('classpath:users/CarlosJunco.json');
    config.paths = karate.read('classpath:paths/feature_paths.json');

    return config;
}
