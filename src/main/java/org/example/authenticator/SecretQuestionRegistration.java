package org.example.authenticator;

import jakarta.ws.rs.core.Response;
import org.example.authenticator.credential.models.SecretQuestionCredentialModel;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SecretQuestionRegistration implements Authenticator {
    private static final Logger logger = Logger.getLogger(SecretQuestionAuthenticator.class);

    private static final String MISSING_SECRET_QUESTION_OR_ANSWER = "missingSecretQuestionOrAnswer";
    public static final String FORM_SECRET_QUESTION_ID = "secret_question";
    public static final String FORM_SECRET_QUESTION_ANSWER_ID = "secret_question_answer";
    public static final String FORM_QUESTIONS_ATTRIBUTE = "questions";
    public static final String FORM_REGISTRATION_FILE_NAME = "secret-question-registration.ftl";

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        Response challenge = createForm(authenticationFlowContext, null);
        authenticationFlowContext.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        String question = (authenticationFlowContext.getHttpRequest().getDecodedFormParameters()
                .getFirst(FORM_SECRET_QUESTION_ID));
        String answer = (authenticationFlowContext.getHttpRequest().getDecodedFormParameters()
                .getFirst(FORM_SECRET_QUESTION_ANSWER_ID));

        if (Objects.isNull(question) || Objects.isNull(answer) || answer.trim().isEmpty()) {
            logger.warn("Illegal argument");

            Response challenge = createForm(authenticationFlowContext, MISSING_SECRET_QUESTION_OR_ANSWER);
            authenticationFlowContext.challenge(challenge);
            return;
        }

        if (answer.trim().length() < 2) {
            Response challenge = createForm(authenticationFlowContext, "answer is too short");
            authenticationFlowContext.challenge(challenge);
            return;
        }

        if (answer.trim().length() > 100) {
            Response challenge = createForm(authenticationFlowContext, "answer is too long");
            authenticationFlowContext.challenge(challenge);
            return;
        }

        answer = sanitize(answer);
        UserModel user = authenticationFlowContext.getUser();
        user.credentialManager().createStoredCredential(
                SecretQuestionCredentialModel.createSecretQuestion(question, answer));

        logger.info("Saving secret question");
        authenticationFlowContext.success();

    }

    private Response createForm(AuthenticationFlowContext context, String error) {
        LoginFormsProvider form = context.form();
        if (error != null) {
            form.setError(error);
        }

        return form
                .setAttribute(FORM_QUESTIONS_ATTRIBUTE,
                        getQuestionsFromConfig(context))
                .createForm(FORM_REGISTRATION_FILE_NAME);
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

    private List<String> getQuestionsFromConfig(AuthenticationFlowContext context) {

        Map<String, String> configMap = context.getAuthenticatorConfig().getConfig();
        String questionsConfigString = configMap.get(SecretQuestionRegistrationFactory.CONFIG_QUESTIONS);
        if (Objects.isNull(questionsConfigString) || questionsConfigString.trim().isEmpty()) {
            logger.info("missing secret questions config, defaulting to predefined list");
            return SecretQuestionRegistrationFactory.PREDEFINED_QUESTIONS;
        }

        String[] questions = questionsConfigString.trim().split(",");

        if (questions.length < 2) {
            logger.warn("less than 2 secret questions configured, defaulting to predefined list");
            return SecretQuestionRegistrationFactory.PREDEFINED_QUESTIONS;
        }

        for (String question : questions) {
            if (question.trim().length() > 200) {
                logger.warn("Configured secret questions too long, defaulting to predefined list");
                return SecretQuestionRegistrationFactory.PREDEFINED_QUESTIONS;
            }
        }

        return Arrays.stream(questions)
                .map(String::trim)
                .toList();
    }

    private String sanitize(String input) {
        return input.replaceAll("[<>\"']", "");
    }
}
