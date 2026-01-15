package org.seaPack.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Data
@Entity
@Table(name = "sys_dict", indexes = {
        @Index(name = "idx_id", columnList = "id")
})
public class Dict {
    @Id
    @Column(name = "id")
    @Comment("唯一标识符")
    private Long id;

    @Column(name = "dict_type")
    @Comment("字典类型")
    private String dictType;

    @Column(name = "dict_code")
    @Comment("字典编号")
    private String dictCode;

    @Column(name = "dict_name")
    @Comment("字典名称")
    private String dictName;

    @Column(name = "order_num")
    @Comment("排序号")
    private String orderNum;

    @Column(name = "status")
    @Comment("状态")
    private String status;

    @Column(name = "remark")
    @Comment("注释")
    private String remark;

    @Column(name = "gmt_create")
    @Comment("创建时间")
    private Date gmtCreate;

    @Column(name = "gmt_modified")
    @Comment("修改时间")
    private Date gmtModified;
}
