package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.enums.ChannelType;
import com.anios.ipification.responseDTO.GenerateUrlResponseDTO;
import com.anios.ipification.responseDTO.StatusResponseDTO;

import com.anios.ipification.util.IpificationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
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



    public StatusResponseDTO smsHandler(String txnId, Channel channel) throws JsonProcessingException {
        String smsMobile = channel.getNumber();
        Map<String, Object> mappedObject = new HashMap<>();
        try {
            ResponseEntity<?>  sendOtpResponse = nhaSmsService.sendOtp(smsMobile);
            Object responseBody =  sendOtpResponse.getBody();
            mappedObject = IpificationUtil.mapObject(responseBody);
            log.info("otpTxnId : " + mappedObject);

            log.info("SMS handler otp {}", responseBody);
            log.info("response " + sendOtpResponse);

            updateChannel(channel, "OTP SENT");
        } catch (FeignException e) {
            if (handleOtpFailure(txnId, channel, e)) {
                log.info("Failed to generate sms otp");
                return StatusResponseDTO.builder().txnId(txnId)
                        .errorMsg("Failed").status("false").errorCode("1001").build();

                //return new StatusResponseDTO(txnId, null, null, "Failed", "false", "1001");
            }
        }
        log.info("otpTxnId outside try-catch : " + mappedObject.get("txnId"));
        return StatusResponseDTO.builder().txnId(txnId).otpTxnId(mappedObject.get("txnId").toString()).channel(ChannelType.sms.name())
                .message((mappedObject.get("message").toString())).status("verification_pending").build();

    }

    public StatusResponseDTO whatsAppHandler(String txnId, Channel channel) throws JsonProcessingException {
        String whatsAppMobile = channel.getNumber();

        try {
            log.info("whatsapp handler");
            whatsAppOtpService.generateWhatsappOtp(whatsAppMobile);
            updateChannel(channel, "OTP SENT");
        } catch (FeignException e) {
            log.error("whatsapp feign error : {}", e.getMessage());
            if (handleOtpFailure(txnId, channel, e)) {
                return StatusResponseDTO.builder().txnId(txnId)
                        .errorMsg("Failed").status("false").errorCode("1001").build();
                //return new StatusResponseDTO(txnId, null, null, "Failed", "false", "1001");
            }
        } catch (Exception e) {
            log.info("Exception catched"+e.getMessage());
        }
        return StatusResponseDTO.builder().txnId(txnId).otpTxnId("whatsapp"+whatsAppMobile).channel(ChannelType.whatsApp.name())
                .message("WhatsApp Otp Sent").status("verification_pending").build();

        //return new StatusResponseDTO(txnId, "WhatsApp", "WhatsApp Otp Sent", null, "verification_pending", null);

    }



    private void updateChannel(Channel channel, String status) {
        channel.setStatus(status);
        channelRepo.save(channel);
    }

    private boolean handleOtpFailure(String txnId, Channel channel, FeignException e) {
        log.info("handleOtpFailure : {}, {}", txnId, channel.getName());
        if ((e.status()) != 200) {
            updateChannel(channel, "OTP SENDING FAILED");
            return true;
        }
        return false;
    }

    public GenerateUrlResponseDTO silentAuthHandler(String txnId, Channel channel) throws JsonProcessingException {
        String name = channel.getName();
        String silentAuthMobile = channel.getNumber();

        log.info("silent auth handler");
        GenerateUrlResponseDTO response = generateUrlService.generateUrl(silentAuthMobile,txnId);
//        String redirectionUrl = generateUrlService.generateUrl(silentAuthMobile,txnId).getRedirectionUrl();
        log.info("response in handler service"+ response);
        if(response != null)
        {
            log.info("response in handler service in if"+ response);
            channel.setStatus("URL GENERATED");
            channelRepo.save(channel);
            return response;
        }
        else{
            log.info("response in handler service in else"+ response);
            channel.setStatus("URL NOT GENERATED");
            channelRepo.save(channel);

            Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(txnId);

            if (!optionalWorkflow.isPresent()) {
                log.error("Workflow not found for txnId: {}", txnId);
                //return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId");
            }

            Workflow workflow = optionalWorkflow.get();

            return null;
        }

    }




}
