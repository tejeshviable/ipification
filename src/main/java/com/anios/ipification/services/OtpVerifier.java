package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.OtpRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.feign.SmsFeign;
import com.anios.ipification.requestDTO.TransactionDTO;
import com.anios.ipification.responseDTO.StatusResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OtpVerifier {

    @Autowired
    ChannelRepo channelRepo;
    @Autowired
    SmsFeign smsFeign;
    @Autowired
    FallBackService fallBackService;
    @Autowired
    SaveDataService saveDataService;

    public StatusResponseDTO validateOtp(TransactionDTO transactionDTO1, String txnId) throws JsonProcessingException {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setOtp(transactionDTO1.getOtp());
        transactionDTO.setMessage(transactionDTO1.getMessage());
        transactionDTO.setTxnId(transactionDTO1.getTxnId());
        transactionDTO.setMobileNumber(transactionDTO1.getMobileNumber());

        Object response =  smsFeign.validateOtp(transactionDTO).getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;

        try {
            Map<String, Object> responseMap = objectMapper.convertValue(response, Map.class);
            message = (String) responseMap.get("message");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Channel> channelList = channelRepo.findByTxnIdAndStatusOrderByPriority(txnId, "OTP SENT");
        Channel channel = channelList.get(0);


        if ("Incorrect Otp".equals(message)) {
            updateChannel(channel, "AUTHENTICATION FAILED");
            StatusResponseDTO statusFailureResponseDTO = StatusResponseDTO.builder()
                    .status("false")
                    .channel("sms")
                    .txnId(txnId)
                    .message("AUTHENTICATION FAILED")
                    .build();
            return statusFailureResponseDTO;
        }

        updateChannel(channel, "AUTHENTICATED");
        System.out.println("OTP validation passed or other response");

        StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder()
                .status("true")
                .channel("sms")
                .txnId(txnId)
                .message(message)
                .build();

        saveDataService.saveToRedis(statusResponseDTO, transactionDTO1.getMobileNumber());

        return statusResponseDTO;
    }

    private void updateChannel(Channel channel, String status) {
        channel.setStatus(status);
        channelRepo.save(channel);
    }

}
