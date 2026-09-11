package com.lbzxks.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbzxks.common.exception.BusinessException;

import java.util.List;

/**
 * JSON 工具 (Jackson)
 */
public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new BusinessException("JSON 序列化失败");
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new BusinessException("JSON 解析失败");
        }
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new BusinessException("JSON 解析失败");
        }
    }
}
