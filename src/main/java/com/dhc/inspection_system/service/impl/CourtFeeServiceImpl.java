package com.dhc.inspection_system.service.impl;

import com.dhc.inspection_system.dto.CourtFeeQueryResult;
import com.dhc.inspection_system.service.CourtFeeService;
import com.dhc.inspection_system.soap.CourtFeeSoapClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourtFeeServiceImpl implements CourtFeeService {

    private static final Logger log = LoggerFactory.getLogger(CourtFeeServiceImpl.class);

    @Autowired
    private CourtFeeSoapClient courtFeeSoapClient;

    @Override
    public String lockCourtFee(int diaryNo, int diaryYr, String ecourtFeeId) {
        if (ecourtFeeId == null || ecourtFeeId.isBlank()) {
            log.info(
                    "Skipping SHCIL court-fee lock for diaryNo={}, diaryYr={}: no ecourt_fee_id",
                    diaryNo,
                    diaryYr
            );
            return "SUCCESS";
        }

        try {
            String rpStatus = courtFeeSoapClient.lockCourtFee(
                    String.valueOf(diaryNo),
                    String.valueOf(diaryYr),
                    ecourtFeeId
            );
            log.info(
                    "SHCIL court-fee lock RPSTATUS={} for diaryNo={}, diaryYr={}",
                    rpStatus,
                    diaryNo,
                    diaryYr
            );
            return rpStatus;
        } catch (Exception ex) {
            log.error(
                    "ERROR LOCK court fee for diaryNo={}, diaryYr={}: {}",
                    diaryNo,
                    diaryYr,
                    ex.toString(),
                    ex
            );
            throw new RuntimeException("Court fee lock failed", ex);
        }
    }

    @Override
    public CourtFeeQueryResult queryCourtFee(String ecourtFeeId) {
        return courtFeeSoapClient.queryCourtFee(ecourtFeeId);
    }
}
