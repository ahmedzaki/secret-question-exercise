package org.example.authenticator;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TwoFactorChoiceAuthenticatorTest {

    @Mock
    private AuthenticationFlowContext context;

    @Mock
    private KeycloakSession session;

    @Mock
    private RealmModel realm;

    @Mock
    private UserModel user;

    @Mock
    private LoginFormsProvider loginFormsProvider;

    @Mock
    private HttpRequest httpRequest;

    @Mock
    private Response response;

    @Mock
    private AuthenticatorConfigModel authenticatorConfig;

    @Mock
    private AuthenticationSessionModel authenticationSession;

    private TwoFactorChoiceAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new TwoFactorChoiceAuthenticator();

        when(context.getSession()).thenReturn(session);
        when(context.getRealm()).thenReturn(realm);
        when(context.getUser()).thenReturn(user);
        when(context.form()).thenReturn(loginFormsProvider);
        when(context.getHttpRequest()).thenReturn(httpRequest);
        when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfig);
        when(context.getAuthenticationSession()).thenReturn(authenticationSession);

        when(loginFormsProvider.setAttribute(anyString(), any())).thenReturn(loginFormsProvider);
        when(loginFormsProvider.setError(anyString())).thenReturn(loginFormsProvider);
        when(loginFormsProvider.createForm(anyString())).thenReturn(response);
    }

    @Test
    void testAuthenticate_WithCustomConfig_ShouldChallengeWithCustomOptions() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(TwoFactorChoiceAuthenticatorFactory.CONFIG_OPTIONS,
                "otp:One Time Password,webAuth:Security Key,recovery:Recovery Code");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        authenticator.authenticate(context);

        // Then
        ArgumentCaptor<Map<String, String>> methodsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(loginFormsProvider).setAttribute(
                eq(TwoFactorChoiceAuthenticator.FORM_ATTR_2FA_METHODS),
                methodsCaptor.capture()
        );

        Map<String, String> methods = methodsCaptor.getValue();
        assertEquals(3, methods.size());
        assertEquals("One Time Password", methods.get("otp"));
        assertEquals("Security Key", methods.get("webAuth"));
        assertEquals("Recovery Code", methods.get("recovery"));
    }

    @Test
    void testAction_WithValidChoice_ShouldStoreNoteAndAttempt() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("chosen_method", "otp");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // When
        authenticator.action(context);

        // Then
        verify(authenticationSession).setAuthNote(
                TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD,
                "otp"
        );
        verify(context).attempted();
        verify(context, never()).challenge(any());
    }

    @Test
    void testAction_WithNullChoice_ShouldChallengeWithError() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("chosen_method", null);

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // When
        authenticator.action(context);

        // Then
        verify(loginFormsProvider).setError("Invalid selection");
        verify(context).challenge(any());
        verify(context, never()).attempted();
    }

    @Test
    void testGetOptionsFromConfig_WithMultipleOptions_ShouldParsCorrectly() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(TwoFactorChoiceAuthenticatorFactory.CONFIG_OPTIONS,
                "otp:OTP Method,secretQuestion:Secret Question,webAuth:WebAuthn");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        authenticator.authenticate(context);

        // Then
        ArgumentCaptor<Map<String, String>> methodsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(loginFormsProvider).setAttribute(
                eq(TwoFactorChoiceAuthenticator.FORM_ATTR_2FA_METHODS),
                methodsCaptor.capture()
        );

        Map<String, String> methods = methodsCaptor.getValue();
        assertEquals(3, methods.size());
        assertEquals("OTP Method", methods.get("otp"));
        assertEquals("Secret Question", methods.get("secretQuestion"));
        assertEquals("WebAuthn", methods.get("webAuth"));
    }

    @Test
    void testGetOptionsFromConfig_WithMalformedOptions_ShouldIgnoreInvalid() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(TwoFactorChoiceAuthenticatorFactory.CONFIG_OPTIONS,
                "otp:OTP,invalidformat,webAuth:WebAuthn");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        authenticator.authenticate(context);

        // Then
        ArgumentCaptor<Map<String, String>> methodsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(loginFormsProvider).setAttribute(
                eq(TwoFactorChoiceAuthenticator.FORM_ATTR_2FA_METHODS),
                methodsCaptor.capture()
        );

        Map<String, String> methods = methodsCaptor.getValue();
        assertEquals(2, methods.size());
        assertEquals("OTP", methods.get("otp"));
        assertEquals("WebAuthn", methods.get("webAuth"));
        assertNull(methods.get("invalidformat"));
    }

    @Test
    void testGetOptionsFromConfig_WithDuplicateKeys_ShouldKeepFirst() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(TwoFactorChoiceAuthenticatorFactory.CONFIG_OPTIONS,
                "otp:First OTP,otp:Second OTP");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        authenticator.authenticate(context);

        // Then
        ArgumentCaptor<Map<String, String>> methodsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(loginFormsProvider).setAttribute(
                eq(TwoFactorChoiceAuthenticator.FORM_ATTR_2FA_METHODS),
                methodsCaptor.capture()
        );

        Map<String, String> methods = methodsCaptor.getValue();
        assertEquals("First OTP", methods.get("otp"));
    }

    @Test
    void testGetOptionsFromConfig_WithEmptyConfig_ShouldReturnDefaultOTP() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(TwoFactorChoiceAuthenticatorFactory.CONFIG_OPTIONS, "  ");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        authenticator.authenticate(context);

        // Then
        ArgumentCaptor<Map<String, String>> methodsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(loginFormsProvider).setAttribute(
                eq(TwoFactorChoiceAuthenticator.FORM_ATTR_2FA_METHODS),
                methodsCaptor.capture()
        );

        Map<String, String> methods = methodsCaptor.getValue();
        assertEquals(1, methods.size());
        assertEquals("OTP", methods.get("otp"));
    }

    @Test
    void testGetOptionsFromConfig_WithNullAuthenticatorConfig_ShouldReturnDefaultOTP() {
        // Given
        when(context.getAuthenticatorConfig()).thenReturn(null);

        // When
        authenticator.authenticate(context);

        // Then
        ArgumentCaptor<Map<String, String>> methodsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(loginFormsProvider).setAttribute(
                eq(TwoFactorChoiceAuthenticator.FORM_ATTR_2FA_METHODS),
                methodsCaptor.capture()
        );

        Map<String, String> methods = methodsCaptor.getValue();
        assertEquals(1, methods.size());
        assertEquals("OTP", methods.get("otp"));
    }

    @Test
    void testRequiresUser_ShouldReturnTrue() {
        assertTrue(authenticator.requiresUser());
    }

    @Test
    void testConfiguredFor_ShouldAlwaysReturnTrue() {
        // When
        boolean result = authenticator.configuredFor(session, realm, user);

        // Then
        assertTrue(result);
    }

    @Test
    void testClose_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> authenticator.close());
    }

    @Test
    void testSetRequiredActions_ShouldDoNothing() {
        // When/Then
        assertDoesNotThrow(() -> authenticator.setRequiredActions(session, realm, user));
    }
}
