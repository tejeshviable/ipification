package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Object fallBack(String txnId/*, String mobileNum*/) throws JsonProcessingException {
        List<Channel> channelList = channelRepo.findByTxnIdAndStatusOrderByPriority(txnId, "PENDING");
        Channel nextCannel = channelList.get(0);
        log.info("Next - {}", nextCannel.getName());
        if (channelList.isEmpty()) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("All channels failed to generate URL or send message.");

//        Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(txnId);

        /*if (!optionalWorkflow.isPresent()) {
            log.error("Workflow not found for txnId: {}", txnId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId");
        }

        Workflow workflow = optionalWorkflow.get();
*/
        if("silent_auth".equalsIgnoreCase(channelList.get(0).getName()))
        {
            log.info("silent_auth fallback");
            String response = handlerService.silentAuthHandler(txnId,channelList.get(0));
            if (response != null || response.equalsIgnoreCase(txnId)) fallBack(txnId);

        }
        else if ("whatsapp".equalsIgnoreCase(channelList.get(0).getName()))
        {
            log.info("whatsApp fallback");
            String response = handlerService.whatsAppHandler(txnId,channelList.get(0));
            if (response != null && response.equalsIgnoreCase(txnId)) fallBack(txnId);

        }
        else if ("sms".equalsIgnoreCase(channelList.get(0).getName()))
        {
            log.info("sms fallback");
            String response = handlerService.smsHandler(txnId,channelList.get(0));
            if (response != null && response.equalsIgnoreCase(txnId)) fallBack(txnId);
        }
        else{
            String msg= "User not validated via silent auth & other workflows";
            saveDataService.saveRedisData("AUTHENTICATION_FAILED",msg,"",txnId,"");
        }

        return channelList;
    }

}
