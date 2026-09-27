package com.evops.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class MybatisPlusConfig {

    /** 乐观锁拦截器：配合实体上的 @Version，更新时自动校验并自增版本号 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /** 审计字段自动填充：时间戳与版本号随每次写操作落库 */
    @Bean
    public MetaObjectHandler auditMetaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                // 乐观锁初始版本：无条件写入，保证新增即带版本号
                setFieldValByName("version", 1L, metaObject);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 无条件刷新：先查后改时实体上带有旧的 updateTime，strictUpdateFill 会跳过
                setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
            }
        };
    }
}
