package org.example.authenticator.credential.models;


import org.example.authenticator.credential.models.dto.SecretQuestionCredentialData;
import org.example.authenticator.credential.models.dto.SecretQuestionSecretData;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;

public class SecretQuestionCredentialModel extends CredentialModel {
    public static final String TYPE = "SECRET_QUESTION";

    private final SecretQuestionCredentialData credentialData;
    private final SecretQuestionSecretData secretData;

    private SecretQuestionCredentialModel(SecretQuestionCredentialData credentialData, SecretQuestionSecretData secretData) {
        this.credentialData = credentialData;
        this.secretData = secretData;
    }

    private SecretQuestionCredentialModel(String question, String answer) {
        credentialData = new SecretQuestionCredentialData(question);
        secretData = new SecretQuestionSecretData(answer);
    }

    public static SecretQuestionCredentialModel createSecretQuestion(String question, String answer) {
        String normalizedInput = answer.trim().toLowerCase();

        SecretQuestionCredentialModel credentialModel = new SecretQuestionCredentialModel(question, normalizedInput);
        credentialModel.fillCredentialModelFields();
        return credentialModel;
    }

    private void fillCredentialModelFields(){
        try {
            setCredentialData(JsonSerialization.writeValueAsString(credentialData));
            setSecretData(JsonSerialization.writeValueAsString(secretData));
            setType(TYPE);
            setCreatedDate(Time.currentTimeMillis());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecretQuestionCredentialModel createFromCredentialModel(CredentialModel credentialModel){
        try {
            SecretQuestionCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), SecretQuestionCredentialData.class);
            SecretQuestionSecretData secretData = JsonSerialization.readValue(credentialModel.getSecretData(), SecretQuestionSecretData.class);

            SecretQuestionCredentialModel secretQuestionCredentialModel = new SecretQuestionCredentialModel(credentialData, secretData);
            secretQuestionCredentialModel.setUserLabel(credentialModel.getUserLabel());
            secretQuestionCredentialModel.setCreatedDate(credentialModel.getCreatedDate());
            secretQuestionCredentialModel.setType(TYPE);
            secretQuestionCredentialModel.setId(credentialModel.getId());
            secretQuestionCredentialModel.setSecretData(credentialModel.getSecretData());
            secretQuestionCredentialModel.setCredentialData(credentialModel.getCredentialData());

            return secretQuestionCredentialModel;

        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public SecretQuestionCredentialData getSecretQuestionCredentialData() {
        return credentialData;
    }

    public SecretQuestionSecretData getSecretQuestionSecretData() {
        return secretData;
    }
}
