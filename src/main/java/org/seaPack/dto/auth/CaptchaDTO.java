package org.seaPack.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 滑块验证码 DTO
 * <p>包含背景图（带缺口）、滑块图、会话 token 及滑块初始坐标，
 * 前端据此渲染滑块验证组件。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDTO {

    private String bgImage;
    private String sliderImage;
    private String token;
    private int sliderX;
    private int sliderY;

}
