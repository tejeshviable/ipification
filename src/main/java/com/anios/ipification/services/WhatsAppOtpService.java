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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;


@Service
@Slf4j
public class WhatsAppOtpService {

    @Autowired
    WhatsappFeign whatsappFeign;

    @Autowired
    OtpRepo otpRepo;

    @Autowired
    ChannelRepo channelRepo;

    public ResponseEntity<?> generateWhatsappOtp(String mobileNo) throws JsonProcessingException {
        Optional<OtpRecord> existingRecord = otpRepo.findById(mobileNo);
        if (existingRecord.isPresent()) {
            log.info("Existing OTP record found, deleting it: " + existingRecord);
            otpRepo.deleteById(mobileNo);
        }
        OtpRecord record = new OtpRecord();
        record.setOtp(generateRandomOtp());
        record.setMobileNumber(mobileNo);
        otpRepo.save(record);
        WhatsappMsgDTO whatsappMsgDTO = new WhatsappMsgDTO();
        whatsappMsgDTO.setMessaging_product("whatsapp");
        whatsappMsgDTO.setTo(mobileNo);
        whatsappMsgDTO.setRecipient_type("individual");
        whatsappMsgDTO.setType("template");

        WhatsappMsgDTO.Template template = new WhatsappMsgDTO.Template();
        template.setName("aionos_dxe_auth_4");

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
        return whatsappFeign.generateWhatsappOtp(whatsappMsgDTO);
    }



    public OtpResponseDTO verifyWhatsappOtp(OtpRecordRequestDTO otpRecordRequestDTO) {
        String mobileNumber = otpRecordRequestDTO.getMobileNumber();
        List<OtpRecord> otpRecords = otpRepo.findAll();
        OtpRecord otpRecord = otpRepo.findByMobileNumber(mobileNumber);
        if (otpRecord != null) {
            String otp = otpRecord.getOtp();
            if (otp.equalsIgnoreCase(otpRecordRequestDTO.getOtp())) {
                otpRepo.deleteById(mobileNumber);
                return OtpResponseDTO.builder()
                        .mobileNumber(otpRecord.getMobileNumber())
                        .status(true)
                        .build();
            } else {
                return OtpResponseDTO.builder()
                        .mobileNumber(otpRecord.getMobileNumber())
                        .status(false)
                        .errorMessage("Invalid OTP")
                        .build();

            }
        }

        return OtpResponseDTO.builder()
                .status(false)
                .errorMessage("Invalid OTP")
                .build();
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
