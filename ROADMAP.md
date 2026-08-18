# SuperMines Development Roadmap

本文档记录 SuperMines 的实际完成状态和后续开发顺序。状态定义：

- `[x]` 已完成并通过构建验证
- `[-]` 已完成一部分，但尚未形成完整功能
- `[ ]` 尚未开始

最后更新：2026-08-18

## 第一档：补缺口

### 随机事件与 Buff

- [ ] 矿区事件调度器和事件状态
- [ ] 幸运时段：提高 Treasure 或稀有矿石权重
- [ ] 双倍掉落：按奖励类型配置倍率
- [ ] 事件开始/结束消息、粒子、音效和 API 事件
- [ ] GUI 查看、立即触发、停止和配置事件

### 已知缺口与 Bug

- [ ] 注册 README/帮助中已有但缺失的 `/sm info <mine>`
- [ ] 统一 `mine.auto-pickup.*` 与代码读取路径
- [ ] 为 `ranks takeRank` 补充权限检查
- [ ] reload 热加载 Mine、Treasure、Rank 和 RegenPoint 数据文件
- [ ] reload 时安全重建相关定时任务和索引
- [ ] 定位 Gradle 10 deprecation warning 来源

## 第二档：核心玩法差异化

### 动态成长矿石

- [ ] 资源节点启用/禁用状态
- [ ] 矿点组/矿脉组，一个资源组管理多个坐标
- [ ] 组内全部挖空后的完成奖励
- [ ] 节点等级、升级条件和分等级方块池
- [ ] 玩家、队伍或岛屿独立的复生状态

## 第三档：运营与可视化

### 反馈与展示

- [-] 资源节点复生粒子和音效已完成
- [ ] 稀有矿石、Treasure、Mine 重置的分级粒子和音效配置
- [ ] ActionBar 资源节点倒计时
- [ ] BossBar 矿区重置和事件倒计时
- [ ] DecentHolograms/HolographicDisplays 可选 Hook
- [ ] 矿区和资源节点实时状态浮字

## 第四档：生态

### API 与事件

- [ ] 扩展 SuperMinesAPI，公开只读查询和受控修改入口

### GUI 与配置能力

- [ ] Mine、Treasure、Rank 和资源节点复制功能
- [ ] 批量编辑复生时间、方块池、奖励和保护规则
- [ ] 配置校验、错误定位和导入/导出

### 平台生态

- [ ] `check-update` 更新检查