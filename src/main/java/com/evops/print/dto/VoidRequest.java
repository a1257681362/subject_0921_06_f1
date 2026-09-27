package com.evops.print.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class VoidRequest {

    @NotBlank(message = "作废原因不能为空")
    @Size(max = 512, message = "作废原因最长512字符")
    private String reason;

    @NotBlank(message = "操作人不能为空")
    @Size(max = 64, message = "操作人最长64字符")
    private String operator;

    /** 请求号，缺省时服务端自动生成 */
    @Size(max = 64, message = "请求号最长64字符")
    private String requestNo;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
}
