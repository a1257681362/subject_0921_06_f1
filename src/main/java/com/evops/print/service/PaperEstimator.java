package com.evops.print.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 用纸量估算：
 * 正文用纸（全张）= 印数 ÷ 开数，向上取整；
 * 损耗加放单列，合计 = 正文 + 加放；
 * 估算纸重（kg）= 合计张数 × 全张面积(m²) × 克重(g/m²) ÷ 1000。
 */
public final class PaperEstimator {

    /** 缺省全张尺寸：正度 787×1092mm */
    public static final int DEFAULT_WIDTH_MM = 787;
    public static final int DEFAULT_HEIGHT_MM = 1092;

    private PaperEstimator() {
    }

    public static int bodySheets(int copies, int sheetSplit) {
        if (copies < 1 || sheetSplit < 1) {
            throw new IllegalArgumentException("印数与开数必须大于0");
        }
        return (copies + sheetSplit - 1) / sheetSplit;
    }

    public static BigDecimal weightKg(int totalSheets, int widthMm, int heightMm, int grammageG) {
        BigDecimal sheets = BigDecimal.valueOf(totalSheets);
        BigDecimal widthM = BigDecimal.valueOf(widthMm).movePointLeft(3);
        BigDecimal heightM = BigDecimal.valueOf(heightMm).movePointLeft(3);
        // 张数 × 面积(m²) × 克重(g/m²) = 克；左移3位 ÷1000 → kg
        return sheets.multiply(widthM).multiply(heightM)
                .multiply(BigDecimal.valueOf(grammageG))
                .movePointLeft(3)
                .setScale(3, RoundingMode.HALF_UP);
    }
}
