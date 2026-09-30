package org.seaPack.model.system;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 字典类型实体
 */
@Data
@Entity
@Table(name = "sys_dict_type")
public class DictType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "dict_type", unique = true, nullable = false, length = 50)
    @Comment("字典类型编码")
    private String dictType;

    @Column(name = "dict_name", nullable = false, length = 100)
    @Comment("字典类型名称")
    private String dictName;

    @Column(name = "remark", length = 500)
    @Comment("类型描述")
    private String remark;

    @Column(name = "order_num")
    @Comment("排序号")
    private Integer orderNum;

    @Column(name = "status", length = 1)
    @Comment("状态（1启用 0停用）")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "gmt_create")
    @Comment("创建时间")
    private Date gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "gmt_modified")
    @Comment("修改时间")
    private Date gmtModified;
}
