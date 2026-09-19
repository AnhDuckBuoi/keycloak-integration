package com.devteria.profile.exception;

import com.devteria.profile.dto.identity.KeyCloakError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ErrorNormalizer {
    ObjectMapper objectMapper;
    Map<String, ErrorCode> map;

    public ErrorNormalizer() {
        this.objectMapper = new ObjectMapper();
        this.map = new ConcurrentHashMap<>();
        this.map.put("User exists with same email", ErrorCode.EMAIL_EXITED);
    }

    public AppException handleKeyCloakException(FeignException exception){
        try {
            log.warn("can not complete request");
            var response = objectMapper.readValue(exception.contentUTF8(), KeyCloakError.class);
            if(Objects.nonNull(response.getErrorMessage())
                    && Objects.nonNull(map.get(response.getErrorMessage()))
            ) throw new AppException(map.get(response.getErrorMessage()));
        } catch (JsonProcessingException e) {
            log.error("can not deserialization");
            throw new RuntimeException(e);
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
