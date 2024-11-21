package com.anios.ipification.feign;

import com.anios.ipification.requestDTO.TransactionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "InfobipClient", url = "${feign.sms.url}")
public interface SmsFeign {

    @PostMapping(value = "/{mobileNumber}")
    ResponseEntity<?> generateOtp(@PathVariable String mobileNumber);

    @PostMapping(value = "/verifyOtp")
    ResponseEntity<?> validateOtp(@RequestBody TransactionDTO transactionDTO);

}
