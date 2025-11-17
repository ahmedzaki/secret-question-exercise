package org.example.authenticator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import java.util.List;

public class SecretQuestionRegistrationFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "secret-question-registration";
    public static final String CONFIG_QUESTIONS = "secretQuestionsList";

    private static final SecretQuestionRegistration SINGLETON = new SecretQuestionRegistration();
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    protected static List<String> PREDEFINED_QUESTIONS = List.of(
            "What is your favorite food?",
            "What was the name of your first pet?",
            "What city were you born in?"
    );

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getDisplayType() {
        return "Secret question selection";
    }

    @Override
    public String getReferenceCategory() {
        return "Category";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "2FA step allows the selection of a secret question and answer";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        return List.of(
                new ProviderConfigProperty(
                        CONFIG_QUESTIONS,
                        "Secret Questions (Comma-separated)",
                        "Enter a comma-separated list of questions for the user to choose from.",
                        ProviderConfigProperty.STRING_TYPE,
                        StringUtils.join(PREDEFINED_QUESTIONS, CollectionUtils.COMMA)
                )
        );
    }

}
