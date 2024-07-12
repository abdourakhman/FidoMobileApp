package com.daon.fido.sdk.sample.model;

public class SubmitOfflineOTPRequest {
    private String emailAddress;
    private String submittedAuthenticationCode;
    private String secureTransactionContent;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getSubmittedAuthenticationCode() {
        return submittedAuthenticationCode;
    }

    public void setSubmittedAuthenticationCode(String submittedAuthenticationCode) {
        this.submittedAuthenticationCode = submittedAuthenticationCode;
    }

    public String getSecureTransactionContent() {
        return secureTransactionContent;
    }

    public void setSecureTransactionContent(String secureTransactionContent) {
        this.secureTransactionContent = secureTransactionContent;
    }
}
