package com.evops.print.controller;

import com.evops.common.ApiResponse;
import com.evops.print.dto.OrderCreateRequest;
import com.evops.print.dto.VoidRequest;
import com.evops.print.entity.PrintOrder;
import com.evops.print.entity.PrintOrderReversal;
import com.evops.print.service.PrintOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/print/orders")
public class PrintOrderController {

    private final PrintOrderService orderService;

    public PrintOrderController(PrintOrderService orderService) {
        this.orderService = orderService;
    }

    /** 印单建档：印单号唯一，重复拒绝并提示已存在；用纸量随单估算 */
    @PostMapping
    public ApiResponse<PrintOrder> create(@Valid @RequestBody OrderCreateRequest req) {
        return ApiResponse.ok(orderService.create(req));
    }

    /** 组合检索：印单号 + 日期区间 + 状态，条件可任意组合 */
    @GetMapping
    public ApiResponse<List<PrintOrder>> search(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.search(orderNo, dateFrom, dateTo, status));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<PrintOrder> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.getByOrderNo(orderNo));
    }

    /** 作废：写反向记录并置状态，同事务；原单保留可追溯 */
    @PostMapping("/{orderNo}/void")
    public ApiResponse<PrintOrderReversal> voidOrder(@PathVariable String orderNo,
                                                     @Valid @RequestBody VoidRequest req) {
        return ApiResponse.ok(orderService.voidOrder(orderNo, req));
    }

    /** 追溯链：该印单的全部反向记录 */
    @GetMapping("/{orderNo}/reversals")
    public ApiResponse<List<PrintOrderReversal>> reversals(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.listReversals(orderNo));
    }

    /** 删除只打标记 */
    @DeleteMapping("/{orderNo}")
    public ApiResponse<Void> delete(@PathVariable String orderNo,
                                    @RequestParam String operator,
                                    @RequestParam(required = false) String requestNo) {
        orderService.delete(orderNo, operator, requestNo);
        return ApiResponse.ok(null);
    }
}
