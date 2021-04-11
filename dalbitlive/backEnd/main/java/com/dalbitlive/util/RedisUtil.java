package com.dalbitlive.util;

import com.dalbitlive.common.json.CustomObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RedisUtil {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void putHash(String name, String key, Object value){
        HashOperations<String, Object, Object> redisOperation = redisTemplate.opsForHash();
        CustomObjectMapper mapper = new CustomObjectMapper();
        try{
            redisOperation.put(name, key, mapper.writeValueAsString(value));
        }catch(JsonProcessingException e){
            redisOperation.put(name, key, new Gson().toJson(value));
        }
    }

    public void delete(String name){
        redisTemplate.delete(name);
    }

    public <T> T getHashData(String name, String key, Class<T> obj){
        HashOperations<String, Object, Object> redisOperation = redisTemplate.opsForHash();
        String value = (String)redisOperation.get(name, key);
        if(!DalbitUtil.isEmpty(value)){
            CustomObjectMapper mapper = new CustomObjectMapper();
            try{
                return mapper.readValue(value, obj);
            }catch(JsonProcessingException e){
                return new Gson().fromJson(value, obj);
            }
        }
        try {
            return obj.newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
        return null;
    }

    public <T> List<T> getHashList(String name, String key, Class obj) {
        HashOperations<String, Object, Object> redisOperation = redisTemplate.opsForHash();
        String value = (String)redisOperation.get(name, key);
        if(!DalbitUtil.isEmpty(value)){
            CustomObjectMapper mapper = new CustomObjectMapper();
            try{
                return (List<T>) mapper.readValue(value, new TypeReference<List>() {
                });
            }catch(JsonProcessingException e){
                return (List<T>) new Gson().fromJson(value, List.class);
            }
        }
        return new ArrayList<>();
    }
}
