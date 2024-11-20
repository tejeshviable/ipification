package com.anios.ipification.services;

import com.anios.ipification.feign.SmsFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NHASmsService {

    @Autowired
    SmsFeign smsFeign;

    public Object sendOtp(String mobileNumber){
        return smsFeign.generateOtp(mobileNumber);

    }

}
