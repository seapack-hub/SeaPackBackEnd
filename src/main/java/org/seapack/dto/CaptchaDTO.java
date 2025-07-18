package org.seapack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDTO {

    private String bgImage;    // 背景图Base64
    private String sliderImage; // 滑块图Base64
    private String token;      // 验证会话ID
    private int sliderX;   // 滑块初始X坐标
    private int sliderY;   // 滑块初始Y坐标

}
