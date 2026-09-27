package com.evops.print.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.AuditEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 印单全量记录。印单号唯一；删除只打 deleted 标记；
 * 作废不改动原始记录，通过反向记录（t_print_order_reversal）完成，本表状态随之置为 VOID。
 */
@TableName("t_print_order")
public class PrintOrder extends AuditEntity {

    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_VOID = "VOID";

    private String orderNo;
    private LocalDate orderDate;
    private String customerName;
    private String productName;

    /** 印数（份） */
    private Integer copies;

    /** 开数：一张全张纸裁出的成品份数 */
    private Integer sheetSplit;

    /** 纸张克重 g/m² */
    private Integer grammageG;

    /** 全张纸尺寸，缺省 787×1092（正度） */
    private Integer paperWidthMm;
    private Integer paperHeightMm;

    /** 正文用纸（全张）= 印数 ÷ 开数 向上取整 */
    private Integer paperSheets;

    /** 损耗加放（全张），单列，不参与正文用纸 */
    private Integer wasteSheets;

    /** 合计用纸 = 正文用纸 + 损耗加放 */
    private Integer totalSheets;

    /** 估算纸重 kg = 合计张数 × 全张面积 × 克重 */
    private BigDecimal paperWeightKg;

    private Long machineId;
    private String status;
    private String remark;

    @TableLogic
    private Integer deleted;

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
    public Integer getPaperSheets() { return paperSheets; }
    public void setPaperSheets(Integer paperSheets) { this.paperSheets = paperSheets; }
    public Integer getWasteSheets() { return wasteSheets; }
    public void setWasteSheets(Integer wasteSheets) { this.wasteSheets = wasteSheets; }
    public Integer getTotalSheets() { return totalSheets; }
    public void setTotalSheets(Integer totalSheets) { this.totalSheets = totalSheets; }
    public BigDecimal getPaperWeightKg() { return paperWeightKg; }
    public void setPaperWeightKg(BigDecimal paperWeightKg) { this.paperWeightKg = paperWeightKg; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
