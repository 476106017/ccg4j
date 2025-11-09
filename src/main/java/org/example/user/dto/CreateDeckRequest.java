package org.example.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeckRequest {
    @NotBlank(message = "卡组名称不能为空")
    private String deckName;
    
    // 允许为空，创建空卡组
    private String deckData;
}
