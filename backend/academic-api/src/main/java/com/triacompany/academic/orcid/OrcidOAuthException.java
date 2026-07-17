package com.triacompany.academic.orcid;

public class OrcidOAuthException extends IllegalArgumentException {

    private final String code;

    public OrcidOAuthException(String code, String message) {
        super(message);
        this.code = code;
    }

    public OrcidOAuthException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
