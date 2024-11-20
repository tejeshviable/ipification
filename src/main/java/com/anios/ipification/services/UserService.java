package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.enums.AuthenticationType;
import com.anios.ipification.enums.ChannelType;
import com.anios.ipification.feign.IpificationFeign;
import com.anios.ipification.requestDTO.GenerateUrlRequestDTO;
import com.anios.ipification.requestDTO.RedisDto;
import com.anios.ipification.responseDTO.GenerateUrlResponseDTO;
import com.anios.ipification.responseDTO.StatusResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

@Service
@Slf4j
public class UserService {

    @Autowired
    WorkflowRepo workflowRepo;

    @Autowired
    RedisService redisService;

    @Value("${url.callback}")
    String url1;

    @Value("${url.clientId}")
    String clientId;

    @Value("${url.client-callback-uri}")
    String clientCallbackUri;

    @Autowired
    ChannelRepo channelRepo;

    @Autowired
    SaveDataService saveDataService;

    @Value("${url.clientSecret}")
    String clientSecret;

    @Autowired
    IpificationFeign ipificationFeign;

    @Autowired
    HandlerService handlerService;

    @Autowired
    FallBackService fallBackService;


    public Object authenticateUser(String clientId, GenerateUrlRequestDTO generateUrlRequestDTO) throws JsonProcessingException {

        String urlMobile = null, smsMobile = null, whatsAppMobile = null;

        List<Channel> channels = new ArrayList<>();

        String requestId = UUID.randomUUID().toString();

        Workflow workflow = saveDataService.setWorkflowInitial(generateUrlRequestDTO, requestId);

        int priority = 1;

        String authenticationType = (String) redisService.getDataFromRedis(clientId);
        log.info("Authentication Type from cache: {} ", authenticationType);
        if(AuthenticationType.IP.name().equalsIgnoreCase(authenticationType)) {
            return authenticationViaIpification(generateUrlRequestDTO, urlMobile, smsMobile, whatsAppMobile, channels, requestId, workflow, priority);
        } else if(AuthenticationType.HE.name().equalsIgnoreCase(authenticationType)) {
            //Todo: Need to handle
            log.info("Authentication via Header enrichment to be done");
        }
        return null;
    }

    private Object authenticationViaIpification(GenerateUrlRequestDTO generateUrlRequestDTO, String urlMobile, String smsMobile, String whatsAppMobile, List<Channel> channels, String requestId, Workflow workflow, int priority) throws JsonProcessingException {
        if (generateUrlRequestDTO.getWorkflow() != null && !generateUrlRequestDTO.getWorkflow().isEmpty()) {
            for (GenerateUrlRequestDTO.WorkflowItem item : generateUrlRequestDTO.getWorkflow()) {

                Channel channel = saveDataService.setChannels(item, workflow, requestId, priority);

                log.info("Processing item with channel: {}", item.getChannel());
                if (ChannelType.sms.name().equals(item.getChannel())) {
                    smsMobile = item.getMobileNumberTo();
                } else if (ChannelType.silent_auth.name().equals(item.getChannel())) {
                    urlMobile = item.getMobileNumberTo();
                } else if (ChannelType.whatsApp.name().equals(item.getChannel())) {
                    whatsAppMobile = item.getMobileNumberTo();

                }
                channels.add(channel);
                priority++;
            }
            workflow.setChannels(channels);
            workflowRepo.save(workflow);
        }

        List<Channel> channelList = channelRepo.findByTxnIdAndStatusOrderByPriority(requestId, "PENDING");
        Channel channel = channelList.get(0);

        StatusResponseDTO response = null;
        StatusResponseDTO fallbackResponse = null;

        if (ChannelType.silent_auth.name().equals(channel.getName())) {
            saveDataService.saveMobileInRedis(requestId, urlMobile);
            GenerateUrlResponseDTO generateUrlResponseDTO = handlerService.silentAuthHandler(requestId, channel);
            if (generateUrlResponseDTO != null && requestId.equalsIgnoreCase(generateUrlResponseDTO.getRequestId())) {
                log.info("url generation success: " + redisService.getDataFromRedis(requestId));
                return generateUrlResponseDTO;
            } else {
                fallBackService.fallBack(requestId);
            }
        } else if (ChannelType.sms.name().equals(channel.getName())) {

            saveDataService.saveMobileInRedis(requestId, smsMobile);
            response = handlerService.smsHandler(requestId, channel);
            fallbackResponse = failedCaseHandler(response, requestId);

        } else if (ChannelType.whatsApp.name().equals(channel.getName())) {

            saveDataService.saveMobileInRedis(requestId, whatsAppMobile);
            response = handlerService.whatsAppHandler(requestId, channel);
            fallbackResponse = failedCaseHandler(response, requestId);

        }
        StatusResponseDTO finalResponse = fallbackResponse == null ? response : fallbackResponse;
        saveDataService.saveToRedis(finalResponse, "");
        return finalResponse;
    }


    private StatusResponseDTO failedCaseHandler(StatusResponseDTO response, String requestId) throws JsonProcessingException {
        if (response != null && "Failed".equalsIgnoreCase(response.getErrorMsg())) {
            return fallBackService.fallBack(requestId);
        }
        return null;
    }

    public GenerateUrlResponseDTO generateUrl(String mobileNumber, String txnId) {

        log.info("mobile number {} , txnId {} : ", mobileNumber, txnId);

        GenerateUrlResponseDTO generateUrlResponseDTO = new GenerateUrlResponseDTO();

        String newUrl = url1 + clientCallbackUri + "&client_id=" + clientId + "&scope=openid ip:phone_verify&state=" + txnId + "&login_hint=" + mobileNumber;
        generateUrlResponseDTO.setRequestId(txnId);
        generateUrlResponseDTO.setRedirectionUrl(newUrl);

        log.info("redis data in generate url : " + redisService.getDataFromRedis(txnId));

        return generateUrlResponseDTO;
    }

    private void updateChannelStatus(List<Channel> channels, String channelName, String status) {
        for (Channel channel : channels) {
            if (channel.getName().equals(channelName)) {
                channel.setStatus(status);
                channelRepo.save(channel);
            }
        }

    }

    public Object verificationOnCallback(String code, String requestId, String error, String errorDescription) throws JsonProcessingException {

        String authenticationType = (String) redisService.getDataFromRedis(clientId);
        log.info("Authentication Type from cache: {} ", authenticationType);
        if(AuthenticationType.IP.name().equalsIgnoreCase(authenticationType)) {
            return verificationOnCallbackViaIpification(code, requestId, error, errorDescription);        }
        else if(AuthenticationType.HE.name().equalsIgnoreCase(authenticationType)) {
            //Todo: Need to handle
            log.info("verification via Header enrichment to be done");
        }
        return null;
    }

    private Object verificationOnCallbackViaIpification(String code, String requestId, String error, String errorDescription) throws JsonProcessingException {
        if (checkError(requestId, error, errorDescription)) {
//            return StatusResponseDTO.builder().errorMsg(errorDescription).status("false").build();
            return fallBackService.fallBack(requestId);
        }

        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();

        addValues(code, values);

        ResponseEntity<?> tokens = ipificationFeign.fetchToken(values);

        if (tokens.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = (Map<String, Object>) tokens.getBody();

            if (body != null && body.containsKey("access_token")) {
                String accessToken = (String) body.get("access_token");
                String bearerToken = "Bearer " + accessToken;


                ResponseEntity<?> userInfoResponse = ipificationFeign.userDetails(bearerToken);
                if (userInfoResponse.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> userBody = (Map<String, Object>) userInfoResponse.getBody();
                    if (userBody != null && userBody.containsKey("phone_number_verified")) {
                        System.out.println("Tejeshvi : {}" + userBody);
                        String status = (String) userBody.get("phone_number_verified");
                        String login_hint = (String) userBody.get("login_hint");

                        Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(requestId);

                        List<Channel> channelList1 = channelRepo.findByTxnIdAndStatusOrderByPriority(requestId, "URL GENERATED");
                        Channel channel1 = channelList1.get(0);

                        if (!optionalWorkflow.isPresent() || channelList1.isEmpty() ) {
                            log.error("Workflow not found for txnId: {}", requestId);
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId");
                        }

                        Workflow workflow = optionalWorkflow.get();

                        if("true".equalsIgnoreCase(status)){
                            saveDataService.saveRedisData(status,"Authenticated", "", login_hint, requestId,"silent_auth");
                            saveDataService.setfinalWorkflow(workflow, "silent_auth",true);
                            updateChannelStatusForAuthenticated(requestId, "silent_auth");
                            log.info("redis-data-silent_auth-true : {}", redisService.getDataFromRedis(requestId));
                            return redisService.getDataFromRedis(requestId);
                        }

                        if("false".equalsIgnoreCase(status)){
                            channel1.setStatus("AUTHENTICATION FAILED");
                            channelRepo.save(channel1);
                            return fallBackService.fallBack(requestId);
                        }
                    }
                }
            }
        }

        return StatusResponseDTO.builder().txnId("")
                .errorMsg("Code should not reach here").status("false").errorCode("1002").build();
    }

    private boolean checkError(String requestId, String error, String errorDescription) {
        if(error != null && !"".equals(error)) {
            RedisDto redisDto = (RedisDto) redisService.getDataFromRedis(requestId);
            redisDto.setStatus("false");
            redisDto.setErrorMessage(errorDescription);
            redisService.saveDataToRedis(requestId, redisDto);
            return true;
        }
        return false;
    }

    private void addValues(String code, MultiValueMap<String, String> values) {
        values.add("code", code);
        values.add("grant_type", "authorization_code");
        values.add("redirect_uri", clientCallbackUri);
        values.add("client_id", clientId);
        values.add("client_secret", clientSecret);
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

    public RedisDto getUserStatus(String txnId) {
        RedisDto redisDto;
        if (redisService.getDataFromRedis(txnId) == null) {
            redisDto = new RedisDto();
            redisDto.setStatus("false");
            return redisDto;
        }
        return (RedisDto) redisService.getDataFromRedis(txnId);

    }
}
