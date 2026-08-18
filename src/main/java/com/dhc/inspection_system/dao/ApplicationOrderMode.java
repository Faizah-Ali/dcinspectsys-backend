package com.dhc.inspection_system.dao;

/**
 * Named ordering modes for the application list query.
 * The DAO resolves each mode to a safe, fixed SQL ORDER BY clause.
 * No arbitrary SQL is accepted from callers.
 */
public enum ApplicationOrderMode {

    /**
     * Common freshness ordering for Admin, Officer, and Approver lists:
     * most recent e-Inspection EFILING_LOG event first.
     */
    LATEST_ACTION
}
