package org.seaPack.dto.ai;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

import java.util.List;

@Data
public class SkillBindingVO {

    private Long skillId;

    private String skillName;

    private String skillCode;

    private String skillType;

    private String endpoint;

    private Integer timeoutMs;

    private String inputSchema;

    private String moduleKey;

    private String position;

    private Integer status;

    private List<SkillBindingParamVO> params;

    @JsonRawValue
    public String getConfig() {
        return config;
    }

    private String config;
}
