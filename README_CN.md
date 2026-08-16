<div align="center">
<h1>SuperMines</h1>
</div>

<div align="center">
一款为 Paper 服务器打造的免费开源矿区插件。
</div>

[English](README.md)

## 为什么选择 SuperMines

- **矿区自动刷新** — 挖空后按设定时间重新填满，生成内容可配置
- **自动拾取** — 掉落物直接进背包，可分别按矿井和玩家开关
- **挖掘成长** — 按挖掘方块数升级，可选联动 mcMMO / AuraSkills 经验
- **可再生矿点** — 全局独立矿点定时复生，支持加权方块池与独立宝藏奖励
- **生成条件** — 表层、Y 层、边缘、生物群系、Placeholder 等条件组合决定方块生成
- **图形化管理** — GUI 编辑矿区，`/sm sphere` 指令创建球形矿区
- **兼容性** — ItemsAdder / Oraxen / Nexo / CraftEngine 方块、Folia、4 种语言自动适配

## 特点

- 支持 MiniPlaceholders / PlaceholdersAPI
- 支持 Folia
- 支持 ItemsAdder / Oraxen / Nexo / CraftEngine 自定义方块
- 等级系统
- 可设置是否允许通过挖掘矿井方块获得 mcMMO / AuraSkills 经验值
- 为玩家提供出色的 I18n 本地化 — 根据客户端语言自动翻译
  - 目前已支持的语言：en-US、pt-BR、zh-CN、zh-TW
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

- 随机事件：幸运时段、双倍掉落、矿脉爆发
- ActionBar / BossBar 倒计时、状态浮字
- 更新检查

详细计划请参阅 [开发路线图](ROADMAP.md)。

## 截图

![image.png](https://www.nexusmc.cn/uploads/images/i9Fj9queNK_gxqUdzX-yO.webp)

![image.png](https://www.nexusmc.cn/uploads/images/LALclEe1BsYf72To81QWy.webp)

![2026-06-10_00.22.08.png](https://www.nexusmc.cn/uploads/images/2c1yaj_pV9PQNe65R87y2.webp)

## API

请参阅 [API 类](https://github.com/LinsMinecraftStudio/SuperMines/blob/main/src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)
