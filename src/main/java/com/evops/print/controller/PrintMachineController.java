package com.evops.print.controller;

import com.evops.common.ApiResponse;
import com.evops.print.dto.MachineCreateRequest;
import com.evops.print.entity.PrintMachine;
import com.evops.print.service.PrintMachineService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/print/machines")
public class PrintMachineController {

    private final PrintMachineService machineService;

    public PrintMachineController(PrintMachineService machineService) {
        this.machineService = machineService;
    }

    /** 机台建档：编码唯一，重复拒绝 */
    @PostMapping
    public ApiResponse<PrintMachine> create(@Valid @RequestBody MachineCreateRequest req) {
        return ApiResponse.ok(machineService.create(req));
    }

    /** 台账列表（不含已删除） */
    @GetMapping
    public ApiResponse<List<PrintMachine>> list() {
        return ApiResponse.ok(machineService.listAll());
    }

    @GetMapping("/{machineCode}")
    public ApiResponse<PrintMachine> detail(@PathVariable String machineCode) {
        return ApiResponse.ok(machineService.getByCode(machineCode));
    }

    /** 删除只打标记 */
    @DeleteMapping("/{machineCode}")
    public ApiResponse<Void> delete(@PathVariable String machineCode,
                                    @RequestParam String operator,
                                    @RequestParam(required = false) String requestNo) {
        machineService.delete(machineCode, operator, requestNo);
        return ApiResponse.ok(null);
    }
}
