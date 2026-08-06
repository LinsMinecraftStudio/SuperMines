<div align="center">
<h1>SuperMines</h1>
</div>

<div align="center">
一款为 Paper 服务器打造的强大、易用且免费的矿区插件。
</div>

[English](README.md)

## 为什么选择 SuperMines

- **全自动刷新，矿品不留洞** — 矿区生成永远填满，刷新时间随心调，玩家永远有事可挖
- **每一块都精雕细琢** — 支持 ItemsAdder / Oraxen / Nexo / CraftEngine 自定义方块，矿石位置用生成条件说了算：表层、随机概率、矿区内指定 Y 层、矿区边缘、指定生物群系……一条配置，随意组合
- **越玩越上瘾** — 自动拾取掉落物直入背包，一刀一收获；按挖掘次数成长的等级与经验体系，配合 mcMMO / AuraSkills（可选开关），升级有目标、挖矿有爽感
- **管理如使臂指** — 图形界面一键编辑矿区，一条指令生成球形矿区
- **装上就开玩** — 4 种语言自动适配玩家客户端，Folia 高负载依旧丝滑

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
- 更多功能即将推出...

## 未来计划

1. 生成条件（已实现，支持任意嵌套组合）
2. 粒子效果???

## 截图

![image.png](https://www.nexusmc.cn/uploads/images/i9Fj9queNK_gxqUdzX-yO.webp)

![image.png](https://www.nexusmc.cn/uploads/images/LALclEe1BsYf72To81QWy.webp)

![2026-06-10_00.22.08.png](https://www.nexusmc.cn/uploads/images/2c1yaj_pV9PQNe65R87y2.webp)

## API

请参阅 [API 类](https://github.com/LinsMinecraftStudio/SuperMines/blob/main/src/main/java/io/github/lijinhong11/supermines/api/SuperMinesAPI.java)
