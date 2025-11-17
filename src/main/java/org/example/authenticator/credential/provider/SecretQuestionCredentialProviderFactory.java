package org.example.authenticator.credential.provider;

import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.CredentialProviderFactory;
import org.keycloak.models.KeycloakSession;

public class SecretQuestionCredentialProviderFactory implements CredentialProviderFactory<SecretQuestionCredentialProvider> {

    public static final String PROVIDER_ID =  "secret-question";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public CredentialProvider create(KeycloakSession session) {
        return new SecretQuestionCredentialProvider(session);
    }
}
