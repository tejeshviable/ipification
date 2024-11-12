package com.anios.ipification.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    private final ValueOperations<String, Object> valueOperations;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        valueOperations = redisTemplate.opsForValue();
    }

    public void saveDataToRedis(String key, Object object){
        valueOperations.set(key, object);
    }

    public Object getDataFromRedis(String key){
        return valueOperations.get(key);
    }

    public void deleteDataFromRedis(String key){
        valueOperations.getAndDelete(key);
    }

}