package org.example.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpenPackRequest {
    @NotBlank(message = "卡包代码不能为空")
    private String packCode;
}
