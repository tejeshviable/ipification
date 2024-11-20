package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.WorkflowRepo;

import com.anios.ipification.enums.ChannelType;
import com.anios.ipification.responseDTO.GenerateUrlResponseDTO;
import com.anios.ipification.responseDTO.StatusResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FallBackService {

    @Autowired
    ChannelRepo channelRepo;

    @Autowired
    SaveDataService saveDataService;

    @Autowired
    HandlerService handlerService;

    @Autowired
    WorkflowRepo workflowRepo;

    public StatusResponseDTO fallBack(String txnId/*, String mobileNum*/) throws JsonProcessingException {

        List<Channel> channelList = channelRepo.findByTxnIdAndStatusOrderByPriority(txnId, "PENDING");
        if (channelList.isEmpty()) {
            log.info("channelList empty");
            String message = "All channels failed to generate URL or send message.";
            StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder().txnId(txnId)
                    .errorMsg(message).status("false").errorCode("1001").build();
            //StatusResponseDTO statusResponseDTO = new StatusResponseDTO(txnId, null, null, message, "false", "1001");
            saveDataService.saveToRedis(statusResponseDTO, "");
            return statusResponseDTO;
        }

        Channel nextChannel = channelList.get(0);
        log.info("Next - {}", nextChannel.getName());


        if ("silent_auth".equalsIgnoreCase(channelList.get(0).getName()))
        {
            log.info("silent_auth fallback");
            GenerateUrlResponseDTO response = handlerService.silentAuthHandler(txnId,channelList.get(0));
            if (response != null || response.getRequestId().equalsIgnoreCase(txnId)) fallBack(txnId);

//            failedCaseHandler(txnId, response);
            StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder().txnId(txnId).otpTxnId(txnId).channel(ChannelType.silent_auth.name())
                    .message("Verified via silent auth").status("true").build();
            //StatusResponseDTO statusResponseDTO = new StatusResponseDTO(txnId, "Silent Auth", "Verified via silent auth", null, "true", null);
            saveDataService.saveToRedis(statusResponseDTO, "");
            return statusResponseDTO;
        }

        else if ("whatsapp".equalsIgnoreCase(channelList.get(0).getName()))
        {

            StatusResponseDTO response = handlerService.whatsAppHandler(txnId,channelList.get(0));
            failedCaseHandler(txnId, response.getErrorMsg());
            log.info("fallback whatsapp : " +response);
            //StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder().txnId(txnId).channel(ChannelType.whatsApp.name())
            //        .message("WhatsApp Otp Sent").status("verification_pending").build();
            //StatusResponseDTO statusResponseDTO = new StatusResponseDTO(txnId, "WhatsApp", "WhatsApp Otp Sent", null, "true", null);
            saveDataService.saveToRedis(response, "");
            return response;
        }

        else if ("sms".equalsIgnoreCase(channelList.get(0).getName()))
        {

            StatusResponseDTO response = handlerService.smsHandler(txnId,channelList.get(0));
            failedCaseHandler(txnId, response.getErrorMsg());
            log.info("fallback sms : "+response);

            //StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder().txnId(txnId).otpTxnId(response.getOtpTxnId()).channel(ChannelType.sms.name())
            //        .message(response.getMessage()).status("verification_pending").build();
            //StatusResponseDTO statusResponseDTO = new StatusResponseDTO(txnId, "SMS", "SMS Otp Sent", null, "true", null);
            saveDataService.saveToRedis(response, "");
            return response;

        }

        return StatusResponseDTO.builder().txnId(txnId)
                .errorMsg("Code should not have reached here").status("false").errorCode("1002").build();

        //return new StatusResponseDTO(txnId, null, null, "Code should not have reached here", "false", "1002");
    }

    private void failedCaseHandler(String txnId, String response) throws JsonProcessingException {
        log.info("failedCaseHandler txnId : {}, response : {}  ", txnId, response);
        if (response != null && "Failed".equalsIgnoreCase(response)) {
            fallBack(txnId);
        }
    }

}
