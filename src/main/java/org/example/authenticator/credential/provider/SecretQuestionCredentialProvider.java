package org.example.authenticator.credential.provider;

import org.example.authenticator.SecretQuestionAuthenticatorFactory;
import org.example.authenticator.credential.models.SecretQuestionCredentialModel;
import org.keycloak.common.util.Time;
import org.keycloak.credential.*;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;

import java.util.Objects;

public class SecretQuestionCredentialProvider implements CredentialProvider<SecretQuestionCredentialModel>, CredentialInputValidator {

    protected KeycloakSession session;

    public SecretQuestionCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;
        return userModel.credentialManager().getStoredCredentialsByTypeStream(credentialType).findAny().isPresent();
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) {
            return false;
        }
        if (!credentialInput.getType().equals(getType())) {
            return false;
        }

        String challengeResponse = credentialInput.getChallengeResponse();
        if (Objects.isNull(challengeResponse)) {
            return false;
        }
        CredentialModel credentialModel = userModel.credentialManager().getStoredCredentialById(credentialInput.getCredentialId());
        SecretQuestionCredentialModel sQCM = getCredentialFromModel(credentialModel);

        String normalizedInput = challengeResponse.trim().toLowerCase();

        return normalizedInput.equals(sQCM.getSecretQuestionSecretData().getAnswer());
    }

    @Override
    public void close() {
        CredentialProvider.super.close();
    }

    @Override
    public String getType() {
        return SecretQuestionCredentialModel.TYPE;
    }

    @Override
    public CredentialModel createCredential(RealmModel realmModel,
                                            UserModel userModel,
                                            SecretQuestionCredentialModel secretQuestionCredentialModel) {
        if (secretQuestionCredentialModel.getCreatedDate() == null) {
            secretQuestionCredentialModel.setCreatedDate(Time.currentTimeMillis());
        }
        return userModel.credentialManager().createStoredCredential(secretQuestionCredentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realmModel, UserModel userModel, String credentialId) {
        return userModel.credentialManager().removeStoredCredentialById(credentialId);
    }

    @Override
    public SecretQuestionCredentialModel getCredentialFromModel(CredentialModel credentialModel) {
        return SecretQuestionCredentialModel.createFromCredentialModel(credentialModel);
    }

    @Override
    public SecretQuestionCredentialModel getCredentialForPresentationFromModel(CredentialModel model) {
        return SecretQuestionCredentialModel.createFromCredentialModel(model);
    }

    @Override
    public SecretQuestionCredentialModel getDefaultCredential(KeycloakSession session, RealmModel realm, UserModel user) {
        return CredentialProvider.super.getDefaultCredential(session, realm, user);
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext credentialTypeMetadataContext) {
        return CredentialTypeMetadata.builder()
                .type(SecretQuestionCredentialModel.TYPE)
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName(SecretQuestionCredentialProviderFactory.PROVIDER_ID)
                .helpText("secret-question-text")
                .createAction(SecretQuestionAuthenticatorFactory.PROVIDER_ID)
                .removeable(false)
                .build(session);
    }

    @Override
    public boolean supportsCredentialType(CredentialModel credential) {
        return getType().equals(credential.getType());
    }

    @Override
    public boolean supportsCredentialType(String type) {
        return getType().equals(type);
    }
}