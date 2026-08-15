# SuperMines Development Roadmap

本文档记录 SuperMines 的实际完成状态和后续开发顺序。状态定义：

- `[x]` 已完成并通过构建验证
- `[-]` 已完成一部分，但尚未形成完整功能
- `[ ]` 尚未开始

最后更新：2026-08-15

## 第一档：补缺口

目标：以较低成本补齐评测和日常管理中最明显的能力空白。

### 矿石形体与矿脉生成

- [ ] 设计形体条件的上下文，解决单格谓词无法表达邻接状态的问题
- [ ] `vein`：长度、方向、转向概率、分支概率、最大分支数
- [ ] `cluster/blob`：半径、填充率、边缘衰减和随机种子
- [ ] `layer`：按矿区相对 Y 分层配置不同方块池
- [ ] ConditionLoader 序列化、反序列化和嵌套支持
- [ ] GUI 条件选择器和参数编辑器
- [ ] 确定性随机测试，保证同一轮生成不会因遍历顺序改变形体

注意：`vein/cluster/layer` 不应简单实现成单格 `IGenerateCondition`。形体生成需要先生成结构，再映射到方块位置，否则无法可靠保证长度、连通性和分支。

### 随机事件与 Buff

- [ ] 矿区事件调度器和事件状态机
- [ ] 幸运时段：提高 Treasure 或稀有矿石权重
- [ ] 双倍掉落：按奖励类型配置倍率
- [ ] 矿脉爆发：临时改变生成池或生成特殊结构
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

- [x] 全局独立可再生矿点基础
- [x] 加权方块池、独立奖励、持久化倒计时、保护和完整 GUI
- [x] 创建时复生时间可选，默认 `0` 表示不自动复生
- [ ] 资源节点启用/禁用状态
- [ ] 矿点组/矿脉组，一个资源组管理多个坐标
- [ ] 组内全部挖空后的完成奖励
- [ ] 节点等级、升级条件和分等级方块池
- [ ] 玩家、队伍或岛屿独立的复生状态

### 矿权与私有矿区

- [ ] Mine 所有权模型：GLOBAL、PLAYER、GUILD、PROVIDER
- [ ] 所有者、成员和访客权限
- [ ] 进入、挖掘、传送和管理权限分离
- [ ] 可选 RegionOwnerProvider，避免硬依赖具体岛屿或领地平台
- [ ] 玩家离线和所有权转移策略

### 内置挖矿成长

- [ ] 独立于 Rank 的玩家挖矿等级和经验
- [ ] 幸运、效率、奖励倍率技能树
- [ ] 技能点、重置和升级成本
- [ ] GUI、PAPI 占位符和管理命令
- [ ] 与 mcMMO/AuraSkills 并存或替代的配置模式

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

- [x] `MineCreateEvent`、`MineRemoveEvent` 和 `MineEditEvent`
- [x] `MineResetStartEvent`（可取消）和 `MineResetEvent`（完成通知）
- [x] `TreasureFoundEvent`
- [x] `BlockBreakInMineEvent`
- [x] `RegenPointBreakEvent` 和 `RegenPointRespawnEvent`
- [x] 所有操作前事件提供 cancellable 语义
- [ ] 扩展 SuperMinesAPI，公开只读查询和受控修改入口

### GUI 与配置能力

- [x] 在 GUI chooser 中开放 Placeholder 条件
- [ ] WorldEdit/FAWE 选区批量创建资源节点
- [ ] Mine、Treasure、Rank 和资源节点复制功能
- [ ] 批量编辑复生时间、方块池、奖励和保护规则
- [ ] 配置校验、错误定位和导入/导出

### 平台生态

- [ ] `check-update` 更新检查落地

## 建议实施顺序

1. 修复第一档四个已知缺口，并补 reload 生命周期测试。
2. 为形体生成设计独立的 StructureGenerator 上下文，再实现 vein/cluster/layer。
3. 加入资源节点统计、ActionBar 倒计时和批量管理。
4. 抽象 EconomyProvider 与 RegionOwnerProvider，再接第三方平台。
5. 最后扩展公开 API、事件体系和商用运营能力。
