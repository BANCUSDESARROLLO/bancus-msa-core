package com.core.referral.sp;

public class ReferralStoredProcedureException extends RuntimeException {

    private final Integer oracleErrorCode;

    public ReferralStoredProcedureException(String message, Integer oracleErrorCode, Throwable cause) {
        super(message, cause);
        this.oracleErrorCode = oracleErrorCode;
    }

    public Integer getOracleErrorCode() {
        return oracleErrorCode;
    }

    public boolean isOracleError(int code) {
        return oracleErrorCode != null && oracleErrorCode == code;
    }
}
