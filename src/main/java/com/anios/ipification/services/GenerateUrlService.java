package com.anios.ipification.services;

import com.anios.ipification.enums.AuthenticationStatus;
import com.anios.ipification.responseDTO.GenerateUrlResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GenerateUrlService {

    @Value("${url.client-callback-uri}")
    String clientCallbackUri;

    @Value("${url.callback}")
    String url1;

    @Value("${url.clientId}")
    String clientId;


    public GenerateUrlResponseDTO generateUrl(String mobileNumber, String txnId) {

        log.info("mobile number {} , txnId {} : ", mobileNumber, txnId);

        GenerateUrlResponseDTO generateUrlResponseDTO = new GenerateUrlResponseDTO();


        String newUrl = url1 + clientCallbackUri + "&client_id=" + clientId + "&scope=openid ip:phone_verify&state=" + txnId + "&login_hint=" + mobileNumber;
//        String newUrl=null;
        log.info("url generated : {} ",newUrl);
        generateUrlResponseDTO.setRequestId(txnId);
        generateUrlResponseDTO.setRedirectionUrl(newUrl);

        return generateUrlResponseDTO;
    }
}
