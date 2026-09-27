package com.evops.print.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.evops.common.AuditSupport;
import com.evops.common.BizException;
import com.evops.print.dto.OrderCreateRequest;
import com.evops.print.dto.VoidRequest;
import com.evops.print.entity.PrintOrder;
import com.evops.print.entity.PrintOrderReversal;
import com.evops.print.mapper.PrintMachineMapper;
import com.evops.print.mapper.PrintOrderMapper;
import com.evops.print.mapper.PrintOrderReversalMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrintOrderService {

    private final PrintOrderMapper orderMapper;
    private final PrintOrderReversalMapper reversalMapper;
    private final PrintMachineMapper machineMapper;

    public PrintOrderService(PrintOrderMapper orderMapper,
                             PrintOrderReversalMapper reversalMapper,
                             PrintMachineMapper machineMapper) {
        this.orderMapper = orderMapper;
        this.reversalMapper = reversalMapper;
        this.machineMapper = machineMapper;
    }

    /**
     * 印单建档：印单号唯一，重复直接拒绝并提示已存在；
     * 用纸量按开数与克重估算，损耗加放单列。
     */
    @Transactional(rollbackFor = Exception.class)
    public PrintOrder create(OrderCreateRequest req) {
        String orderNo = req.getOrderNo().trim();
        Long exists = orderMapper.selectCount(Wrappers.lambdaQuery(PrintOrder.class)
                .eq(PrintOrder::getOrderNo, orderNo));
        if (exists != null && exists > 0) {
            throw new BizException("印单号已存在: " + orderNo);
        }
        if (req.getMachineId() != null && machineMapper.selectById(req.getMachineId()) == null) {
            throw new BizException("机台不存在: id=" + req.getMachineId());
        }

        int width = req.getPaperWidthMm() != null ? req.getPaperWidthMm() : PaperEstimator.DEFAULT_WIDTH_MM;
        int height = req.getPaperHeightMm() != null ? req.getPaperHeightMm() : PaperEstimator.DEFAULT_HEIGHT_MM;
        int waste = req.getWasteSheets() != null ? req.getWasteSheets() : 0;
        int bodySheets = PaperEstimator.bodySheets(req.getCopies(), req.getSheetSplit());
        int totalSheets = bodySheets + waste;

        PrintOrder order = new PrintOrder();
        order.setOrderNo(orderNo);
        order.setOrderDate(req.getOrderDate());
        order.setCustomerName(req.getCustomerName());
        order.setProductName(req.getProductName());
        order.setCopies(req.getCopies());
        order.setSheetSplit(req.getSheetSplit());
        order.setGrammageG(req.getGrammageG());
        order.setPaperWidthMm(width);
        order.setPaperHeightMm(height);
        order.setPaperSheets(bodySheets);
        order.setWasteSheets(waste);
        order.setTotalSheets(totalSheets);
        order.setPaperWeightKg(PaperEstimator.weightKg(totalSheets, width, height, req.getGrammageG()));
        order.setMachineId(req.getMachineId());
        order.setRemark(req.getRemark());
        order.setStatus(PrintOrder.STATUS_NORMAL);
        order.setDeleted(0);
        AuditSupport.fill(order, req.getOperator(), req.getRequestNo());
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // 并发下唯一约束兜底，与预检同一口径
            throw new BizException("印单号已存在: " + orderNo);
        }
        return order;
    }

    /** 组合检索：印单号（精确）+ 日期区间 + 状态，条件可任意组合 */
    public List<PrintOrder> search(String orderNo, LocalDate dateFrom, LocalDate dateTo, String status) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BizException("日期范围起止颠倒");
        }
        if (StringUtils.hasText(status)
                && !PrintOrder.STATUS_NORMAL.equals(status)
                && !PrintOrder.STATUS_VOID.equals(status)) {
            throw new BizException("状态取值仅支持 NORMAL/VOID");
        }
        LambdaQueryWrapper<PrintOrder> query = Wrappers.lambdaQuery(PrintOrder.class)
                .eq(StringUtils.hasText(orderNo), PrintOrder::getOrderNo, orderNo)
                .ge(dateFrom != null, PrintOrder::getOrderDate, dateFrom)
                .le(dateTo != null, PrintOrder::getOrderDate, dateTo)
                .eq(StringUtils.hasText(status), PrintOrder::getStatus, status)
                .orderByDesc(PrintOrder::getOrderDate)
                .orderByDesc(PrintOrder::getId);
        return orderMapper.selectList(query);
    }

    public PrintOrder getByOrderNo(String orderNo) {
        PrintOrder order = orderMapper.selectOne(Wrappers.lambdaQuery(PrintOrder.class)
                .eq(PrintOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException("印单不存在: " + orderNo);
        }
        return order;
    }

    /**
     * 作废：反向记录 + 印单状态置 VOID，两表写在同一事务内，要么全成要么全不动。
     * 反向记录只增不改，prevReversalId 串起追溯链。
     */
    @Transactional(rollbackFor = Exception.class)
    public PrintOrderReversal voidOrder(String orderNo, VoidRequest req) {
        PrintOrder order = getByOrderNo(orderNo);
        if (PrintOrder.STATUS_VOID.equals(order.getStatus())) {
            throw new BizException("印单已作废，请勿重复作废: " + orderNo);
        }

        PrintOrderReversal last = reversalMapper.selectOne(Wrappers.lambdaQuery(PrintOrderReversal.class)
                .eq(PrintOrderReversal::getOrderId, order.getId())
                .orderByDesc(PrintOrderReversal::getId)
                .last("LIMIT 1"));
        Long count = reversalMapper.selectCount(Wrappers.lambdaQuery(PrintOrderReversal.class)
                .eq(PrintOrderReversal::getOrderId, order.getId()));
        long seq = (count == null ? 0 : count) + 1;

        PrintOrderReversal reversal = new PrintOrderReversal();
        reversal.setOrderId(order.getId());
        reversal.setOrderNo(order.getOrderNo());
        reversal.setReversalNo("RV-" + order.getOrderNo() + "-" + seq);
        reversal.setPrevReversalId(last == null ? null : last.getId());
        reversal.setReason(req.getReason());
        AuditSupport.fill(reversal, req.getOperator(), req.getRequestNo());
        reversalMapper.insert(reversal);

        order.setStatus(PrintOrder.STATUS_VOID);
        AuditSupport.fill(order, req.getOperator(), req.getRequestNo());
        if (orderMapper.updateById(order) != 1) {
            // 乐观锁冲突：回滚，反向记录一并撤销
            throw new BizException("数据已被他人修改，请刷新后重试");
        }
        return reversal;
    }

    /** 追溯链：同一印单的全部反向记录，按发生顺序返回 */
    public List<PrintOrderReversal> listReversals(String orderNo) {
        PrintOrder order = getByOrderNo(orderNo);
        return reversalMapper.selectList(Wrappers.lambdaQuery(PrintOrderReversal.class)
                .eq(PrintOrderReversal::getOrderId, order.getId())
                .orderByAsc(PrintOrderReversal::getId));
    }

    /** 删除只打标记：先落审计字段，再由逻辑删除把 deleted 置 1，同一事务 */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String orderNo, String operator, String requestNo) {
        PrintOrder order = getByOrderNo(orderNo);
        AuditSupport.fill(order, operator, requestNo);
        if (orderMapper.updateById(order) != 1) {
            throw new BizException("数据已被他人修改，请刷新后重试");
        }
        // MP 逻辑删除：UPDATE deleted=1（updateById 不会更新逻辑删除字段）
        orderMapper.deleteById(order.getId());
    }
}
