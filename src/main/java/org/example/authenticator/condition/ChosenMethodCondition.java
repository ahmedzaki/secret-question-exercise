package org.example.authenticator.condition;

import org.example.authenticator.TwoFactorChoiceAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class ChosenMethodCondition implements ConditionalAuthenticator {


 @Override
    public boolean matchCondition(AuthenticationFlowContext authenticationFlowContext) {
     AuthenticatorConfigModel config = authenticationFlowContext.getAuthenticatorConfig();
     if (config == null || config.getConfig() == null) {
         return false;
     }

     String expectedMethod = config.getConfig().get(ChosenMethodConditionFactory.CONFIG_EXPECTED_VALUE);
     if (expectedMethod == null || expectedMethod.isEmpty()) {
         return false;
     }

     String chosenMethod = authenticationFlowContext.getAuthenticationSession().getAuthNote(TwoFactorChoiceAuthenticator.FLOW_NOTE_CHOICE_2FA_METHOD);

     if (chosenMethod == null) {
         return false;
     }

     return expectedMethod.equalsIgnoreCase(chosenMethod);
 }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {

    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }
}
