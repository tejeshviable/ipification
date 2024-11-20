package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.enums.ChannelType;
import com.anios.ipification.feign.IpificationFeign;
import com.anios.ipification.requestDTO.GenerateUrlRequestDTO;
import com.anios.ipification.requestDTO.RedisDto;
import com.anios.ipification.responseDTO.GenerateUrlResponseDTO;
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
    NHASmsService nhaSmsService;

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


    public Object authenticateUser(GenerateUrlRequestDTO generateUrlRequestDTO) throws JsonProcessingException {

        String urlMobile = null, smsMobile = null, whatsAppMobile = null;

        List<Channel> channels = new ArrayList<>();

        String requestId = UUID.randomUUID().toString();

        Workflow workflow = saveDataService.setWorkflowInitial(generateUrlRequestDTO, requestId);

        int priority = 1;

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


        if (ChannelType.silent_auth.name().equals(channel.getName())) {

            saveDataService.saveMobileInRedis(requestId, urlMobile);

            GenerateUrlResponseDTO response = handlerService.silentAuthHandler(requestId, channel);

            if (response != null && requestId.equalsIgnoreCase(response.getRequestId())) {
                log.info("url generation success: " + redisService.getDataFromRedis(requestId));
                return response;
            } else {
                fallBackService.fallBack(requestId);
            }


                /*Object generateUrlResponse = generateUrl(urlMobile, requestId);
                log.info("generateUrlResponse 492 : " + generateUrlResponse);

                log.info("redis response authenticate: " + redisService.getDataFromRedis(requestId));
                status = AuthenticationStatus.URL_GENERATED.name();
                updateChannelStatus(channels, channelName, status);
                return generateUrlResponse;*/

        } else if (ChannelType.sms.name().equals(channel.getName())) {
            saveDataService.saveMobileInRedis(requestId, smsMobile);

            //List<Channel> channelList = channelRepo.findByTxnIdAndStatusOrderByPriority(txnId, "PENDING");
            //Channel nextCannel = channelList.get(0);
            String response = handlerService.smsHandler(requestId, channel);
            if (response != null && requestId.equalsIgnoreCase(response)) {
                fallBackService.fallBack(requestId);
            }

        } else if (ChannelType.whatsApp.name().equals(channel.getName())) {
            saveDataService.saveMobileInRedis(requestId, whatsAppMobile);

            String response = handlerService.whatsAppHandler(requestId, channel);
            if (response != null && requestId.equalsIgnoreCase(response)) {
                fallBackService.fallBack(requestId);
            }

        }
        return null;

    }

    /*public GenerateUrlResponseDTO generateUrl(String mobileNumber, String txnId) {

        log.info("mobile number {} , txnId {} : ", mobileNumber, txnId);

        GenerateUrlResponseDTO generateUrlResponseDTO = new GenerateUrlResponseDTO();

        String newUrl = url1 + clientCallbackUri + "&client_id=" + clientId + "&scope=openid ip:phone_verify&state=" + txnId + "&login_hint=" + mobileNumber;
        generateUrlResponseDTO.setRequestId(txnId);
        generateUrlResponseDTO.setRedirectionUrl(newUrl);

        log.info("redis data in generate url : " + redisService.getDataFromRedis(txnId));

        return generateUrlResponseDTO;
    }*/

    private void updateChannelStatus(List<Channel> channels, String channelName, String status) {
        for (Channel channel : channels) {
            if (channel.getName().equals(channelName)) {
                channel.setStatus(status);
                channelRepo.save(channel);
            }
        }
    }


    public Object saveVerificationStatus(String code, String state) throws JsonProcessingException {

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

                        RedisDto redisDto = (RedisDto) redisService.getDataFromRedis(state);

                        String mobile = (String) userBody.get("login_hint");

                        List<Channel> channelList = new ArrayList<>();
                        channelList = channelRepo.findByTxnIdAndStatusOrderByPriority(state, "PENDING");
                        log.info("Channel List: {}", channelList.size());
                        Optional<Workflow> optionalWorkflow = workflowRepo.findByTxnId(state);

                        List<Channel> channelList1 = channelRepo.findByTxnIdAndStatusOrderByPriority(state, "URL GENERATED");
                        Channel channel = channelList.get(0);
                        Channel channel1 = channelList1.get(0);

                        if (!optionalWorkflow.isPresent()) {
                            log.error("Workflow not found for txnId: {}", state);
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workflow not found for txnId");
                        }

                        Workflow workflow = optionalWorkflow.get();

                        if ("true".equalsIgnoreCase(status)) {

                            saveDataService.saveRedisData(state, "Authenticated", login_hint,status , "silent_auth");

                            saveDataService.setfinalWorkflow(workflow, "silent_auth", true);

                            updateChannelStatusForAuthenticated(state, "silent_auth");

                            log.info("redis-data-silent_auth-true : {}", redisService.getDataFromRedis(state));
                            return redisDto;
                        }

                        if ("false".equalsIgnoreCase(status)) {
                            channel1.setStatus("AUTHENTICATION FAILED");
                            channelRepo.save(channel1);
                            fallBackService.fallBack(state);
                        }

                        /*if ("false".equalsIgnoreCase(status)) {
                            updateChannelStatusForAuthenticationFailure(state, "silent_auth");

                            *//*if(channelList.size() == 0) {
                                String msg = "User not validated via silent auth & other workflows";
                                saveDataService.saveRedisData("AUTHENTICATION_FAILED",msg,login_hint,state,"silent_auth");

                                saveDataService.setfinalWorkflow(workflow, "silent_auth",false);

                                redisService.saveDataToRedis(state,redisDto);

                            }*//*


                            else if ("sms".equalsIgnoreCase(channelList.get(0).getName())) {
                                String smsMobile = channelList.get(0).getNumber();
                                Object smsResponse = handleSmsFallbackwithoutInfobip(smsMobile,state);



                                if (smsResponse instanceof Map && "MESSAGE_NOT_SENT".equals(((Map) smsResponse).get("smsStatus"))) {

                                    redisDto.setStatus("false");
                                    log.info("redis-data 204 : {}", redisService.getDataFromRedis(login_hint));

                                    status = AuthenticationStatus.MESSAGE_NOT_SENT.name();
                                    redisDto.setStatus(status);
                                    log.info("SMS message not sent, trying next channel if available.");
                                } else {

                                    redisDto.setStatus("OTP_SENT");
                                    redisDto.setChannel("sms");
                                    redisDto.setMessage("OTP send to " + smsMobile + ". Please verify to authenticate!");
                                    redisService.saveDataToRedis(state, redisDto);

                                    channelList.get(0).setStatus("OTP_SENT");
                                    channelRepo.save(channelList.get(0));

                                    workflowRepo.save(workflow);
                                    log.info("redis data in sms sent : " + redisService.getDataFromRedis(state));
                                    return smsResponse;

                                }
                            }
                            else if ("whatsapp".equalsIgnoreCase(channelList.get(0).getName())) {
                                String mobileNum = channelList.get(0).getNumber();
                                String msg = "OTP send to " + mobileNum + ". Please verify to authenticate!";
                                saveDataService.saveRedisData("AUTHENTICATION_FAILED",msg,login_hint,state,"whatsApp");

                            } else {
                                String msg= "User not validated via silent auth & other workflows";
                                saveDataService.saveRedisData("AUTHENTICATION_FAILED",msg,login_hint,state,"");
                            }
                            return redisDto;
                        }*/
                        /*else if("true".equalsIgnoreCase(status)){

                            saveDataService.saveRedisData(status,"Authenticated",login_hint,state,"silent_auth");

                            saveDataService.setfinalWorkflow(workflow, "silent_auth",true);

                            updateChannelStatusForAuthenticated(state, "silent_auth");

                            log.info("redis-data-silent_auth-true : {}", redisService.getDataFromRedis(state));
                            return redisDto;
                        }*/
                    }
                }
            }
        }

        RedisDto dto = new RedisDto();
        return dto;

    }

    private void addValues(String code, MultiValueMap<String, String> values) {
        values.add("code", code);
        values.add("grant_type", "authorization_code");
        values.add("redirect_uri", clientCallbackUri);
        values.add("client_id", clientId);
        values.add("client_secret", clientSecret);
    }

    /*private void updateChannelStatusForAuthenticationFailure(String txnId, String channelName) {
        Optional<Channel> channels = channelRepo.findByTxnIdAndName(txnId, channelName);

        if (channels.isPresent()) {
            Channel channel = channels.get();
            channel.setStatus("AUTHENTICATION FAILED");
            channelRepo.save(channel);
            log.info("Updated channel status to AUTHENTICATED for txnId: {}, channel: {}", txnId, channelName);
        } else {
            log.warn("No channel found for txnId {} and name {}", txnId, channelName);
        }
    }
*/
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
