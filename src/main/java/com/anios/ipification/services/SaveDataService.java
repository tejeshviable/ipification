package com.anios.ipification.services;

import com.anios.ipification.Entity.Channel;
import com.anios.ipification.Entity.Workflow;
import com.anios.ipification.Repository.WorkflowRepo;
import com.anios.ipification.enums.AuthenticationStatus;
import com.anios.ipification.requestDTO.GenerateUrlRequestDTO;
import com.anios.ipification.requestDTO.RedisDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaveDataService {

    @Autowired
    RedisService redisService;

    @Autowired
    WorkflowRepo workflowRepo;

    public Workflow setWorkflowInitial(GenerateUrlRequestDTO generateUrlRequestDTO, String requestId) {
        Workflow workflow = new Workflow();
        workflow.setTxnId(requestId);
        workflow.setBrand(generateUrlRequestDTO.getBrand());
        return workflow;
    }

    public void saveRedisData(String status,String msg,String mobile, String requestId,String channel) {
        RedisDto redisDto = new RedisDto();
        redisDto.setMobileNumber(mobile);
        redisDto.setMessage(msg);
        redisDto.setStatus(status);
        redisDto.setChannel(channel);
        redisService.saveDataToRedis(requestId, redisDto);
    }

    public void saveMobileInRedis(String urlMobile, String requestId) {
        RedisDto redisDto = new RedisDto();
        redisDto.setMobileNumber(urlMobile);
        redisService.saveDataToRedis(requestId, redisDto);
    }

    public Channel setChannels(GenerateUrlRequestDTO.WorkflowItem item, Workflow workflow, String requestId, int priority) {
        Channel channel = new Channel();
        channel.setWorkflow(workflow);
        channel.setName(item.getChannel());
        channel.setNumber(item.getMobileNumberTo());
        channel.setTxnId(requestId);
        channel.setStatus(AuthenticationStatus.PENDING.name());
        channel.setPriority(priority);
        return channel;
    }

    public void setfinalWorkflow(Workflow workflow,String fChannel,boolean status) {
        workflow.setFinalChannel(fChannel);
        workflow.setStatus(status);
        workflowRepo.save(workflow);
    }



}
