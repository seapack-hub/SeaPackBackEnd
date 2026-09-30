package org.seaPack.dto.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 字典类型统计 VO
 * 用于字典管理左侧类型面板展示
 */
@Data
public class DictTypeVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 字典类型编码（如 blog_category）
     */
    private String dictType;

    /**
     * 字典类型名称
     */
    private String dictName;

    /**
     * 类型描述
     */
    private String remark;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 状态（1启用 0停用）
     */
    private String status;

    /**
     * 该类型下的字典值数量
     */
    private Integer count;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date gmtModified;
}
