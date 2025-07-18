package org.seapack.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VerifyDTO implements Serializable {

    // 核心字段定义
    private String token;      // 验证会话标识（必传）
    private int userX;         // 用户滑动距离（像素位置）
    private String ename;      // 英文属性名（可选）
    private String cname;      // 中文描述（可选）
    private int maxLen;        // 最大长度限制（可选）
    private int minLen;        // 最小长度限制（可选）

}
