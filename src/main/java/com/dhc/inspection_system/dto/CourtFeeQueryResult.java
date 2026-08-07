package com.dhc.inspection_system.dto;

/**
 * Result of SHCIL CERTRQ / ecfreq court-fee query (legacy getcourtFeeDetails).
 */
public final class CourtFeeQueryResult {

    private final boolean success;
    private final boolean skipped;
    private final String amount;
    private final boolean locked;
    private final String message;

    private CourtFeeQueryResult(
            boolean success,
            boolean skipped,
            String amount,
            boolean locked,
            String message
    ) {
        this.success = success;
        this.skipped = skipped;
        this.amount = amount;
        this.locked = locked;
        this.message = message;
    }

    public static CourtFeeQueryResult success(String amount, boolean locked, String message) {
        return new CourtFeeQueryResult(true, false, amount, locked, message);
    }

    public static CourtFeeQueryResult error(String message) {
        return new CourtFeeQueryResult(false, false, null, false, message);
    }

    public static CourtFeeQueryResult skip() {
        return new CourtFeeQueryResult(false, true, null, false, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public String getAmount() {
        return amount;
    }

    public boolean isLocked() {
        return locked;
    }

    public String getMessage() {
        return message;
    }
}
