package com.evops.common;

/**
 * 业务异常：消息原样返回给调用方（如“印单号已存在”）。
 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
