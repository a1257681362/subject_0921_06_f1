package com.evops.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 端到端覆盖：建档、唯一约束拒绝、组合检索、用纸估算、作废反向记录与追溯链、软删、审计字段。
 */
@SpringBootTest
@AutoConfigureMockMvc
class PrintWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String toJson(Map<String, Object> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private Map<String, Object> machineBody(String code) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("machineCode", code);
        body.put("machineName", "海德堡对开-" + code);
        body.put("machineModel", "SM-102");
        body.put("operator", "zhang.san");
        return body;
    }

    private Map<String, Object> orderBody(String orderNo, LocalDate date) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderNo", orderNo);
        body.put("orderDate", date.toString());
        body.put("customerName", "新华书店");
        body.put("productName", "宣传册");
        body.put("copies", 1000);
        body.put("sheetSplit", 16);
        body.put("grammageG", 80);
        body.put("wasteSheets", 50);
        body.put("operator", "li.si");
        body.put("requestNo", "REQ-TEST-" + orderNo);
        return body;
    }

    // ---------- 机台台账 ----------

    @Test
    void machineCreateDuplicateRejectAndSoftDelete() throws Exception {
        // 建档成功，审计字段随写落库（请求号按传入值）
        mockMvc.perform(post("/api/print/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(machineBody("M001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.machineCode").value("M001"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.operator").value("zhang.san"))
                .andExpect(jsonPath("$.data.version").value(1));

        // 同编码重复建档直接拒绝并提示已存在
        mockMvc.perform(post("/api/print/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(machineBody("M001"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("机台编码已存在")));

        // 列表可查
        mockMvc.perform(get("/api/print/machines"))
                .andExpect(jsonPath("$.data[?(@.machineCode=='M001')]", hasSize(1)));

        // 删除只打标记：列表与详情均不再可见
        mockMvc.perform(delete("/api/print/machines/M001").param("operator", "wang.wu"))
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/print/machines"))
                .andExpect(jsonPath("$.data[?(@.machineCode=='M001')]", hasSize(0)));
        mockMvc.perform(get("/api/print/machines/M001"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("机台不存在")));
    }

    // ---------- 印单建档 / 用纸估算 ----------

    @Test
    void orderCreateEstimatesPaperAndRejectsDuplicate() throws Exception {
        mockMvc.perform(post("/api/print/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(orderBody("PO-1001", LocalDate.of(2026, 9, 20)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // 1000÷16 向上取整 63；损耗加放 50 单列；合计 113
                .andExpect(jsonPath("$.data.paperSheets").value(63))
                .andExpect(jsonPath("$.data.wasteSheets").value(50))
                .andExpect(jsonPath("$.data.totalSheets").value(113))
                .andExpect(jsonPath("$.data.paperWeightKg").value(7.769))
                .andExpect(jsonPath("$.data.status").value("NORMAL"))
                .andExpect(jsonPath("$.data.requestNo").value("REQ-TEST-PO-1001"))
                .andExpect(jsonPath("$.data.version").value(1));

        // 同一印单号重复建档直接拒绝
        mockMvc.perform(post("/api/print/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(orderBody("PO-1001", LocalDate.of(2026, 9, 20)))))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("印单号已存在")));

        // 缺操作人 → 参数校验失败
        Map<String, Object> bad = orderBody("PO-1002", LocalDate.of(2026, 9, 20));
        bad.remove("operator");
        mockMvc.perform(post("/api/print/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(bad)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("operator")));
    }

    // ---------- 组合检索 ----------

    @Test
    void searchByOrderNoAndDateRange() throws Exception {
        mockMvc.perform(post("/api/print/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderBody("PO-2001", LocalDate.of(2026, 9, 1)))));
        mockMvc.perform(post("/api/print/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderBody("PO-2002", LocalDate.of(2026, 9, 10)))));

        // 印单号精确
        mockMvc.perform(get("/api/print/orders").param("orderNo", "PO-2001"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orderNo").value("PO-2001"));

        // 日期区间
        mockMvc.perform(get("/api/print/orders")
                        .param("dateFrom", "2026-09-05")
                        .param("dateTo", "2026-09-15"))
                .andExpect(jsonPath("$.data[?(@.orderNo=='PO-2002')]", hasSize(1)))
                .andExpect(jsonPath("$.data[?(@.orderNo=='PO-2001')]", hasSize(0)));

        // 印单号 + 日期组合
        mockMvc.perform(get("/api/print/orders")
                        .param("orderNo", "PO-2002")
                        .param("dateFrom", "2026-09-01")
                        .param("dateTo", "2026-09-30"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].orderNo").value("PO-2002"));

        // 起止颠倒拒绝
        mockMvc.perform(get("/api/print/orders")
                        .param("dateFrom", "2026-09-30")
                        .param("dateTo", "2026-09-01"))
                .andExpect(jsonPath("$.success").value(false));
    }

    // ---------- 作废 / 反向记录 / 追溯链 ----------

    @Test
    void voidOrderWritesReversalAndKeepsTraceChain() throws Exception {
        mockMvc.perform(post("/api/print/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderBody("PO-3001", LocalDate.of(2026, 9, 21)))));

        Map<String, Object> voidBody = new LinkedHashMap<>();
        voidBody.put("reason", "客户撤单");
        voidBody.put("operator", "li.si");
        voidBody.put("requestNo", "REQ-VOID-1");

        // 作废：反向记录落库，原单置 VOID，版本号随更新自增
        mockMvc.perform(post("/api/print/orders/PO-3001/void")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(voidBody)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reversalNo").value("RV-PO-3001-1"))
                .andExpect(jsonPath("$.data.prevReversalId").doesNotExist());
        mockMvc.perform(get("/api/print/orders/PO-3001"))
                .andExpect(jsonPath("$.data.status").value("VOID"))
                .andExpect(jsonPath("$.data.version").value(2))
                .andExpect(jsonPath("$.data.requestNo").value("REQ-VOID-1"));

        // 追溯链可查
        mockMvc.perform(get("/api/print/orders/PO-3001/reversals"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].reason").value("客户撤单"));

        // 重复作废拒绝
        mockMvc.perform(post("/api/print/orders/PO-3001/void")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(voidBody)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("已作废")));
    }

    // ---------- 印单软删 ----------

    @Test
    void orderSoftDeleteHidesFromQueries() throws Exception {
        mockMvc.perform(post("/api/print/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(orderBody("PO-4001", LocalDate.of(2026, 9, 22)))));
        mockMvc.perform(delete("/api/print/orders/PO-4001").param("operator", "zhao.liu"))
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/print/orders").param("orderNo", "PO-4001"))
                .andExpect(jsonPath("$.data", hasSize(0)));
        mockMvc.perform(get("/api/print/orders/PO-4001"))
                .andExpect(jsonPath("$.success").value(false));
    }
}
