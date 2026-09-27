package com.evops.print.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.AuditEntity;

/**
 * 印单作废反向记录：只增不改不删。
 * 同一印单的多条反向记录通过 prevReversalId 串成追溯链。
 */
@TableName("t_print_order_reversal")
public class PrintOrderReversal extends AuditEntity {

    private Long orderId;
    private String orderNo;

    /** 反向记录单号：RV-{印单号}-{序号} */
    private String reversalNo;

    /** 上一条反向记录 id，首条为 null，由此串起追溯链 */
    private Long prevReversalId;

    private String reason;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getReversalNo() { return reversalNo; }
    public void setReversalNo(String reversalNo) { this.reversalNo = reversalNo; }
    public Long getPrevReversalId() { return prevReversalId; }
    public void setPrevReversalId(Long prevReversalId) { this.prevReversalId = prevReversalId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
