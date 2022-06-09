package com.creditville.notifications.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class FeeUtil {

    public BigDecimal calculatePaystackCharge(BigDecimal amt){
        log.info("amt: "+amt);

        final BigDecimal defaultAmt = new BigDecimal("2500");
        final BigDecimal decimalFee = new BigDecimal("0.015");
        final BigDecimal flatFee = new BigDecimal("100");
        final BigDecimal one = new BigDecimal("1.00");
        final BigDecimal maxFee = new BigDecimal("2000");
        final BigDecimal zeroPointZeroOne = new BigDecimal("0.01");
        BigDecimal finalFeeAmt;
        BigDecimal finalChargeFeeAmt;

        BigDecimal feeAmt;
        BigDecimal fee;
        BigDecimal dfAmt;

        if(amt.compareTo(defaultAmt) < 0){

            finalFeeAmt = decimalFee.multiply(amt).add(flatFee).setScale(2, RoundingMode.CEILING);
//            finalFeeAmt = decimalFee.multiply(amt).setScale(2, RoundingMode.CEILING);
            finalFeeAmt = amt.add(finalFeeAmt).setScale(2,RoundingMode.CEILING);
            System.out.println("finalFeeAmt1: "+finalFeeAmt);

        }else {
//            finalFeeAmt = amt.multiply(decimalFee).add(flatFee).setScale(2,RoundingMode.CEILING);
            fee = amt.add(flatFee);
            dfAmt = one.subtract(decimalFee);
            feeAmt = fee.divide(dfAmt,2,RoundingMode.CEILING);
            finalFeeAmt = feeAmt.add(zeroPointZeroOne).setScale(2,RoundingMode.CEILING);
            System.out.println("finalFeeAmt: " + finalFeeAmt);
        }
        finalChargeFeeAmt = finalFeeAmt.subtract(amt).setScale(2,RoundingMode.CEILING);
        log.info("finalChargeFeeAmt: "+finalChargeFeeAmt);

        if(finalChargeFeeAmt.compareTo(maxFee) > 0){
            return maxFee;
        }
        return finalChargeFeeAmt;
    }

    public BigDecimal calculatePaystackFee(BigDecimal loanAmt){
        final BigDecimal decimalFee = new BigDecimal("0.985");
        final BigDecimal hundred = new BigDecimal("100");
        final BigDecimal defaultAmt = new BigDecimal("2500");
        final BigDecimal maxFee = new BigDecimal("2000");

        final BigDecimal finalFeeAmt;
        final BigDecimal finalChargeAmt;

        if(loanAmt.compareTo(defaultAmt) < 0){

            finalFeeAmt = loanAmt.divide(decimalFee, 2,RoundingMode.CEILING);

        }else {
            finalFeeAmt = loanAmt.add(hundred).divide(decimalFee,2,RoundingMode.CEILING);
        }

        finalChargeAmt = finalFeeAmt.subtract(loanAmt).setScale(2,RoundingMode.CEILING);

        log.info("ENTRY CalculatePaystackCharge -> finalChargeAmt: {}",finalChargeAmt);

        if(finalChargeAmt.compareTo(maxFee) > 0){
            return maxFee;
        }
        return finalChargeAmt;
    }
}
