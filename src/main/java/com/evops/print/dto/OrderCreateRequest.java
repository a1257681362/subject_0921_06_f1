package com.evops.print.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

public class OrderCreateRequest {

    @NotBlank(message = "印单号不能为空")
    @Size(max = 64, message = "印单号最长64字符")
    private String orderNo;

    @NotNull(message = "印单日期不能为空")
    private LocalDate orderDate;

    @Size(max = 128, message = "客户名称最长128字符")
    private String customerName;

    @NotBlank(message = "产品名称不能为空")
    @Size(max = 128, message = "产品名称最长128字符")
    private String productName;

    @NotNull(message = "印数不能为空")
    @Min(value = 1, message = "印数必须大于0")
    private Integer copies;

    @NotNull(message = "开数不能为空")
    @Min(value = 1, message = "开数必须大于0")
    private Integer sheetSplit;

    @NotNull(message = "克重不能为空")
    @Min(value = 1, message = "克重必须大于0")
    private Integer grammageG;

    /** 全张纸尺寸，缺省 787×1092（正度） */
    @Min(value = 1, message = "纸张宽度必须大于0")
    private Integer paperWidthMm;

    @Min(value = 1, message = "纸张高度必须大于0")
    private Integer paperHeightMm;

    /** 损耗加放（全张），单列，缺省 0 */
    @Min(value = 0, message = "损耗加放不能为负")
    private Integer wasteSheets;

    private Long machineId;

    @Size(max = 512, message = "备注最长512字符")
    private String remark;

    @NotBlank(message = "操作人不能为空")
    @Size(max = 64, message = "操作人最长64字符")
    private String operator;

    /** 请求号，缺省时服务端自动生成 */
    @Size(max = 64, message = "请求号最长64字符")
    private String requestNo;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }
    public Integer getSheetSplit() { return sheetSplit; }
    public void setSheetSplit(Integer sheetSplit) { this.sheetSplit = sheetSplit; }
    public Integer getGrammageG() { return grammageG; }
    public void setGrammageG(Integer grammageG) { this.grammageG = grammageG; }
    public Integer getPaperWidthMm() { return paperWidthMm; }
    public void setPaperWidthMm(Integer paperWidthMm) { this.paperWidthMm = paperWidthMm; }
    public Integer getPaperHeightMm() { return paperHeightMm; }
    public void setPaperHeightMm(Integer paperHeightMm) { this.paperHeightMm = paperHeightMm; }
    public Integer getWasteSheets() { return wasteSheets; }
    public void setWasteSheets(Integer wasteSheets) { this.wasteSheets = wasteSheets; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
}
