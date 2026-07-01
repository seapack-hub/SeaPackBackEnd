package org.seaPack.dto.ai;

import lombok.Data;

@Data
public class SkillBindingParamVO {

    private String paramName;

    private String label;

    private String paramType;

    private Integer required;

    private String options;

    private String placeholder;
}
