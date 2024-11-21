package com.anios.ipification.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class IpificationUtil {

    public static Map<String, Object> mapObject(Object object){
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = null;
        String message = null;

        try {
            responseMap = objectMapper.convertValue(object, Map.class);
            return responseMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }
}
