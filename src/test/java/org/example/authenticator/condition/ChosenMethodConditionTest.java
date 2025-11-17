package org.example.authenticator.condition;

import org.example.authenticator.TwoFactorChoiceAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChosenMethodConditionTest {

    @Mock
    private AuthenticationFlowContext context;

    @Mock
    private KeycloakSession session;

    @Mock
    private RealmModel realm;

    @Mock
    private UserModel user;

    @Mock
    private AuthenticatorConfigModel authenticatorConfig;

    @Mock
    private AuthenticationSessionModel authenticationSession;

    private ChosenMethodCondition condition;

    @BeforeEach
    void setUp() {
        condition = new ChosenMethodCondition();

        when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfig);
        when(context.getAuthenticationSession()).thenReturn(authenticationSession);
    }

    @Test
    void testMatchCondition_WithMatchingMethod_ShouldReturnTrue() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, "otp");

        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(authenticationSession.getAuthNote(TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD))
                .thenReturn("otp");

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertTrue(result);
    }

    @Test
    void testMatchCondition_WithNonMatchingMethod_ShouldReturnFalse() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, "otp");

        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(authenticationSession.getAuthNote(TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD))
                .thenReturn("webAuth");

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertFalse(result);
    }

    @Test
    void testMatchCondition_WithCaseInsensitiveMatch_ShouldReturnTrue() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, "OTP");

        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(authenticationSession.getAuthNote(TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD))
                .thenReturn("otp");

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertTrue(result);
    }

    @Test
    void testMatchCondition_WithNullConfig_ShouldReturnFalse() {
        // Given
        when(authenticatorConfig.getConfig()).thenReturn(null);

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertFalse(result);
    }

    @Test
    void testMatchCondition_WithNullAuthenticatorConfig_ShouldReturnFalse() {
        // Given
        when(context.getAuthenticatorConfig()).thenReturn(null);

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertFalse(result);
    }

    @Test
    void testMatchCondition_WithNullExpectedValue_ShouldReturnFalse() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, null);

        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertFalse(result);
    }

    @Test
    void testMatchCondition_WithEmptyExpectedValue_ShouldReturnFalse() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, "");

        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertFalse(result);
    }

    @Test
    void testMatchCondition_WithNullChosenMethod_ShouldReturnFalse() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, "otp");

        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(authenticationSession.getAuthNote(TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD))
                .thenReturn(null);

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertFalse(result);
    }

    @Test
    void testMatchCondition_WithSecretQuestionMethod_ShouldWork() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, "secretQuestion");

        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(authenticationSession.getAuthNote(TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD))
                .thenReturn("secretQuestion");

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertTrue(result);
    }

    @Test
    void testMatchCondition_WithRecoveryMethod_ShouldWork() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE, "recovery");

        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(authenticationSession.getAuthNote(TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD))
                .thenReturn("recovery");

        // When
        boolean result = condition.matchCondition(context);

        // Then
        assertTrue(result);
    }

    @Test
    void testRequiresUser_ShouldReturnTrue() {
        assertTrue(condition.requiresUser());
    }

    @Test
    void testAction_ShouldDoNothing() {
        // When/Then
        assertDoesNotThrow(() -> condition.action(context));
    }

    @Test
    void testSetRequiredActions_ShouldDoNothing() {
        // When/Then
        assertDoesNotThrow(() -> condition.setRequiredActions(session, realm, user));
    }

    @Test
    void testClose_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> condition.close());
    }
}
