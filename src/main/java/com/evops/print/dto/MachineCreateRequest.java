package com.evops.print.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MachineCreateRequest {

    @NotBlank(message = "机台编码不能为空")
    @Size(max = 64, message = "机台编码最长64字符")
    private String machineCode;

    @NotBlank(message = "机台名称不能为空")
    @Size(max = 128, message = "机台名称最长128字符")
    private String machineName;

    @Size(max = 128, message = "机台型号最长128字符")
    private String machineModel;

    @Size(max = 512, message = "备注最长512字符")
    private String remark;

    @NotBlank(message = "操作人不能为空")
    @Size(max = 64, message = "操作人最长64字符")
    private String operator;

    /** 请求号，缺省时服务端自动生成 */
    @Size(max = 64, message = "请求号最长64字符")
    private String requestNo;

    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getMachineModel() { return machineModel; }
    public void setMachineModel(String machineModel) { this.machineModel = machineModel; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
}
