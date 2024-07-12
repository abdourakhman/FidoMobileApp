package com.daon.fido.sdk.sample.model;

public class SubmitFailedAttemptResponse {
    private String fidoAuthenticationResponse;
    private String fidoAuthenticationRequest;
    private String authenticationRequestId;

    public SubmitFailedAttemptResponse() {

    }

    public String getFidoAuthenticationResponse() {
        return fidoAuthenticationResponse;
    }

    public void setFidoAuthenticationResponse(String fidoAuthenticationResponse) {
        this.fidoAuthenticationResponse = fidoAuthenticationResponse;
    }

    public String getFidoAuthenticationRequest() {
        return fidoAuthenticationRequest;
    }

    public void setFidoAuthenticationRequest(String fidoAuthenticationRequest) {
        this.fidoAuthenticationRequest = fidoAuthenticationRequest;
    }

    public String getAuthenticationRequestId() {
        return authenticationRequestId;
    }

    public void setAuthenticationRequestId(String authenticationRequestId) {
        this.authenticationRequestId = authenticationRequestId;
    }
}
