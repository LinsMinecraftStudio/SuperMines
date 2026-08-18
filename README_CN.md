<div align="center">
<h1>SuperMines</h1>
</div>

<div align="center">
一款为 Paper 服务器打造的免费开源矿区插件。
</div>

[English](README.md)

## 为什么选择 SuperMines

- **方块生成有规则，不再只靠随机** — 通过表层、Y 层、边界、生物群系和占位符条件控制生成位置
- **不只整区重置，单个矿点也能复生** — 全局可再生矿点独立计时，支持加权方块池与宝藏奖励
- **挖完直接进背包，减少重复操作** — 自动拾取可按矿区统一设置，也可由玩家单独开关
- **每一镐都有进度，挖矿不再只是刷方块** — 按挖掘数量提升等级，可选联动 mcMMO / AuraSkills 经验
- **不用反复改配置，游戏内就能管理** — 通过 GUI 编辑矿区、方块池、生成条件与可再生矿点
- **普通矿坑和球形矿区都能快速创建** — 支持选区创建，并可用 `/sm sphere` 直接生成球形矿区
- **兼容常用服务端生态** — 支持 Folia、ItemsAdder / Oraxen / Nexo / CraftEngine

## 特点

- 支持 MiniPlaceholders / PlaceholdersAPI
- 支持 Folia
- 支持 ItemsAdder / Oraxen / Nexo / CraftEngine 自定义方块
- 等级系统
- 可设置是否允许通过挖掘矿井方块获得 mcMMO / AuraSkills 经验值
- 为玩家提供出色的 I18n 本地化 — 根据客户端语言自动翻译
  - 目前已支持的语言：en-US、fr-FR、pt-BR、zh-CN、zh-TW
  - *部分翻译使用了 AI，如果你发现翻译有误，欢迎提交 PR 进行修正。*
- 使用 MittelLib 实现物品序列化系统
- 通过 `/sm sphere <半径>` 创建球形矿井
- 自动拾取 — 支持按矿井单独开关，也支持玩家单独开关（`/sm auto-pickup`），掉落物会直接进入玩家背包
- 广播控制
  - 可在 config.yml 中设置 `mine.broadcast-reset-messages` 来控制重置/提醒消息的发送范围
  - 可设置为发送给服务器内所有玩家，或仅发送给矿井内的玩家
- 提供图形界面来编辑矿区
- 全局可再生矿点 — 独立于矿区的单方块，按设定秒数自动复生，支持加权方块池与独立宝藏奖励
- 事件 API — 覆盖矿区创建/删除/编辑/重置及挖掘、宝藏、矿点事件，便于扩展与联动
- 生成条件 — 用条件控制每个方块的生成逻辑，可自由组合
  - 表层 / 指定 Y 层 / 矿区边缘 / 生物群系 / Placeholder（PlaceholdersAPI / MiniPlaceholders）等
- 更多功能即将推出...

## 未来计划

- 随机事件：幸运时段、双倍掉落
- 更新检查

详细计划请参阅 [开发路线图](ROADMAP.md)。

## 截图

### 指令帮助

![指令帮助](./media/command_help.png)

### 长方体矿区

![长方体矿区](./media/cuboid_mine.png)

### 球形矿区

![球形矿区](./media/sphere_mine.png)

### 边界条件：内部

下图中的铁矿仅生成在矿区内部。

![矿区内部的铁矿](./media/border_condition_core_in_mine.png)

## API

请参阅 [API 类](https://github.com/LinsMinecraftStudio/SuperMines/blob/main/src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)
