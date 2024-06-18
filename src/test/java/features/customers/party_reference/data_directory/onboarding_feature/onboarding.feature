Feature: Onboarding complete

  @offer_and_onboarding_complete_truora
  Scenario: Onboarding complete when offer does not exist
    # CREATE OFFER
    * call read(paths.create_offer)
    # CREATE COGNITO USER
    * call read(paths.create_cognito_user)
    # GET OTP
    * call read(paths.get_otp)
    # POST OTP
    * call read(paths.post_otp)
    # LOGIN
    * call read(paths.login)
    # VALIDATION
    * call read(paths.validation_user)

  @onboarding_complete_truora
  Scenario: Onboarding complete when offer exist
    * def docNumber = "71813923"
    * def email = "carlos.junco.matrix@gmail.com"
    # CREATE COGNITO USER
    * call read(paths.create_cognito_user)
    # GET OTP
    * call read(paths.get_otp)
    # POST OTP
    * call read(paths.post_otp)
    # LOGIN
    * call read(paths.login)
    # VALIDATION
    * call read(paths.validation_user)

  @onboarding_complete_facephi
  Scenario: Onboarding complete when offer exist
    * def docNumber = "71813923"
    * def email = "carlos.junco.matrix@gmail.com"
    # GET SESSION TOKEN
    * call read(paths.session_token)
    # CREATE COGNITO USER
    * call read(paths.create)
    # GET OTP
    * call read(paths.get_otp)
    # POST OTP
    * call read(paths.post_otp)
    # INITIATE AUTH
    * call read(paths.initiate_auth)
    # RESPOND TO AUTH CHALLENGE
    * call read(paths.respond_auth_challenge)
