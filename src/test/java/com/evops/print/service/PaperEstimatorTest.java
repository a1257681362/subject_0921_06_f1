package com.evops.print.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaperEstimatorTest {

    @Test
    void bodySheetsRoundsUp() {
        // 1000 份 ÷ 16 开 = 62.5 → 向上取整 63 张全张
        assertThat(PaperEstimator.bodySheets(1000, 16)).isEqualTo(63);
        // 整除不补
        assertThat(PaperEstimator.bodySheets(960, 16)).isEqualTo(60);
    }

    @Test
    void bodySheetsRejectsInvalid() {
        assertThatThrownBy(() -> PaperEstimator.bodySheets(0, 16))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaperEstimator.bodySheets(100, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void weightKgMatchesHandCalculation() {
        // 113 张 787×1092、80g：113 × 0.787 × 1.092 × 80 ÷ 1000 ≈ 7.769 kg
        BigDecimal weight = PaperEstimator.weightKg(113, 787, 1092, 80);
        assertThat(weight).isEqualByComparingTo("7.769");
        assertThat(weight.scale()).isEqualTo(3);
    }

    @Test
    void defaultSheetSizeIsPositiveDegree() {
        assertThat(PaperEstimator.DEFAULT_WIDTH_MM).isEqualTo(787);
        assertThat(PaperEstimator.DEFAULT_HEIGHT_MM).isEqualTo(1092);
    }
}
