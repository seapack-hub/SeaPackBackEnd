package org.seaPack.model.system;

import lombok.Data;

/**
 * 字典类型统计 VO
 * 用于字典管理左侧类型面板展示
 */
@Data
public class DictTypeVO {

    /** 字典类型编码（如 blog_category） */
    private String dictType;

    /** 该类型下的字典值数量 */
    private Integer count;

    /** 类型描述（取自排序号最小的那条记录的 remark，可选） */
    private String remark;
}
