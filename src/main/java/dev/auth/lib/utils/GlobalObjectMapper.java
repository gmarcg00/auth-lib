package dev.auth.lib.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GlobalObjectMapper {

    private static ObjectMapper objectMapper;

    private GlobalObjectMapper(){}

    public static ObjectMapper getInstance(){
        if(objectMapper == null){
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }
}
