package org.example.authenticator.credential.models.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SecretQuestionSecretData {

    private final String answer;

    @JsonCreator
    public SecretQuestionSecretData(@JsonProperty("answer") String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

}
