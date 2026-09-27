package com.evops.print.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.evops.common.AuditSupport;
import com.evops.common.BizException;
import com.evops.print.dto.MachineCreateRequest;
import com.evops.print.entity.PrintMachine;
import com.evops.print.mapper.PrintMachineMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrintMachineService {

    private final PrintMachineMapper machineMapper;

    public PrintMachineService(PrintMachineMapper machineMapper) {
        this.machineMapper = machineMapper;
    }

    /** 机台建档：编码唯一，重复直接拒绝 */
    @Transactional(rollbackFor = Exception.class)
    public PrintMachine create(MachineCreateRequest req) {
        String code = req.getMachineCode().trim();
        Long exists = machineMapper.selectCount(Wrappers.lambdaQuery(PrintMachine.class)
                .eq(PrintMachine::getMachineCode, code));
        if (exists != null && exists > 0) {
            throw new BizException("机台编码已存在: " + code);
        }
        PrintMachine machine = new PrintMachine();
        machine.setMachineCode(code);
        machine.setMachineName(req.getMachineName());
        machine.setMachineModel(req.getMachineModel());
        machine.setRemark(req.getRemark());
        machine.setStatus(PrintMachine.STATUS_ACTIVE);
        machine.setDeleted(0);
        AuditSupport.fill(machine, req.getOperator(), req.getRequestNo());
        try {
            machineMapper.insert(machine);
        } catch (DuplicateKeyException e) {
            // 并发下唯一约束兜底，与预检同一口径
            throw new BizException("机台编码已存在: " + code);
        }
        return machine;
    }

    /** 台账列表：软删记录不出现 */
    public List<PrintMachine> listAll() {
        return machineMapper.selectList(Wrappers.lambdaQuery(PrintMachine.class)
                .orderByAsc(PrintMachine::getId));
    }

    public PrintMachine getByCode(String machineCode) {
        PrintMachine machine = machineMapper.selectOne(Wrappers.lambdaQuery(PrintMachine.class)
                .eq(PrintMachine::getMachineCode, machineCode));
        if (machine == null) {
            throw new BizException("机台不存在: " + machineCode);
        }
        return machine;
    }

    /** 删除只打标记：先落审计字段，再由逻辑删除把 deleted 置 1，同一事务 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String machineCode, String operator, String requestNo) {
        PrintMachine machine = getByCode(machineCode);
        AuditSupport.fill(machine, operator, requestNo);
        if (machineMapper.updateById(machine) != 1) {
            throw new BizException("数据已被他人修改，请刷新后重试");
        }
        // MP 逻辑删除：UPDATE deleted=1（updateById 不会更新逻辑删除字段）
        machineMapper.deleteById(machine.getId());
    }
}
