# Keycloak Secret Question Authenticator

A custom Keycloak authenticator that provides secret question-based two-factor authentication (2FA) with flexible configuration options.

## Features

- **Secret Question Registration**: Users can select from predefined security questions during registration
- **Secret Question Verification**: Validates user answers during authentication
- **2FA Choice Mechanism**: Allows users to choose between multiple 2FA methods (OTP, Security Key, Recovery Code, Secret Question)
- **Conditional Flow Support**: Route authentication based on chosen 2FA method
- **Configurable Questions**: Administrators can customize available security questions
- **Fallback Options**: Users can switch to alternative verification methods

## Components

### Authenticators

1. **SecretQuestionRegistration** (`secret-question-registration`)
    - Allows users to select and answer a secret question during registration
    - Configurable list of questions
    - Stores encrypted credentials

2. **SecretQuestionAuthenticator** (`secret-question-authenticator`)
    - Verifies secret question answers during authentication
    - Supports fallback to other authentication methods
    - Displays user's chosen question

3. **TwoFactorChoiceAuthenticator** (`two-factor-choice-authenticator`)
    - Presents multiple 2FA options to users
    - Stores user's choice in authentication session
    - Configurable authentication methods

### Conditional Authenticator

**ChosenMethodCondition** (`chosen-second-factor-condition`)
- Evaluates which 2FA method was selected
- Enables conditional flow routing
- Configurable expected values

### Credential Provider

**SecretQuestionCredentialProvider** (`secret-question`)
- Manages secret question credentials
- Validates answers
- Integrates with Keycloak credential management

## Installation

### Prerequisites

- Keycloak 26.x or later
- Java 21 or later
- Maven 3.8+

### Build

```bash
mvn clean package
```

### Deploy

1. Copy the generated JAR to Keycloak's `providers` directory:
   ```bash
   cp target/keycloak-secret-question-authenticator-*.jar $KEYCLOAK_HOME/providers/
   ```

2. Rebuild Keycloak (for Quarkus distribution):
   ```bash
   $KEYCLOAK_HOME/bin/kc.sh build
   ```

3. Restart Keycloak:
   ```bash
   $KEYCLOAK_HOME/bin/kc.sh start
   ```

## Configuration

### 1. Create Authentication Flow

1. Navigate to **Authentication** → **Flows**
2. Create a new flow or copy an existing one
3. Add **Secret Question Registration** to registration flow
4. Add **Two Factor Choice** and **Secret Question Authenticator** to authentication flow

### 2. Configure Secret Questions

**Secret Question Registration Configuration:**
- **Secret Questions**: Comma-separated list of questions
  ```
  What is your favorite food?,What was the name of your first pet?,What city were you born in?
  ```

**Two Factor Choice Configuration:**
- **Other 2FA options**: Comma-separated key:value pairs
  ```
  otp:One Time Password,webAuth:Security Key,recovery:Recovery Code,secretQuestion:Secret Question
  ```

## Usage

### User Registration

1. User completes standard registration fields
2. Secret Question Registration screen appears
3. User selects a question from dropdown
4. User provides an answer
5. Credential is stored securely

### User Authentication

1. User enters username and password
2. Two Factor Choice screen appears (if configured)
3. User selects "Secret Question" (or appropriate option)
4. User answers their chosen secret question
5. Access is granted upon correct answer

### Fallback Authentication

If a user cannot answer their secret question:
1. Click "Other verification options" button
2. Flow returns to 2FA choice screen
3. User can select alternative method (OTP, Recovery Code, etc.)
