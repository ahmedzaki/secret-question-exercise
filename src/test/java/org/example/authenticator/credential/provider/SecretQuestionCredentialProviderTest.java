package org.example.authenticator.credential.provider;

import org.example.authenticator.credential.models.SecretQuestionCredentialModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.credential.*;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
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
class SecretQuestionCredentialProviderTest {

    @Mock
    private KeycloakSession session;

    @Mock
    private RealmModel realm;

    @Mock
    private UserModel user;

    @Mock
    private UserCredentialManager credentialManager;

    @Mock
    private CredentialTypeMetadataContext metadataContext;

    private SecretQuestionCredentialProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SecretQuestionCredentialProvider(session);
        when(user.credentialManager()).thenReturn(credentialManager);
    }

    @Test
    void testGetType_ShouldReturnCorrectType() {
        assertEquals(SecretQuestionCredentialModel.TYPE, provider.getType());
    }

    @Test
    void testIsConfiguredFor_WithStoredCredential_ShouldReturnTrue() {
        // Given
        CredentialModel credentialModel = new CredentialModel();
        credentialModel.setType(SecretQuestionCredentialModel.TYPE);

        when(credentialManager.getStoredCredentialsByTypeStream(SecretQuestionCredentialModel.TYPE))
                .thenReturn(Stream.of(credentialModel));

        // When
        boolean result = provider.isConfiguredFor(realm, user, SecretQuestionCredentialModel.TYPE);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsConfiguredFor_WithoutStoredCredential_ShouldReturnFalse() {
        // Given
        when(credentialManager.getStoredCredentialsByTypeStream(SecretQuestionCredentialModel.TYPE))
                .thenReturn(Stream.empty());

        // When
        boolean result = provider.isConfiguredFor(realm, user, SecretQuestionCredentialModel.TYPE);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsConfiguredFor_WithWrongCredentialType_ShouldReturnFalse() {
        // When
        boolean result = provider.isConfiguredFor(realm, user, "WRONG_TYPE");

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValid_WithCorrectAnswer_ShouldReturnTrue() {
        // Given
        SecretQuestionCredentialModel sqModel = SecretQuestionCredentialModel
                .createSecretQuestion("What is your favorite food?", "Pizza");

        CredentialModel storedCredential = new CredentialModel();
        storedCredential.setId("cred-123");
        storedCredential.setType(SecretQuestionCredentialModel.TYPE);
        storedCredential.setCredentialData(sqModel.getCredentialData());
        storedCredential.setSecretData(sqModel.getSecretData());

        UserCredentialModel input = new UserCredentialModel(
                "cred-123",
                SecretQuestionCredentialModel.TYPE,
                "Pizza"
        );

        when(credentialManager.getStoredCredentialById("cred-123")).thenReturn(storedCredential);

        // When
        boolean result = provider.isValid(realm, user, input);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsValid_WithIncorrectAnswer_ShouldReturnFalse() {
        // Given
        SecretQuestionCredentialModel sqModel = SecretQuestionCredentialModel
                .createSecretQuestion("What is your favorite food?", "Pizza");

        CredentialModel storedCredential = new CredentialModel();
        storedCredential.setId("cred-123");
        storedCredential.setType(SecretQuestionCredentialModel.TYPE);
        storedCredential.setCredentialData(sqModel.getCredentialData());
        storedCredential.setSecretData(sqModel.getSecretData());

        UserCredentialModel input = new UserCredentialModel(
                "cred-123",
                SecretQuestionCredentialModel.TYPE,
                "WrongAnswer"
        );

        when(credentialManager.getStoredCredentialById("cred-123")).thenReturn(storedCredential);

        // When
        boolean result = provider.isValid(realm, user, input);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValid_WithNullChallengeResponse_ShouldReturnFalse() {
        // Given
        UserCredentialModel input = new UserCredentialModel(
                "cred-123",
                SecretQuestionCredentialModel.TYPE,
                null
        );

        // When
        boolean result = provider.isValid(realm, user, input);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsValid_WithWrongCredentialType_ShouldReturnFalse() {
        // Given
        UserCredentialModel input = new UserCredentialModel(
                "cred-123",
                "WRONG_TYPE",
                "Pizza"
        );

        // When
        boolean result = provider.isValid(realm, user, input);

        // Then
        assertFalse(result);
    }

    @Test
    void testCreateCredential_ShouldStoreCredential() {
        // Given
        SecretQuestionCredentialModel sqModel = SecretQuestionCredentialModel
                .createSecretQuestion("What is your favorite food?", "Pizza");

        CredentialModel storedCredential = new CredentialModel();
        when(credentialManager.createStoredCredential(any(CredentialModel.class)))
                .thenReturn(storedCredential);

        // When
        CredentialModel result = provider.createCredential(realm, user, sqModel);

        // Then
        assertNotNull(result);
        assertNotNull(sqModel.getCreatedDate());
        verify(credentialManager).createStoredCredential(sqModel);
    }

    @Test
    void testCreateCredential_WithExistingCreatedDate_ShouldNotOverwrite() {
        // Given
        SecretQuestionCredentialModel sqModel = SecretQuestionCredentialModel
                .createSecretQuestion("What is your favorite food?", "Pizza");
        Long existingDate = 1234567890L;
        sqModel.setCreatedDate(existingDate);

        CredentialModel storedCredential = new CredentialModel();
        when(credentialManager.createStoredCredential(any(CredentialModel.class)))
                .thenReturn(storedCredential);

        // When
        provider.createCredential(realm, user, sqModel);

        // Then
        assertEquals(existingDate, sqModel.getCreatedDate());
    }

    @Test
    void testDeleteCredential_ShouldRemoveCredential() {
        // Given
        String credentialId = "cred-123";
        when(credentialManager.removeStoredCredentialById(credentialId)).thenReturn(true);

        // When
        boolean result = provider.deleteCredential(realm, user, credentialId);

        // Then
        assertTrue(result);
        verify(credentialManager).removeStoredCredentialById(credentialId);
    }

    @Test
    void testDeleteCredential_WhenNotFound_ShouldReturnFalse() {
        // Given
        String credentialId = "non-existent";
        when(credentialManager.removeStoredCredentialById(credentialId)).thenReturn(false);

        // When
        boolean result = provider.deleteCredential(realm, user, credentialId);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetCredentialFromModel_ShouldConvertCorrectly() {
        // Given
        SecretQuestionCredentialModel original = SecretQuestionCredentialModel
                .createSecretQuestion("What is your favorite food?", "Pizza");

        CredentialModel credentialModel = new CredentialModel();
        credentialModel.setType(SecretQuestionCredentialModel.TYPE);
        credentialModel.setCredentialData(original.getCredentialData());
        credentialModel.setSecretData(original.getSecretData());
        credentialModel.setId("cred-123");

        // When
        SecretQuestionCredentialModel result = provider.getCredentialFromModel(credentialModel);

        // Then
        assertNotNull(result);
        assertEquals(SecretQuestionCredentialModel.TYPE, result.getType());
        assertEquals("What is your favorite food?", result.getSecretQuestionCredentialData().getQuestion());
        assertTrue("pizza".equalsIgnoreCase(result.getSecretQuestionSecretData().getAnswer()));
    }

    @Test
    void testSupportsCredentialType_WithCredentialModel_ShouldReturnTrue() {
        // Given
        CredentialModel credential = new CredentialModel();
        credential.setType(SecretQuestionCredentialModel.TYPE);

        // When
        boolean result = provider.supportsCredentialType(credential);

        // Then
        assertTrue(result);
    }

    @Test
    void testSupportsCredentialType_WithWrongCredentialModel_ShouldReturnFalse() {
        // Given
        CredentialModel credential = new CredentialModel();
        credential.setType("WRONG_TYPE");

        // When
        boolean result = provider.supportsCredentialType(credential);

        // Then
        assertFalse(result);
    }

    @Test
    void testSupportsCredentialType_WithString_ShouldReturnTrue() {
        // When
        boolean result = provider.supportsCredentialType(SecretQuestionCredentialModel.TYPE);

        // Then
        assertTrue(result);
    }

    @Test
    void testSupportsCredentialType_WithWrongString_ShouldReturnFalse() {
        // When
        boolean result = provider.supportsCredentialType("WRONG_TYPE");

        // Then
        assertFalse(result);
    }

    @Test
    void testClose_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> provider.close());
    }
}
