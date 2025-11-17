package org.example.authenticator;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.example.authenticator.credential.models.SecretQuestionCredentialModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SecretQuestionRegistrationTest {

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
    private LoginFormsProvider loginFormsProvider;

    @Mock
    private HttpRequest httpRequest;

    @Mock
    private Response response;

    @Mock
    private AuthenticatorConfigModel authenticatorConfig;

    private SecretQuestionRegistration registration;

    @BeforeEach
    void setUp() {
        registration = new SecretQuestionRegistration();

        when(context.getSession()).thenReturn(session);
        when(context.getRealm()).thenReturn(realm);
        when(context.getUser()).thenReturn(user);
        when(context.form()).thenReturn(loginFormsProvider);
        when(context.getHttpRequest()).thenReturn(httpRequest);
        when(context.getAuthenticatorConfig()).thenReturn(authenticatorConfig);

        when(user.credentialManager()).thenReturn(credentialManager);

        when(loginFormsProvider.setAttribute(anyString(), any())).thenReturn(loginFormsProvider);
        when(loginFormsProvider.setError(anyString())).thenReturn(loginFormsProvider);
        when(loginFormsProvider.createForm(anyString())).thenReturn(response);
    }

    @Test
    void testAuthenticate_ShouldChallengeWithForm() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(SecretQuestionRegistrationFactory.CONFIG_QUESTIONS,
                "Question1,Question2,Question3");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        registration.authenticate(context);

        // Then
        verify(loginFormsProvider).setAttribute(
                eq(SecretQuestionRegistration.FORM_QUESTIONS_ATTRIBUTE),
                anyList()
        );
        verify(loginFormsProvider).createForm(SecretQuestionRegistration.FORM_REGISTRATION_FILE_NAME);
        verify(context).challenge(response);
    }

    @Test
    void testAction_WithValidQuestionAndAnswer_ShouldSucceed() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("secret_question", "What is your favorite food?");
        formData.putSingle("secret_question_answer", "Pizza");

        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);
        when(credentialManager.createStoredCredential(any(CredentialModel.class)))
                .thenReturn(new CredentialModel());

        // When
        registration.action(context);

        // Then
        ArgumentCaptor<CredentialModel> credentialCaptor = ArgumentCaptor.forClass(CredentialModel.class);
        verify(credentialManager).createStoredCredential(credentialCaptor.capture());

        CredentialModel captured = credentialCaptor.getValue();
        assertEquals(SecretQuestionCredentialModel.TYPE, captured.getType());

        verify(context).success();
        verify(context, never()).challenge(any());
    }

    @Test
    void testAction_WithNullQuestion_ShouldChallengeWithError() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("secret_question", null);
        formData.putSingle("secret_question_answer", "Pizza");

        Map<String, String> config = new HashMap<>();
        config.put(SecretQuestionRegistrationFactory.CONFIG_QUESTIONS, "Question1,Question2");
        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // When
        registration.action(context);

        // Then
        verify(loginFormsProvider).setError("missingSecretQuestionOrAnswer");
        verify(context).challenge(any());
        verify(context, never()).success();
    }

    @Test
    void testAction_WithNullAnswer_ShouldChallengeWithError() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("secret_question", "What is your favorite food?");
        formData.putSingle("secret_question_answer", null);

        Map<String, String> config = new HashMap<>();
        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // When
        registration.action(context);

        // Then
        verify(loginFormsProvider).setError("missingSecretQuestionOrAnswer");
        verify(context).challenge(any());
        verify(context, never()).success();
    }

    @Test
    void testAction_WithEmptyAnswer_ShouldChallengeWithError() {
        // Given
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle("secret_question", "What is your favorite food?");
        formData.putSingle("secret_question_answer", "   ");

        Map<String, String> config = new HashMap<>();
        when(authenticatorConfig.getConfig()).thenReturn(config);
        when(httpRequest.getDecodedFormParameters()).thenReturn(formData);

        // When
        registration.action(context);

        // Then
        verify(loginFormsProvider).setError("missingSecretQuestionOrAnswer");
        verify(context).challenge(any());
        verify(context, never()).success();
    }

    @Test
    void testGetQuestionsFromConfig_WithCustomQuestions_ShouldReturnCustomList() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(SecretQuestionRegistrationFactory.CONFIG_QUESTIONS,
                "Custom Q1, Custom Q2, Custom Q3");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        registration.authenticate(context);

        // Then
        ArgumentCaptor<List<String>> questionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(loginFormsProvider).setAttribute(
                eq(SecretQuestionRegistration.FORM_QUESTIONS_ATTRIBUTE),
                questionsCaptor.capture()
        );

        List<String> questions = questionsCaptor.getValue();
        assertEquals(3, questions.size());
        assertTrue(questions.contains("Custom Q1"));
        assertTrue(questions.contains("Custom Q2"));
        assertTrue(questions.contains("Custom Q3"));
    }

    @Test
    void testGetQuestionsFromConfig_WithNullConfig_ShouldReturnPredefinedQuestions() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(SecretQuestionRegistrationFactory.CONFIG_QUESTIONS, null);
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        registration.authenticate(context);

        // Then
        ArgumentCaptor<List<String>> questionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(loginFormsProvider).setAttribute(
                eq(SecretQuestionRegistration.FORM_QUESTIONS_ATTRIBUTE),
                questionsCaptor.capture()
        );

        List<String> questions = questionsCaptor.getValue();
        assertEquals(SecretQuestionRegistrationFactory.PREDEFINED_QUESTIONS, questions);
    }

    @Test
    void testGetQuestionsFromConfig_WithEmptyConfig_ShouldReturnPredefinedQuestions() {
        // Given
        Map<String, String> config = new HashMap<>();
        config.put(SecretQuestionRegistrationFactory.CONFIG_QUESTIONS, "  ");
        when(authenticatorConfig.getConfig()).thenReturn(config);

        // When
        registration.authenticate(context);

        // Then
        ArgumentCaptor<List<String>> questionsCaptor = ArgumentCaptor.forClass(List.class);
        verify(loginFormsProvider).setAttribute(
                eq(SecretQuestionRegistration.FORM_QUESTIONS_ATTRIBUTE),
                questionsCaptor.capture()
        );

        List<String> questions = questionsCaptor.getValue();
        assertEquals(SecretQuestionRegistrationFactory.PREDEFINED_QUESTIONS, questions);
    }

    @Test
    void testRequiresUser_ShouldReturnTrue() {
        assertTrue(registration.requiresUser());
    }

    @Test
    void testConfiguredFor_ShouldAlwaysReturnTrue() {
        // When
        boolean result = registration.configuredFor(session, realm, user);

        // Then
        assertTrue(result);
    }

    @Test
    void testClose_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> registration.close());
    }

    @Test
    void testSetRequiredActions_ShouldDoNothing() {
        // When/Then
        assertDoesNotThrow(() -> registration.setRequiredActions(session, realm, user));
    }
}
