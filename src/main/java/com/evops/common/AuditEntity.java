package com.evops.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;

import java.time.LocalDateTime;

/**
 * 业务表审计基类：请求号、操作人、时间、版本随每次写操作落库。
 * 操作人为调用方传入的工号/姓名文本，独立于登录账号体系（认证接入后再收敛）。
 * 注意：不复用 {@link BaseEntity}，其 createBy/updateBy 为 Long 型用户主键，当前尚无用户体系。
 */
public class AuditEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 最近一次写操作的请求号，缺省时由服务端生成 */
    private String requestNo;

    /** 最近一次写操作的操作人 */
    private String operator;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 乐观锁版本号，每次更新自增 */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
