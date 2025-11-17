package org.example.authenticator.condition;

import org.keycloak.Config;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class ChosenMethodConditionFactory implements ConditionalAuthenticatorFactory {

    public static final String PROVIDER_ID = "chosen-second-factor-condition";
    public static final String CONFIG_EXPECTED_VALUE = "expectedSecondFactorValue";

    private static final ChosenMethodCondition SINGLETON = new ChosenMethodCondition();

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public ConditionalAuthenticator getSingleton() {
        return SINGLETON;
    }

    @Override
    public String getDisplayType() {
        return "Chosen 2FA method Condition";
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
        return "Chosen 2FA method condition, returns true when chosen 2FA method matches configured method value";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
                new ProviderConfigProperty(
                        ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE,
                        "Expected 2FA Method",
                        "Enter the value (e.g., 'otp', 'webauthn', or 'recovery') that must match the user's choice.",
                        ProviderConfigProperty.STRING_TYPE,
                        "otp"
                )
        );
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
    public String getId() {
        return PROVIDER_ID;
    }
}
