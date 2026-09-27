package com.evops.print.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.evops.print.entity.PrintOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrintOrderMapper extends BaseMapper<PrintOrder> {
}
