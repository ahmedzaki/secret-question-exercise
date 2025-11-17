package org.example.authenticator;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.example.authenticator.credential.models.SecretQuestionCredentialModel;
import org.example.authenticator.credential.provider.SecretQuestionCredentialProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecretQuestionAuthenticatorTest {

    @Mock
    private AuthenticationFlowContext context;

    @Mock
    private KeycloakSession session;

    @Mock
    private RealmModel realm;

    @Mock
    private UserModel user;

    @Mock
    private UserCredentialManager credentialManager;

    @Mock
    private SecretQuestionCredentialProvider credentialProvider;

    @Mock
    private LoginFormsProvider loginFormsProvider;

    @Mock
    private HttpRequest httpRequest;

    @Mock
    private Response response;

    private SecretQuestionAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new SecretQuestionAuthenticator();

        when(context.getSession()).thenReturn(session);
        when(context.getRealm()).thenReturn(realm);
        when(context.getUser()).thenReturn(user);
        when(context.form()).thenReturn(loginFormsProvider);
        when(context.getHttpRequest()).thenReturn(httpRequest);

        when(user.credentialManager()).thenReturn(credentialManager);
        when(session.getProvider(CredentialProvider.class, "secret-question"))
                .thenReturn(credentialProvider);

        when(loginFormsProvider.setAttribute(anyString(), any())).thenReturn(loginFormsProvider);
        when(loginFormsProvider.setError(anyString())).thenReturn(loginFormsProvider);
        when(loginFormsProvider.createForm(anyString())).thenReturn(response);
    }

    @Test
    void testAuthenticate_WithExistingCredential_ShouldChallengeUser() {
        // Given
        CredentialModel credentialModel = new CredentialModel();
        credentialModel.setType(SecretQuestionCredentialModel.TYPE);

        SecretQuestionCredentialModel sqModel = SecretQuestionCredentialModel
                .createSecretQuestion("What is your favorite food?", "Pizza");

        when(credentialManager.getStoredCredentialsByTypeStream(SecretQuestionCredentialModel.TYPE))
                .thenReturn(Stream.of(credentialModel));
        when(credentialProvider.getCredentialFromModel(credentialModel)).thenReturn(sqModel);

        // When
        authenticator.authenticate(context);

        // Then
        verify(loginFormsProvider).setAttribute(
                eq(SecretQuestionAuthenticator.FORM_ATTR_LOGIN_SECRET_QUESTION),
                eq("What is your favorite food?")
        );
        verify(loginFormsProvider).createForm(SecretQuestionAuthenticator.FORM_FILE_NAME);
        verify(context).challenge(response);
        verify(context, never()).failure(any());
    }

    @Test
    void testAuthenticate_WithoutCredential_ShouldFailWithSetupRequired() {
        // Given
        when(credentialManager.getStoredCredentialsByTypeStream(SecretQuestionCredentialModel.TYPE))
                .thenReturn(Stream.empty());

        // When
        authenticator.authenticate(context);

        // Then
        verify(context).failure(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED);
        verify(context, never()).challenge(any());
    }

    @Test
    void testAction_WithCorrectAnswer_ShouldSucceed() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("secret_question_answer", "Pizza");
        formData.putSingle("credentialId", "cred-123");
        formData.putSingle("action", "submit");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);
        when(credentialProvider.isValid(eq(realm), eq(user), any(UserCredentialModel.class)))
                .thenReturn(true);

        // When
        authenticator.action(context);

        // Then
        verify(context).success();
        verify(context, never()).failureChallenge(any(), any());
    }

    @Test
    void testAction_WithIncorrectAnswer_ShouldFailChallenge() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("secret_question_answer", "WrongAnswer");
        formData.putSingle("secret_question", "What is your favorite food?");
        formData.putSingle("credentialId", "cred-123");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);
        when(credentialProvider.isValid(eq(realm), eq(user), any(UserCredentialModel.class)))
                .thenReturn(false);

        // When
        authenticator.action(context);

        // Then
        verify(context).failureChallenge(
                eq(AuthenticationFlowError.INVALID_CREDENTIALS),
                any(Response.class)
        );
        verify(loginFormsProvider).setError("Incorrect answer");
        verify(context, never()).success();
    }

    @Test
    void testAction_WithFallbackAction_ShouldAttempt() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("action", "fallback");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // When
        authenticator.action(context);

        // Then
        verify(context).attempted();
        verify(context, never()).success();
        verify(context, never()).failureChallenge(any(), any());
    }

    @Test
    void testRequiresUser_ShouldReturnTrue() {
        assertTrue(authenticator.requiresUser());
    }

    @Test
    void testConfiguredFor_WithoutStoredCredential_ShouldReturnFalse() {
        // Given
        when(credentialProvider.isConfiguredFor(eq(realm), eq(user), anyString()))
                .thenReturn(false);

        // When
        boolean result = authenticator.configuredFor(session, realm, user);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetCredentialProvider_ShouldReturnCorrectProvider() {
        // When
        SecretQuestionCredentialProvider result = authenticator.getCredentialProvider(session);

        // Then
        assertNotNull(result);
        assertEquals(credentialProvider, result);
    }

    @Test
    void testClose_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> authenticator.close());
    }
}
