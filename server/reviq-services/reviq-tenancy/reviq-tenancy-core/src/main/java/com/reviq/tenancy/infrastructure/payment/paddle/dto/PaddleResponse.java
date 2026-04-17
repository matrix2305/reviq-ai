package com.reviq.tenancy.infrastructure.payment.paddle.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaddleResponse<T> {

    private T data;
    private Map<String, Object> meta;
    private PaddleError error;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record PaddleError(
            String type,
            String code,
            String detail
    ) {}

    public boolean isSuccess() {
        return error == null;
    }
}
