# EVOPS 基础工作区

这是 springboot的骨架项目。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

启动前准备：

1. 执行 `mvn -q -DskipTests compile` 验证骨架构建。
2. 执行 `mvn spring-boot:run` 启动应用；Spring Boot 会自动执行 `src/main/resources/schema.sql`。
3. H2 数据文件默认写入工作区 `data/evops`。如需修改路径，编辑 `application.yml` 中的 `jdbc:h2:file:` 连接串。

根目录的 `..\..\docs\schema\evops.sql` 是交付副本，应与工作区 `src/main/resources/schema.sql` 保持一致。

## F1 印刷生产管理（印单 + 机台台账）

**数据表**（见 `schema.sql`，版本号 `print-1`）：

- `t_print_machine` 机台台账，机台编码唯一。
- `t_print_order` 印单全量记录，印单号唯一约束。
- `t_print_order_reversal` 印单作废反向记录，只增不改不删，`prev_reversal_id` 串起追溯链。

**约定**：

- 删除只打 `deleted` 标记，不做物理删除；软删记录仍占用唯一键（防追溯链断链）。
- 作废走反向记录：原单保留，状态置 `VOID`，同时新增一条反向记录；两表写在同一事务内，要么全成要么全不动。
- 审计字段随每次写操作落库：`request_no`（缺省自动生成 `REQ-<uuid>`）、`operator`、`create_time`/`update_time`、`version`（乐观锁，更新冲突返回“数据已被他人修改”）。
- 用纸量估算：正文用纸 = 印数 ÷ 开数（向上取整，全张）；损耗加放单列；估算纸重 = 合计张数 × 全张面积（缺省正度 787×1092mm）× 克重。
- 同一印单号/机台编码重复建档直接拒绝并提示已存在（服务预检 + 数据库唯一约束双保险）。
- `/api/print/**` 暂经 Shiro anon 放行（`shiro.annotations.enabled=false`），认证体系接入后收紧，操作人改由登录态获取。

**接口**：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/print/machines` | 机台建档 |
| GET | `/api/print/machines` | 机台列表（不含已删除） |
| GET | `/api/print/machines/{code}` | 机台详情 |
| DELETE | `/api/print/machines/{code}?operator=` | 机台软删 |
| POST | `/api/print/orders` | 印单建档（返回用纸估算） |
| GET | `/api/print/orders?orderNo=&dateFrom=&dateTo=&status=` | 组合检索，条件可任意组合 |
| GET | `/api/print/orders/{orderNo}` | 印单详情 |
| POST | `/api/print/orders/{orderNo}/void` | 作废（body: reason/operator/requestNo） |
| GET | `/api/print/orders/{orderNo}/reversals` | 作废追溯链 |
| DELETE | `/api/print/orders/{orderNo}?operator=` | 印单软删 |

示例：

```bash
curl -X POST localhost:8080/api/print/orders -H 'Content-Type: application/json' -d '{
  "orderNo":"PO-20260926-001","orderDate":"2026-09-26","productName":"精装画册",
  "copies":1000,"sheetSplit":16,"grammageG":80,"wasteSheets":50,"operator":"lisi"
}'
# → paperSheets=63, wasteSheets=50, totalSheets=113, paperWeightKg=7.769
```

测试：`mvn test`（单测 + H2 内存库集成测试，覆盖建档/重复拒绝/组合检索/作废链/软删）。
