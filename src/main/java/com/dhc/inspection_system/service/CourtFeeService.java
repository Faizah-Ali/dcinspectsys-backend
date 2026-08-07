package com.dhc.inspection_system.service;

import com.dhc.inspection_system.dto.CourtFeeQueryResult;

public interface CourtFeeService {

    /**
     * Legacy AcceptAppl / PaymentGatewayAction.lockcourtFee behaviour.
     * Blank receipt → "SUCCESS" without SOAP call.
     *
     * @return RPSTATUS (e.g. SUCCESS) or null on parse failure
     */
    String lockCourtFee(int diaryNo, int diaryYr, String ecourtFeeId);

    /**
     * Legacy getcourtFeeDetails / CERTRQ. Never throws.
     */
    CourtFeeQueryResult queryCourtFee(String ecourtFeeId);
}
