package org.seaPack.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Data
@Entity
@Table(name = "sys_dict")
public class Dict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "dict_type")
    @Comment("字典类型")
    private String dictType;

    @Column(name = "dict_code")
    @Comment("字典编码（同一类型下唯一）")
    private String dictCode;

    @Column(name = "dict_name")
    @Comment("字典名称")
    private String dictName;

    @Column(name = "order_num")
    @Comment("排序号")
    private Integer orderNum;

    @Column(name = "status")
    @Comment("状态（1启用 0停用）")
    private String status;

    @Column(name = "remark")
    @Comment("备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "gmt_create")
    @Comment("创建时间")
    private Date gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "gmt_modified")
    @Comment("修改时间")
    private Date gmtModified;
}
