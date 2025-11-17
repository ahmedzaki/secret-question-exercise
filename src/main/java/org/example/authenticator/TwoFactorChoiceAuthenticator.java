package org.example.authenticator;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.example.authenticator.credential.provider.SecretQuestionCredentialProvider;
import org.example.authenticator.credential.provider.SecretQuestionCredentialProviderFactory;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.models.*;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TwoFactorChoiceAuthenticator implements CredentialValidator<SecretQuestionCredentialProvider>, Authenticator {
    private static final Logger logger = Logger.getLogger(SecretQuestionAuthenticator.class);

    public static final String FORM_FILE_NAME = "two-factor-choice.ftl";
    public static final String FLOW_NOTE_CHOICE_2FA_METHOD = "chosen2FAMethod";
    public static final String FORM_ATTR_2FA_METHODS = "methods";
    public static final String FORM_ATTR_CHOSEN_METHOD = "chosen_method";


    @Override
    public SecretQuestionCredentialProvider getCredentialProvider(KeycloakSession keycloakSession) {
        return (SecretQuestionCredentialProvider) keycloakSession
                .getProvider(CredentialProvider.class,
                        SecretQuestionCredentialProviderFactory.PROVIDER_ID);
    }

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        Response challenge = authenticationFlowContext.form()
                .setAttribute(FORM_ATTR_2FA_METHODS, getOptionsFromConfig(authenticationFlowContext))
                .createForm(FORM_FILE_NAME);
        authenticationFlowContext.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        MultivaluedMap<String, String> formData = authenticationFlowContext.getHttpRequest().getDecodedFormParameters();
        String chosenMethod = formData.getFirst(FORM_ATTR_CHOSEN_METHOD);

        if (Objects.isNull(chosenMethod)) {
            authenticationFlowContext.challenge(authenticationFlowContext.form()
                    .setError("Invalid selection").createForm(FORM_FILE_NAME));
            return;
        }

        AuthenticationSessionModel authSession = authenticationFlowContext.getAuthenticationSession();
        authSession.setAuthNote(FLOW_NOTE_CHOICE_2FA_METHOD, chosenMethod);

        authenticationFlowContext.attempted();

    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    }

    @Override
    public void close() {
    }

    private Map<String, String> getOptionsFromConfig(AuthenticationFlowContext context) {
        String questionsConfigString = null;

        if (Objects.nonNull(context.getAuthenticatorConfig())) {
            Map<String, String> configMap = context.getAuthenticatorConfig().getConfig();
            questionsConfigString = configMap.get(TwoFactorChoiceAuthenticatorFactory.CONFIG_OPTIONS);
        }

        if (Objects.isNull(questionsConfigString) || questionsConfigString.trim().isEmpty()) {
            logger.warn("missing second factor methods config, defaulting to otp");
            return Map.of("otp", "OTP");
        }

        return Arrays.stream(questionsConfigString.split(","))
                .map(String::trim)
                .map(s -> {
                    String[] option = s.split(":");
                    if (option.length > 1)
                        return Map.entry(option[0], option[1]);
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing
                ));
    }
}
