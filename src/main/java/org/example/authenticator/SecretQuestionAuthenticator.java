package org.example.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.example.authenticator.credential.models.SecretQuestionCredentialModel;
import org.example.authenticator.credential.provider.SecretQuestionCredentialProvider;
import org.example.authenticator.credential.provider.SecretQuestionCredentialProviderFactory;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.*;

import java.util.Objects;

public class SecretQuestionAuthenticator implements CredentialValidator<SecretQuestionCredentialProvider>, Authenticator {
    private static final String FORM_SECRET_QUESTION_ANSWER_ID = "secret_question_answer";
    private static final String FORM_SECRET_QUESTION_ID = "secret_question";
    public static final String FORM_FILE_NAME = "secret-question-verification.ftl";
    public static final String FORM_ATTR_LOGIN_SECRET_QUESTION = "loginSecretQuestion";
    private static final String FORM_ACTION_FALLBACK = "fallback";
    public static final String FORM_ATTR_ACTION = "action";

    @Override
    public SecretQuestionCredentialProvider getCredentialProvider(KeycloakSession keycloakSession) {
        return (SecretQuestionCredentialProvider) keycloakSession
                .getProvider(CredentialProvider.class,
                        SecretQuestionCredentialProviderFactory.PROVIDER_ID);
    }

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        CredentialModel credentialModel = authenticationFlowContext.getUser().credentialManager()
                .getStoredCredentialsByTypeStream(SecretQuestionCredentialModel.TYPE).findFirst().orElse(null);

        if(Objects.isNull(credentialModel)) {
            authenticationFlowContext.failure(AuthenticationFlowError.CLIENT_CREDENTIALS_SETUP_REQUIRED);
            return;
        }
        SecretQuestionCredentialModel sqCredentialsModel = getCredentialProvider(authenticationFlowContext.getSession())
                .getCredentialFromModel(credentialModel);

        Response challenge = authenticationFlowContext.form()
                .setAttribute(FORM_ATTR_LOGIN_SECRET_QUESTION, sqCredentialsModel.getSecretQuestionCredentialData().getQuestion())
                .createForm(FORM_FILE_NAME);

        authenticationFlowContext.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {

        MultivaluedMap<String, String> formData = authenticationFlowContext.getHttpRequest().getDecodedFormParameters();
        String action = formData.getFirst(FORM_ATTR_ACTION);

        if (FORM_ACTION_FALLBACK.equals(action)) {
            authenticationFlowContext.attempted();
            return;
        }

        boolean validated = validateAnswer(authenticationFlowContext);
        if (!validated) {
            String question = formData.getFirst(FORM_SECRET_QUESTION_ID);
            Response challenge = authenticationFlowContext.form()
                    .setAttribute(FORM_ATTR_LOGIN_SECRET_QUESTION, question)
                    .setError("Incorrect answer")
                    .createForm(FORM_FILE_NAME);
            authenticationFlowContext.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
        authenticationFlowContext.success();

    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return getCredentialProvider(keycloakSession).isConfiguredFor(realmModel, userModel, getType(keycloakSession));
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    }

    @Override
    public void close() {
    }

    protected boolean validateAnswer(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String secret = formData.getFirst(FORM_SECRET_QUESTION_ANSWER_ID);
        String credentialId = formData.getFirst("credentialId");
        if (credentialId == null || credentialId.isEmpty()) {
            credentialId = getCredentialProvider(context.getSession())
                    .getDefaultCredential(context.getSession(), context.getRealm(), context.getUser()).getId();
        }

        UserCredentialModel input = new UserCredentialModel(credentialId, getType(context.getSession()), secret);
        return getCredentialProvider(context.getSession()).isValid(context.getRealm(), context.getUser(), input);
    }

}
