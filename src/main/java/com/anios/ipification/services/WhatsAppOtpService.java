package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.OtpRecord;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.OtpRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.enums.AuthenticationStatus;
import com.anios.ipification.feign.WhatsappFeign;
import com.anios.ipification.requestDTO.OtpRecordRequestDTO;

import com.anios.ipification.requestDTO.WhatsappMsgDTO;
import com.anios.ipification.responseDTO.OtpResponseDTO;
import com.anios.ipification.responseDTO.StatusResponseDTO;
import com.anios.ipification.util.IpificationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@Slf4j
public class WhatsAppOtpService {

    @Autowired
    WhatsappFeign whatsappFeign;

    @Autowired
    OtpRepo otpRepo;

    @Autowired
    ChannelRepo channelRepo;

    @Autowired
    SaveDataService saveDataService;

    @Autowired
    RedisService redisService;

    public ResponseEntity<?> generateWhatsappOtp(String mobileNo) throws JsonProcessingException {
        Optional<OtpRecord> existingRecord = otpRepo.findById(mobileNo);
        if (existingRecord.isPresent()) {
            log.info("Existing OTP record found, deleting it: " + existingRecord);
            otpRepo.deleteById(mobileNo);
        }
        OtpRecord record = new OtpRecord();
        record.setOtp(generateRandomOtp());
        record.setMobileNumber(mobileNo);
//        otpRepo.save(record);

        redisService.saveDataToRedis("whatsapp"+mobileNo, record.getOtp());
        WhatsappMsgDTO whatsappMsgDTO = new WhatsappMsgDTO();
        whatsappMsgDTO.setMessaging_product("whatsapp");
        whatsappMsgDTO.setTo(mobileNo);
        whatsappMsgDTO.setRecipient_type("individual");
        whatsappMsgDTO.setType("template");

        WhatsappMsgDTO.Template template = new WhatsappMsgDTO.Template();
        template.setName("aionos_otp");

        WhatsappMsgDTO.Language language = new WhatsappMsgDTO.Language();
        language.setCode("en_US");
        template.setLanguage(language);
        WhatsappMsgDTO.Component component = new WhatsappMsgDTO.Component();
        component.setType("body");

        WhatsappMsgDTO.Parameter parameter = new WhatsappMsgDTO.Parameter();
        parameter.setType("text");
        parameter.setText(record.getOtp());
        component.setParameters(Arrays.asList(parameter));

        WhatsappMsgDTO.Component component1 = new WhatsappMsgDTO.Component();
        component1.setType("button");
        component1.setIndex(0);
        component1.setSub_type("url");

        WhatsappMsgDTO.Parameter parameter1 = new WhatsappMsgDTO.Parameter();
        parameter1.setType("text");
        parameter1.setText("1234567");
        component1.setParameters(Arrays.asList(parameter1));

        template.setComponents(Arrays.asList(component, component1));
        whatsappMsgDTO.setTemplate(template);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(whatsappMsgDTO);
        log.info(jsonString);
//        Object response = whatsappFeign.generateWhatsappOtp(whatsappMsgDTO);
//        Map<String, Object> mappedObject = IpificationUtil.mapObject(response).get();
//        String txnId = mappedObject.get();
        return whatsappFeign.generateWhatsappOtp(whatsappMsgDTO);
    }



    public StatusResponseDTO verifyWhatsappOtp(OtpRecordRequestDTO otpRecordRequestDTO) {
        String mobileNumber = otpRecordRequestDTO.getMobileNumber();
        List<OtpRecord> otpRecords = otpRepo.findAll();
//        OtpRecord otpRecord = otpRepo.findByMobileNumber(mobileNumber);
//        if (otpRecord != null) {
//            String otp = otpRecord.getOtp();
        String otp = (String) redisService.getDataFromRedis("whatsapp"+mobileNumber);
            if (otp.equalsIgnoreCase(otpRecordRequestDTO.getOtp())) {
                otpRepo.deleteById(mobileNumber);

                StatusResponseDTO statusResponseDTO = StatusResponseDTO.builder()
                        .status("true")
                        .channel("whatsApp")
                        .txnId(otpRecordRequestDTO.getTxnId())
                        .otpTxnId("whatsapp"+mobileNumber)
                        .message("Authentication successful")
                        .build();

                saveDataService.saveToRedis(statusResponseDTO, otpRecordRequestDTO.getMobileNumber());
                return statusResponseDTO;
            } else {
                StatusResponseDTO response = StatusResponseDTO.builder()
                        .status("false")
                        .channel("whatsApp")
                        .txnId(otpRecordRequestDTO.getTxnId())
                        .otpTxnId("whatsapp"+mobileNumber)
                        .message("Authentication failed")
                        .build();
                saveDataService.saveToRedis(response, otpRecordRequestDTO.getMobileNumber());
                return response;

            }
//        }

//        return OtpResponseDTO.builder()
//                .status(false)
//                .errorMessage("Invalid OTP")
//                .build();
    }

    public static String generateRandomOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
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

    private void updateChannelStatusForAuthenticationFailure(String txnId, String channelName) {
        Optional<Channel> channels = Optional.ofNullable(channelRepo.findByTxnIdAndName(txnId, channelName));

        if (channels.isPresent()) {
            Channel channel = channels.get();
            channel.setStatus("AUTHENTICATION FAILED");
            channelRepo.save(channel);
            log.info("Updated channel status to AUTHENTICATED for txnId: {}, channel: {}", txnId, channelName);
        } else {
            log.warn("No channel found for txnId {} and name {}", txnId, channelName);
        }
    }

    private void updateChannelStatus(String txnId, String channelName,String status) {
        Optional<Channel> channels = Optional.ofNullable(channelRepo.findByTxnIdAndName(txnId, channelName));

        if (channels.isPresent()) {
            Channel channel = channels.get();
            channel.setStatus(status);
            channelRepo.save(channel);
            log.info("Updated channel status to AUTHENTICATED for txnId: {}, channel: {}", txnId, channelName);
        } else {
            log.warn("No channel found for txnId {} and name {}", txnId, channelName);
        }
    }
}
