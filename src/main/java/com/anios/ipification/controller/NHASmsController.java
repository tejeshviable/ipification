package com.anios.ipification.controller;

import com.anios.ipification.requestDTO.SmsRequestDTO;
import com.anios.ipification.requestDTO.TransactionDTO;
import com.anios.ipification.services.NHASmsService;
import com.anios.ipification.services.OtpVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class NHASmsController {

    @Autowired
    NHASmsService nhaSmsService;

    @Autowired
    OtpVerifier otpVerifier;

    @PostMapping("/otp/send/{mobileNumber}")
    public ResponseEntity<?> sendSms(@PathVariable String mobileNumber) throws Exception
    {
        return new ResponseEntity<>(nhaSmsService.sendOtp(mobileNumber), HttpStatus.OK);
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<?> validateOtp(@RequestBody TransactionDTO transactionDTO,@RequestParam String txnId) throws JsonProcessingException {
        return new ResponseEntity<>(otpVerifier.validateOtp(transactionDTO,txnId), HttpStatus.OK);
    }
}
