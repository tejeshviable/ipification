package com.anios.ipification.requestDTO;

import lombok.Data;

import java.util.List;

@Data
public class GenerateUrlRequestDTO {
    private String brand;
    private List<WorkflowItem> workflow;

    @Data
    public static class WorkflowItem{
        private String channel;
        private String mobileNumberTo;
    }
}
