package org.seaPack.model.market;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.util.Date;

/**
 * 用户股票监控池实体
 * <p>记录用户添加了哪些股票到监控列表，含启用/停用状态。</p>
 */
@Data
@Entity
@Table(name = "user_stock_monitor")
public class UserStockMonitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("主键ID")
    private Long id;

    @Column(name = "user_id")
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "stock_code")
    @Comment("股票代码")
    private String stockCode;

    @Column(name = "is_active")
    @Comment("是否启用监控 (1-启用, 0-暂停)")
    private Integer isActive;

    @Column(name = "remark")
    @Comment("用户备注")
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "created_at")
    @Comment("创建时间")
    private Date createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updated_at")
    @Comment("更新时间")
    private Date updatedAt;
}
