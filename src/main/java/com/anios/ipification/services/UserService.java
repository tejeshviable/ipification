package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.ChannelRepo;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.enums.AuthenticationStatus;
import com.anios.ipification.enums.ChannelType;
import com.anios.ipification.enums.WhatsappMediaType;
import com.anios.ipification.feign.IpificationFeign;
import com.anios.ipification.requestDTO.*;
import com.anios.ipification.responseDTO.GenerateUrlResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Value("${url.redirect}")
    String url;
    @Value("${url.callback}")
    String url1;

    @Value("${url.clientId}")
    String clientId;

    @Value("${url.client-callback-uri}")
    String clientCallbackUri;

    @Value("${url.clientSecret}")
    String clientSecret;
    @Autowired
    RedisService redisService;

    @Autowired
    IpificationFeign ipificationFeign;

    @Autowired
    AuthService authService;

    @Value("${infobip.api.key}")
    private String apiKey;

    @Value("${infobip.applicationId}")
    private String applicationId;

    @Value("${infobip.messageId}")
    private String messageId;

    @Value("${infobip.from}")
    private String from;

    @Autowired
    SmsService smsService;

    @Autowired
    WorkflowRepo workflowRepo;

    @Autowired
    ChannelRepo channelRepo;

    public RedisDto saveVerificationStatus(String code, String requestId, String error, String errorDescription) {

        if(error != null && !"".equals(error)) {
            System.out.println("Received error in service: " + error);
            System.out.println("Received error_description in service: " + errorDescription);

            RedisDto redisDto = (RedisDto) redisService.getDataFromRedis(requestId);
            redisDto.setStatus("false");
            redisService.saveDataToRedis(requestId, redisDto);

            return RedisDto.builder().errorMsg(errorDescription).status("false").build();
        }
        MultiValueMap<String,String> values = new LinkedMultiValueMap<>();
        values.add("code",code);
        values.add("grant_type","authorization_code");
        values.add("redirect_uri",clientCallbackUri);
        values.add("client_id",clientId);
        values.add("client_secret",clientSecret);

        ResponseEntity<?> tokens =  ipificationFeign.fetchToken(values);

        if(tokens.getStatusCode().is2xxSuccessful()){
            Map<String, Object> body = (Map<String, Object>) tokens.getBody();

            if (body != null && body.containsKey("access_token")) {
                String accessToken = (String) body.get("access_token");
                String bearerToken = "Bearer "+accessToken;

                ResponseEntity<?> userInfoResponse = ipificationFeign.userDetails(bearerToken);
                if (userInfoResponse.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> userBody = (Map<String,Object>) userInfoResponse.getBody();
                    if(userBody != null && userBody.containsKey("phone_number_verified")){
                        System.out.println("Tejeshvi : {}"+ userBody);
                        String status = (String) userBody.get("phone_number_verified");
                        String login_hint = (String) userBody.get("login_hint");


                        RedisDto redisDto = (RedisDto) redisService.getDataFromRedis(requestId);
                        redisDto.setStatus(status);

                        redisService.saveDataToRedis(requestId,redisDto);

                        log.info("redis-data : {}", redisService.getDataFromRedis(requestId));
                        return redisDto;
                    }
                }
            }
        }
        return RedisDto.builder().status("false").build();
    }

    public RedisDto getUserStatus(String mobileNumber) {
        if(redisService.getDataFromRedis(mobileNumber) == null)
        {
            return RedisDto.builder().status("false").build();
        }
        return (RedisDto) redisService.getDataFromRedis(mobileNumber);

    }

    public RedisDto getUserStatus(String mobileNumber, String requestId) {
        if(redisService.getDataFromRedis(requestId) == null)
        {
            return RedisDto.builder().status("false").build();
        }
        return (RedisDto) redisService.getDataFromRedis(requestId);

    }

    private Object handleWhatsAppFallback(String whatsAppMobile) {
        MediaRequestDTO mediaRequestDTO = new MediaRequestDTO();
        mediaRequestDTO.setTo(whatsAppMobile);
        mediaRequestDTO.setType(WhatsappMediaType.template);
        mediaRequestDTO.setMessagingProduct("whatsapp");

        MediaRequestDTO.WhatsappTemplate.Language language = new MediaRequestDTO.WhatsappTemplate.Language();
        language.setCode("en_US");

        MediaRequestDTO.WhatsappTemplate whatsappTemplate = new MediaRequestDTO.WhatsappTemplate();
        whatsappTemplate.setLanguage(language);
        whatsappTemplate.setName("aionos_team_updates_1");

        mediaRequestDTO.setTemplate(whatsappTemplate);
        return smsService.sendWhatsAppMessage(mediaRequestDTO);
    }

    private Object handleSmsFallback(String smsMobile) {
        PinRequestDTO pinRequestDTO = new PinRequestDTO();
        pinRequestDTO.setTo(smsMobile);
        pinRequestDTO.setFrom(from);
        pinRequestDTO.setMessageId(messageId);
        pinRequestDTO.setApplicationId(applicationId);

        return authService.sendPin(apiKey, true, pinRequestDTO);
    }

    private String handleSilentAuth(String urlMobile, String requestId) {
        if (urlMobile == null) return null;

        String newUrl = url1 + clientCallbackUri + "&client_id=" + clientId
                + "&scope=openid ip:phone_verify&state=" + requestId
                + "&login_hint=" + urlMobile;
//        String newUrl = null;
        log.info("Generated Silent Auth URL: {}", newUrl);

        return newUrl;
    }


    public Object authenticateUser(GenerateUrlRequestDTO generateUrlRequestDTO, boolean skipUrlGeneration)  {

        GenerateUrlResponseDTO generateUrlResponseDTO = new GenerateUrlResponseDTO();
        String urlMobile = null,smsMobile = null,whatsAppMobile = null;

        Workflow workflow = new Workflow();
        List<Channel> channels = new ArrayList<>();

        String requestId = UUID.randomUUID().toString();
        workflow.setTxnId(requestId);
        workflow.setBrand(generateUrlRequestDTO.getBrand());

        RedisDto redisDto = RedisDto.builder().requestId(generateUrlResponseDTO.getRequestId()).build();

        if (generateUrlRequestDTO.getWorkflow() != null && !generateUrlRequestDTO.getWorkflow().isEmpty()) {
            for (GenerateUrlRequestDTO.WorkflowItem item : generateUrlRequestDTO.getWorkflow()) {

                Channel channel = new Channel();
                channel.setWorkflow(workflow);
                channel.setName(item.getChannel());
                channel.setNumber(item.getMobileNumberTo());

                channel.setStatus(AuthenticationStatus.PENDING.name());

                log.info("Processing item with channel: {}", item.getChannel());
                if (ChannelType.sms.name().equals(item.getChannel())) {
                    smsMobile = item.getMobileNumberTo();
                    log.info("smsMobile {}",smsMobile);
                }
                else if (ChannelType.silent_auth.name().equals(item.getChannel())) {
                    urlMobile = item.getMobileNumberTo();
                    log.info("urlMobile {}",urlMobile);
                }
                else if (ChannelType.whatsApp.name().equals(item.getChannel())) {
                    whatsAppMobile = item.getMobileNumberTo();
                    log.info("whatsAppMobile {}",whatsAppMobile);

                }
                channels.add(channel);
            }
            workflow.setChannels(channels);
            workflowRepo.save(workflow);
        }

        //newUrl = url1 + clientCallbackUri + "&client_id=" + clientId + "&scope=openid ip:phone_verify&state=" + requestId + "&login_hint=" + urlMobile;

        for(GenerateUrlRequestDTO.WorkflowItem item:generateUrlRequestDTO.getWorkflow())
        {
            String channelName = item.getChannel();
            String status;

            if(ChannelType.sms.name().equals(channelName)){
                Object smsResponse = handleSmsFallback(smsMobile);
                if (smsResponse instanceof Map && "MESSAGE_NOT_SENT".equals(((Map) smsResponse).get("smsStatus"))) {

                    status = AuthenticationStatus.MESSAGE_NOT_SENT.name();
                    log.info("SMS message not sent, trying next channel if available.");
                }
                    else{
                    redisService.saveDataToRedis(smsMobile,redisDto);
                        workflow.setFinalChannel("sms");
                        workflow.setStatus(true);
                        status = AuthenticationStatus.MESSAGE_SENT.name();
                        workflowRepo.save(workflow);
                        updateChannelStatus(channels, channelName, status);
                        return smsResponse;
                    }
            }
            else if (ChannelType.silent_auth.name().equals(channelName)) {
                String url = handleSilentAuth(urlMobile, requestId);
                if(skipUrlGeneration) {
                    url = null;
                }
                if (url == null) {
                    status = AuthenticationStatus.URL_GENERATION_FAILED.name();
                    log.error("Silent auth URL generation failed, trying next channel if available.");

                }
                else{
                    redisService.saveDataToRedis(urlMobile,redisDto);
                    workflow.setFinalChannel("silent_auth");
                    workflow.setStatus(true);
                    workflowRepo.save(workflow);
                    status = AuthenticationStatus.URL_GENERATED.name();
                    updateChannelStatus(channels, channelName, status);
                    generateUrlResponseDTO.setRequestId(requestId);
                    generateUrlResponseDTO.setRedirectionUrl(url);
                    return ResponseEntity.ok(generateUrlResponseDTO);
                }


            }
            else if (ChannelType.whatsApp.name().equals(channelName)) {
                String waResponse = (String) handleWhatsAppFallback(whatsAppMobile);
                if (waResponse == null || waResponse.contains("Failed to send message")) {
                    status = AuthenticationStatus.MESSAGE_NOT_SENT.name();
                    log.error("WhatsApp message not sent to {}, trying next channel if available.", whatsAppMobile);
                }else {
                    redisService.saveDataToRedis(whatsAppMobile,redisDto);

                    workflow.setFinalChannel("whatsApp");
                    workflow.setStatus(true);
                    status = AuthenticationStatus.MESSAGE_SENT.name();
                    workflowRepo.save(workflow);
                    updateChannelStatus(channels, channelName, status);

                    return waResponse;
                }
            }
            else {
                status = AuthenticationStatus.AUTHENTICATION_FAILED.name();
                workflow.setStatus(false);
                workflowRepo.save(workflow);
            }

            updateChannelStatus(channels, channelName, status);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("All channels failed to generate URL or send message.");
        }


    private void updateChannelStatus(List<Channel> channels, String channelName, String status) {
        for (Channel channel : channels) {
            if (channel.getName().equals(channelName)) {
                channel.setStatus(status);
                channelRepo.save(channel);
            }
        }
    }

    public GenerateUrlResponseDTO generateUrl(MobileRequestDTO mobileRequestDTO)  {

        GenerateUrlResponseDTO generateUrlResponseDTO = new GenerateUrlResponseDTO();

        String requestId = UUID.randomUUID().toString();
        String newUrl = url1+clientCallbackUri+"&client_id="+clientId+"&scope=openid ip:phone_verify&state="+requestId+"&login_hint="+ mobileRequestDTO.getMobileNumber();
        generateUrlResponseDTO.setRequestId(requestId);
        generateUrlResponseDTO.setRedirectionUrl(newUrl);
        RedisDto redisDto = RedisDto.builder().requestId(generateUrlResponseDTO.getRequestId()).build();
        redisService.saveDataToRedis(mobileRequestDTO.getMobileNumber(), redisDto);
        redisService.saveDataToRedis(requestId, redisDto);

        return generateUrlResponseDTO;
    }

}
