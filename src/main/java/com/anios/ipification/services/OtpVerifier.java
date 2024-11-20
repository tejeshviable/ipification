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
    WorkflowRepo workflowRepo;

    @Autowired
    OtpRepo otpRepo;

    @Autowired
    SmsFeign smsFeign;

    @Autowired
    FallBackService fallBackService;

    @Autowired
    SaveDataService saveDataService;

    /*public Boolean verifyWhatsappOtp(OtpRecordDTO otpRecordDTO) throws JsonProcessingException {

        List<Channel> channelList = channelRepo.findByNameAndStatusNotAndNumber("whatsApp", "AUTHENTICATED",otpRecordDTO.getRequestId());

        Channel lastChannel = channelList.get(channelList.size() - 1);

        String lastTxnId = lastChannel.getTxnId();
        //RedisDto redisDto = (RedisDto) redisService.getDataFromRedis(lastTxnId);

        Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(lastTxnId);

        List<Channel> channelListPending = new ArrayList<>();
        channelListPending = channelRepo.findByTxnIdAndStatusOrderByPriority(lastTxnId, "PENDING");

        String requestId = otpRecordDTO.getRequestId();
        Optional<OtpRecord> data = otpRepo.findById(requestId); //db
        if (data.isPresent()) {// present
            OtpRecord otpRecord = data.get(); // get
            String otp = otpRecord.getOtp();  //otp
            if (otp.equalsIgnoreCase(otpRecordDTO.getOtp())) {

                *//*redisDto.setStatus("true");
                redisDto.setMessage("Authentication success");
                redisDto.setChannel("sms");
                redisService.saveDataToRedis(lastTxnId,redisDto);
*//*


                if (!optionalWorkflow.isPresent()) {
                    log.error("Workflow not found for txnId: {}", lastTxnId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId").hasBody();
                }

                Workflow workflow = optionalWorkflow.get();

                workflow.setFinalChannel("whatApp");
                workflow.setStatus(true);
                workflowRepo.save(workflow);

                updateChannelStatusForAuthenticated(lastTxnId, "whatsApp");

                otpRepo.deleteById(requestId);

                return true;
            } else {
                fallBackService.fallBack(requestId);

            }

        }
        return true;
    }

    private void updateChannelStatusForAuthenticated(String txnId, String channelName) {
        Optional<Channel> channels = Optional.ofNullable(channelRepo.findByTxnIdAndName(txnId, channelName));

        if (channels.isPresent()) {
            Channel channel = channels.get();
            channel.setStatus("AUTHENTICATED");
            channelRepo.save(channel);
            log.info("Updated channel status to AUTHENTICATED for txnId: {}, channel: {}", txnId, channelName);
        } else {
            log.warn("No channel found for txnId {} and name {}", txnId, channelName);
        }
    }
*/


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
            return fallBackService.fallBack(txnId);
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
