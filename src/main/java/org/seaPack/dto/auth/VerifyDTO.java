package org.seaPack.dto.auth;

import lombok.Data;

import java.io.Serializable;

/**
 * 验证码校验请求 DTO
 * <p>提交用户拖拽滑块后的 token 和 x 坐标，服务端校验偏差是否在允许范围内。</p>
 */
@Data
public class VerifyDTO implements Serializable {

    private String token;
    private int userX;
    private String ename;
    private String cname;
    private int maxLen;
    private int minLen;

}
