package com.evops.common;

import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 审计字段填充工具：保证每次写操作都带请求号与操作人落库。
 */
public final class AuditSupport {

    private AuditSupport() {
    }

    /** 请求号缺省时自动生成，保证每笔写都有可追溯的请求标识 */
    public static String requestNoOrGenerate(String requestNo) {
        return StringUtils.hasText(requestNo) ? requestNo.trim() : "REQ-" + UUID.randomUUID();
    }

    public static void fill(AuditEntity entity, String operator, String requestNo) {
        entity.setOperator(operator);
        entity.setRequestNo(requestNoOrGenerate(requestNo));
    }
}
