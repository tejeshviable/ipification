package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.responseDTO.GenerateUrlResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class HandlerService {


    @Autowired
    NHASmsService nhaSmsService;

    @Autowired
    WorkflowRepo workflowRepo;

    @Autowired
    ChannelRepo channelRepo;

    @Autowired
    WhatsAppOtpService whatsAppOtpService;

    @Autowired
    GenerateUrlService generateUrlService;



    public String smsHandler(String txnId, Channel channel) throws JsonProcessingException {
        String name = channel.getName();
        String smsMobile = channel.getNumber();

        try {
            log.info("SMS handler");
            nhaSmsService.sendOtp(smsMobile);
            log.info("SMS OTP: {}",nhaSmsService.sendOtp(smsMobile));
            channel.setStatus("OTP SENT");
            channelRepo.save(channel);
        } catch (FeignException e) {
            if((e.status())!= 200)
            {

                channel.setStatus("OTP SENDING FAILED");
                channelRepo.save(channel);

                Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(txnId);

                if (!optionalWorkflow.isPresent()) {
                    log.error("Workflow not found for txnId: {}", txnId);
                    //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId");
                }

                Workflow workflow = optionalWorkflow.get();

                return txnId;
            }
        }

        return null;

    }

    public String whatsAppHandler(String txnId, Channel channel) throws JsonProcessingException {
        String name = channel.getName();
        String whatsAppMobile = channel.getNumber();

        try {
            log.info("whatsapp handler");
            whatsAppOtpService.generateWhatsappOtp(whatsAppMobile);
            channel.setStatus("OTP SENT");
            channelRepo.save(channel);
        } catch (FeignException e) {
            if((e.status())!= 200)
            {

                channel.setStatus("OTP SENDING FAILED");
                channelRepo.save(channel);

                Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(txnId);

                if (!optionalWorkflow.isPresent()) {
                    log.error("Workflow not found for txnId: {}", txnId);
                    //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId");
                }

                Workflow workflow = optionalWorkflow.get();

                return txnId;
                //fallBackService.fallBack(txnId);
            }
        }

        return null;

    }

    public String silentAuthHandler(String txnId, Channel channel) throws JsonProcessingException {
        String name = channel.getName();
        String silentAuthMobile = channel.getNumber();

        log.info("silent auth handler");
//        GenerateUrlResponseDTO response = generateUrlService.generateUrl(silentAuthMobile,txnId);
        String redirectionUrl = generateUrlService.generateUrl(silentAuthMobile,txnId).getRedirectionUrl();

        if(redirectionUrl != null)
        {
            channel.setStatus("URL GENERATED");
            channelRepo.save(channel);
            return redirectionUrl;
        }
        else{
                channel.setStatus("URL NOT GENERATED");
                channelRepo.save(channel);

                Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(txnId);

                if (!optionalWorkflow.isPresent()) {
                    log.error("Workflow not found for txnId: {}", txnId);
                    //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId");
                }

                Workflow workflow = optionalWorkflow.get();

                return txnId;
            }

        }






}
