# Gregicality Modern 迁移 TODO

目标：把 `gregicality-TJFork` 从 Minecraft 1.12.2 / GTCE TJ Fork 迁移到 Minecraft 1.20.1 / Forge / GTCEu Modern，并使用 Scala 作为主要实现语言。

原则：

- 不逐文件翻译旧代码；按功能模块重新实现。
- 先做最小可运行闭环，再逐步迁移内容。
- 优先迁移 Gregicality 自身核心玩法；跨模组集成后置。
- 每完成一个模块，都更新本文件状态和必要的迁移说明。

## 0. 迁移基线

- [x] 确认现代工程目标：Minecraft 1.20.1、Forge 47、GTCEu Modern、Scala 3。
- [x] 确认 mod id、包名、license、作者、版本号和发布名称。
- [x] 建立源模组功能索引，把旧 `gregicadditions` 包按模块归档：见 [source-module-index.md](source-module-index.md)。
- [x] 建立旧 API 到新 API 对照表：材料、物品、机器、配方、世界生成、集成：见 [api-migration-map.md](api-migration-map.md)。
- [ ] 标记不准备迁移、延后迁移、需要重新设计的旧功能。

## 1. GTCEu Modern Addon 接入

- [ ] 创建 Gregicality 的 GTCEu addon 入口。
- [ ] 接入 GTCEu 的材料注册生命周期。
- [ ] 接入 GTCEu 的机器注册生命周期。
- [ ] 接入 GTCEu 的 recipe type 和配方生成生命周期。
- [ ] 接入 datagen 入口，用于生成资源和数据。
- [ ] 建立一个最小测试闭环：材料、物品、机器、配方都能在游戏内出现。

## 2. 材料系统

- [ ] 迁移基础元素和流体材料。
- [ ] 迁移矿物材料和矿石生成所需材料。
- [ ] 迁移常规工程合金。
- [ ] 迁移核材料和同位素材料。
- [ ] 迁移超导材料。
- [ ] 迁移高阶聚合物材料。
- [ ] 迁移 Naquadah、Trinium、Taranium 等高阶 GT 材料链。
- [ ] 迁移光学、量子、宇宙、超因果相关材料。
- [ ] 检查所有材料颜色、图标集、形态、flags、熔点和组件定义。

## 3. TagPrefix 与自动生成物

- [ ] 映射旧 `OrePrefix` 到 GTCEu Modern `TagPrefix`。
- [ ] 确认哪些材料需要板、杆、螺丝、箔、线、齿轮、框架、转子等形态。
- [ ] 迁移圆片、双层板、金属外壳等 Gregicality 特有形态。
- [ ] 避免手写能由材料系统自动生成的普通物品。
- [ ] 检查自动生成物的 tag、语言文件、模型和配方输出。

## 4. 普通物品

- [ ] 迁移电路板、晶圆、芯片和电路部件。
- [ ] 迁移 SMD 元件：基础、高阶、生物、光学、宇宙、超因果等系列。
- [ ] 迁移磁共振电路系列。
- [ ] 迁移电池、电池外壳和储能组件。
- [ ] 迁移核废料和核处理副产物。
- [ ] 迁移工具、探矿工具、手泵、特殊扳手等行为物品。
- [ ] 迁移装甲、喷气背包、夜视镜等装备。
- [ ] 迁移光学、激光、生物培养、虫洞、奇异物质等特殊中间件。

## 5. 方块与外壳

- [ ] 迁移多方块外壳。
- [ ] 迁移机器外壳。
- [ ] 迁移反应堆外壳和核相关结构方块。
- [ ] 迁移聚变、真空、低温、导流、透明等特殊外壳。
- [ ] 迁移金属外壳和自动生成金属 casing。
- [ ] 迁移矿石方块。
- [ ] 迁移光纤、线缆和特殊管线类方块。
- [ ] 迁移模型、方块状态、loot table、本地化和材质。

## 6. Recipe Type 与配方基础设施

- [ ] 迁移 Cluster Mill recipe type。
- [ ] 迁移 Circuit Assembler recipe type。
- [ ] 迁移 Mass Fabricator 和 Replicator recipe type。
- [ ] 迁移 Electric Sieve recipe type。
- [ ] 迁移 Chemical Dehydrator、Chemical Plant 等化工 recipe type。
- [ ] 迁移大型机器 recipe type：Large Chemical Reactor、Large Mixer、Large Centrifuge 等。
- [ ] 迁移 Nuclear Reactor、Nuclear Breeder、Hot Coolant Turbine recipe type。
- [ ] 迁移 Stellar Forge、Plasma Condenser、Advanced Fusion、Electric Implosion recipe type。
- [ ] 建立 Scala 配方 helper，统一处理输入、输出、时长、电压、条件和保存。

## 7. 基础配方与机器合成

- [ ] 迁移机器外壳和多方块结构件配方。
- [ ] 迁移单方块机器合成配方。
- [ ] 迁移多方块控制器合成配方。
- [ ] 迁移组件配方：马达、泵、活塞、机械臂、传送带、传感器、发射器、场发生器。
- [ ] 迁移电路板、晶圆、芯片、SMD、组装线相关配方。
- [ ] 迁移电池、储能、充电器、电池缓冲器相关配方。
- [ ] 迁移拆解机和电爆压缩相关配方。

## 8. 材料处理链

- [ ] 迁移矿物处理和矿物副产物链。
- [ ] 迁移铝、铬、钡、锌、锆、硒、碘、铼等化工链。
- [ ] 迁移铂族泥处理链。
- [ ] 迁移钨纯化链。
- [ ] 迁移锂、钠、钾、氨、盐水等基础化工链。
- [ ] 迁移聚合物、PEEK、Zylon、富勒烯、纳米管等高分子链。
- [ ] 迁移 Naquadah、Trinium、Taranium 等高阶材料链。
- [ ] 迁移 UHV、Ultimate、Cosmic、Wormhole、Supracausal 等末期链。
- [ ] 迁移超导材料和超导 SMD 链。

## 9. 单方块机器

- [ ] 迁移高阶基础机器：电炉、研磨机、合金炉、电弧炉、组装机等。
- [ ] 迁移高阶化工机器：化学反应釜、电解机、离心机、化学浴、蒸馏机等。
- [ ] 迁移高阶加工机器：弯曲机、线材机、挤压机、锻造锤、激光雕刻机等。
- [ ] 迁移 Mass Fabricator、Replicator、Decay Chamber、Green House。
- [ ] 迁移 Disassembler。
- [ ] 迁移 Rock Breaker、World Accelerator、Pump、Air Collector。
- [ ] 迁移 Energy Converter、Diode、Transformer、Battery Buffer、Charger。

## 10. 基础多方块机器

- [ ] 迁移 Steam Grinder 和 Steam Oven。
- [ ] 迁移 Assembly Line 和 Large Circuit Assembly Line。
- [ ] 迁移 Large Chemical Reactor。
- [ ] 迁移 Large Mixer。
- [ ] 迁移 Large Centrifuge。
- [ ] 迁移 Large Electrolyzer。
- [ ] 迁移 Large Macerator、Large Sifter、Large Washing Plant。
- [ ] 迁移 Large Assembler、Large Extruder、Large Forge Hammer、Large Wiremill。
- [ ] 迁移 Large Laser Engraver。
- [ ] 迁移 Chemical Plant。

## 11. 高阶与特殊多方块

- [ ] 迁移 Electric Blast Furnace 覆盖逻辑。
- [ ] 迁移 Vacuum Freezer 覆盖逻辑。
- [ ] 迁移 Distillation Tower、Cracking Unit、Pyrolyse Oven 覆盖逻辑。
- [ ] 迁移 Large Combustion Engine、Extreme Diesel Engine、Large Turbine。
- [ ] 迁移 Industrial Primitive Blast Furnace。
- [ ] 迁移 Alloy Blast Furnace。
- [ ] 迁移 Advanced Distillation Tower。
- [ ] 迁移 Volcanus。
- [ ] 迁移 Cryogenic Freezer。
- [ ] 迁移 Large Rocket Engine。
- [ ] 迁移 Large Naquadah Reactor 和 Hyper Reactor。
- [ ] 迁移 Advanced Fusion Reactor。
- [ ] 迁移 Stellar Forge。
- [ ] 迁移 Qubit Computer 和 Qubit Hatch。
- [ ] 迁移 Battery Tower。
- [ ] 迁移 Mega Blast Furnace、Mega Distillation Tower、Mega Vacuum Freezer。

## 12. 核工业系统

- [ ] 迁移核反应堆。
- [ ] 迁移核增殖反应堆。
- [ ] 迁移气体离心机。
- [ ] 迁移热冷却液涡轮。
- [ ] 迁移核燃料、同位素、核废料和副产物处理。
- [ ] 迁移核材料 tooltip、安全提示和 JEI 显示。
- [ ] 迁移核反应堆文档和示意图。

## 13. 采矿、虚空矿机与油藏

- [ ] 迁移 Large Miner 系列。
- [ ] 迁移 Chunk Miner。
- [ ] 迁移 Steam Miner。
- [ ] 迁移 Void Miner 及其等级。
- [ ] 迁移 Void Miner 白名单和产物池。
- [ ] 迁移 Fluid Drilling Plant。
- [ ] 迁移油藏、流体矿床、抽取概率和同步逻辑。
- [ ] 迁移相关文档和 JEI/tooltip 信息。

## 14. 世界生成

- [ ] 迁移旧矿脉替换逻辑为现代 datapack/datagen 定义。
- [ ] 迁移 Gregicality 自定义矿脉。
- [ ] 迁移自定义矿石 filler 或等价机制。
- [ ] 迁移维度差异：主世界、下界、末地。
- [ ] 迁移 bedrock fluid 或现代等价油藏配置。
- [ ] 检查服务端与客户端资源同步。

## 15. 跨模组集成

- [ ] 迁移 JEI 显示和 recipe catalyst。
- [ ] 迁移 KubeJS 支持，替代旧 CraftTweaker 主路径。
- [ ] 评估是否保留 CraftTweaker 兼容。
- [ ] 迁移 AE2/AE2UEL 集成：ME Stocking Bus 等。
- [ ] 评估并迁移 Tinkers Construct 集成。
- [ ] 评估并迁移 Forestry / 蜜蜂系统。
- [ ] 评估并迁移 Ex Nihilo 筛子和资源碎块。
- [ ] 评估并迁移 Mystical Agriculture 作物与精华。
- [ ] 评估并迁移 OpenComputers 或现代替代模组集成。
- [ ] 迁移 The One Probe 或现代信息显示集成。

## 16. 客户端、UI 与显示

- [ ] 迁移机器 GUI 和进度条。
- [ ] 迁移多方块预览和层显示。
- [ ] 迁移中心监视器和屏幕类机器。
- [ ] 迁移 HUD 配置和显示。
- [ ] 迁移按键绑定。
- [ ] 迁移 tooltip、状态文本、错误提示和结构提示。
- [ ] 迁移音效和 recipe map sound。
- [ ] 迁移材质、模型、渲染器和特殊视觉效果。

## 17. 配置系统

- [ ] 迁移客户端配置。
- [ ] 迁移能源转换配置。
- [ ] 迁移 GT5U / GT6 风格功能开关。
- [ ] 迁移高阶机器注册开关。
- [ ] 迁移多方块维护配置。
- [ ] 迁移矿物处理、UU-Matter、钨处理、拆解机等玩法开关。
- [ ] 迁移跨模组集成开关。
- [ ] 迁移配置热同步或重启要求说明。

## 18. 网络、能力与持久化

- [ ] 迁移网络包注册。
- [ ] 迁移油藏列表同步。
- [ ] 迁移机器客户端状态同步。
- [ ] 迁移自定义 capability 或改用 GTCEu Modern 现有能力。
- [ ] 迁移世界存档数据。
- [ ] 检查服务端专用环境和客户端专用环境隔离。

## 19. 数据生成与资源

- [ ] 建立 blockstate datagen。
- [ ] 建立 item model datagen。
- [ ] 建立 block model datagen。
- [ ] 建立 loot table datagen。
- [ ] 建立 recipe datagen。
- [ ] 建立 tag datagen。
- [ ] 建立 language datagen。
- [ ] 建立 worldgen datagen。
- [ ] 建立 `existingFileHelper` 校验策略。
- [ ] 迁移英文、简中、繁中、俄文本地化。

## 20. 测试与验证

- [ ] 编译验证：Scala 和资源处理。
- [ ] datagen 验证：生成文件稳定且可加载。
- [ ] 客户端启动验证：能进入主菜单。
- [ ] 世界创建验证：能创建并进入新世界。
- [ ] JEI 验证：recipe type 和机器配方可见。
- [ ] GTCEu 机器放置验证：单方块和多方块可成型。
- [ ] 世界生成验证：矿脉、矿石、油藏按预期出现。
- [ ] 服务端启动验证：无客户端类加载错误。
- [ ] 存档重进验证：机器、网络、配置和世界数据稳定。

## 21. 文档与交接

- [ ] 维护迁移状态表。
- [ ] 记录每个旧模块对应的新实现路径。
- [ ] 记录放弃迁移或重新设计的功能及原因。
- [ ] 记录 GTCEu Modern API 使用约定。
- [ ] 记录 Scala 编码约定。
- [ ] 记录测试命令和验证结果。
- [ ] 迁移旧 `docs/multiblock/voidminer` 文档。
- [ ] 迁移旧核反应堆文档。

## 22. 暂缓清单

- [ ] 是否完整复刻旧 Forestry 蜜蜂系统。
- [ ] 是否完整复刻旧 OpenComputers 集成。
- [ ] 是否保留旧 CraftTweaker API。
- [ ] 是否复刻旧装甲和喷气背包。
- [ ] 是否复刻所有旧世界生成替换行为。
- [ ] 是否保留所有旧配置项名称和默认值。
- [ ] 是否把旧 Gregicality TJFork 的所有终局内容一次性迁完。
