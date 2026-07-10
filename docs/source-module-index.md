# 源模组功能索引

本文把旧源模组 `gregicadditions` 包按迁移模块归档，作为后续从
Minecraft 1.12.2 / GTCE TJ Fork 迁移到 Minecraft 1.20.1 / GTCEu Modern
的入口地图。

旧源根目录：

- 旧仓库：`../gregicality-TJFork`
- Java 包：`../gregicality-TJFork/src/main/java/gregicadditions`
- 资源根：`../gregicality-TJFork/src/main/resources`

本索引只回答“旧功能在哪、属于哪个迁移模块、后续应该先看哪些入口”。旧 API 到
GTCEu Modern API 的具体对应关系、保留/放弃/重设计决策会在后续 TODO 中单独记录。

粗略规模依据：旧 `gregicadditions` Java 包中最多的是 `machines/`、`recipes/`、
`jei/`、`integrations/`、`item/`，资源则主要集中在 `assets/gregtech/` 和
`assets/gtadditions/`。因此本文按“核心注册/材料/物品/方块/配方/机器/世界生成/客户端/集成”
归档，而不是按旧目录逐个平铺。

## 总体入口

| 入口 | 主要职责 | 迁移提示 |
| --- | --- | --- |
| `Gregicality.java` | 旧 Forge mod 入口，声明 `gtadditions` mod id、依赖、生命周期和各集成 proxy。 | 现代入口不逐行迁移；用它确认旧生命周期顺序和可选集成边界。 |
| `CommonProxy.java` / `ClientProxy.java` | 通用和客户端注册入口，串起物品、方块、流体、机器、配方、世界生成、客户端渲染。 | 迁移时拆到 GTCEu addon lifecycle、Forge event bus 和 datagen。 |
| `GAConfig.java` | 旧配置总表，覆盖客户端、能量转换、GT5U/GT6 兼容、集成、机器、多方块、装备、油藏等。 | 是判断功能开关和默认行为的重要来源。 |
| `GAValues.java` / `GAEnums.java` | 电压层级、mod id、枚举扩展、RecipeMap slot 修改等常量和反射逻辑。 | 现代版应优先用 GTCEu Modern 公开 API；旧反射只作行为线索。 |
| `GAEventHandler.java` / `GAUtility.java` / `utils/` | 事件处理、辅助函数、日志和结构检查工具。 | 迁移前先确认具体调用点，避免带入旧 1.12 工具层。 |

## 模块归档

| 迁移模块 | 旧包/文件 | 功能范围 | 初始迁移优先级 |
| --- | --- | --- | --- |
| 生命周期与注册 | `Gregicality.java`, `CommonProxy.java`, `ClientProxy.java`, `GAEventHandler.java`, `GAValues.java`, `GAEnums.java` | 旧 mod lifecycle、事件订阅、proxy、GTCE 扩展点、跨模块注册顺序。 | 基线。先用它建立现代 addon 接入。 |
| 配置系统 | `GAConfig.java` | 客户端显示、EU/FE 转换、GT5U/GT6 开关、高阶机器开关、集成开关、多方块参数、装备参数、油藏概率。 | 早期。先迁移影响注册和配方生成的服务端配置。 |
| 材料系统 | `GAMaterials.java`, `materials/` | 新元素、核同位素、流体材料、合金、聚合物、高阶材料、材料 flag、简单材料封装。 | 早期核心。先迁移材料和 flags，再迁移依赖它们的物品/配方。 |
| 流体注册 | `fluid/GAMetaFluids.java`, `GAMaterials.java` 中 `FluidMaterial` | 旧流体材料注册、tooltip、流体方块生成。 | 跟随材料系统。 |
| TagPrefix / 自动生成物 | `GAMaterials.java`, `recipes/categories/handlers/*`, `item/GADustItem.java`, `item/BasicMaterial.java`, `item/GAOredictItem.java` | 旧 `OrePrefix` handler、round、double plate、metal casing、nuclear compound、isotope compound、矿物变体和 oredict。 | 材料后置。需要映射到 GTCEu Modern `TagPrefix`。 |
| 普通物品与 meta item | `item/GAMetaItems.java`, `item/GAMetaItem.java`, `item/GAMetaItem2.java`, `item/behaviors/`, `item/components/`, `tools/` | 电路、SMD、晶圆、工具、手泵、特殊扳手、监视器插件、核废料、组件、行为物品。 | 中期。先迁移不依赖 UI/网络的静态物品。 |
| 装备和电物品 | `armor/`, `GAElectricItem.java`, `item/GAMetaArmor.java`, `item/GAMetaItems.java` 的 armor fields | 夜视镜、喷气背包、肌肉套装、QuarkTech 套装、电池包、能量行为。 | 中后期。需要现代能力和客户端行为重写。 |
| 方块、外壳和矿石 | `item/GAMetaBlocks.java`, `item/*Casing*.java`, `item/metal/`, `item/fusion/`, `blocks/`, `blocks/factories/` | 多方块外壳、机器外壳、反应堆/聚变/真空/低温外壳、自动金属 casing、矿石变体、爆炸物、简单方块。 | 早中期。材料后先迁移外壳和 datagen。 |
| 线缆和光纤 | `pipelike/cable/`, `pipelike/opticalfiber/`, `item/GAMetaBlocks.java` | 高阶电缆材料、光纤方块、qubit 网络、光纤 tile、渲染。 | 中后期。普通线缆和光纤/qubit 可拆开迁移。 |
| RecipeMap 与配方基础设施 | `recipes/GARecipeMaps.java`, `recipes/impl/`, `recipes/helper/`, `recipes/compat/crafttweaker/` | 自定义 recipe map、large recipe map、assembly line、nuclear/hot coolant/qubit builders、CraftTweaker bridge。 | 早中期。先迁移 recipe type，再迁移配方数据。 |
| 基础配方和机器合成 | `recipes/categories/`, `recipes/categories/machines/`, `recipes/MetalCasingRecipes.java`, `recipes/StagedRemovalRecipes.java` | meta item、casing、组件、机器和多方块合成、配方覆盖、阶段性移除。 | RecipeMap 后。 |
| 材料处理链 | `recipes/chain/` | 铝、钡、铬、锂、钠钾、盐水、铂族泥、钨、聚合物、PEEK、Zylon、富勒烯、纳米管、Naquadah、Trinium、Taranium、UHV/Ultimate/Cosmic/Wormhole/Supracausal 链。 | 中期核心。适合按文件逐链迁移。 |
| 机器注册总表 | `machines/GATileEntities.java` | 所有旧 MetaTileEntity id、机器数组、单方块、多方块、hatch、能源设备注册。 | 早期索引源。不要整体翻译，应按模块拆迁。 |
| 高阶单方块机器 | `machines/overrides/`, `machines/MetaTileEntityRockBreaker.java`, `machines/TileEntityWorldAccelerator.java`, `machines/SteamPump.java`, `machines/MetaTileEntitySolarSampler.java`, `machines/MetaTileEntityDrum.java`, `machines/TileEntityCrate.java` | 高阶基础机器覆写、Rock Breaker、World Accelerator、Pump/Air Collector 扩展、鼓、箱子、太阳流体采样器。 | 中期。优先高阶机器注册闭环。 |
| 能量转换与电力部件 | `machines/energy/`, `machines/energyconverter/`, `machines/multi/multiblockpart/*Energy*`, `machines/overrides/GAMetaTileEntityBatteryBuffer.java`, `machines/overrides/GAMetaTileEntityCharger.java` | EU/FE 转换器、二极管、变压器、高 amp 能源仓、电池缓冲器、充电器。 | 中后期。依赖现代能量能力设计。 |
| 基础多方块机器 | `machines/multi/simple/`, `machines/multi/steam/`, `machines/multi/TileEntityAssemblyLine.java`, `machines/multi/TileEntityAlloyBlastFurnace.java` | Large Chemical Reactor、Large Mixer、Large Centrifuge、Large Assembler 等大型机器，Steam Grinder/Oven，Assembly Line，Alloy Blast Furnace。 | 中期。适合按 recipe map 成组迁移。 |
| 高阶与特殊多方块 | `machines/multi/advance/`, `machines/multi/advance/hyper/`, `machines/multi/override/`, `machines/multi/mega/`, `machines/multi/MetaTileEntityStellarForge.java`, `machines/multi/MetaTileEntityBatteryTower.java`, `machines/multi/qubit/` | Advanced Fusion、Volcanus、Cryogenic Freezer、Large Rocket Engine、Large Naquadah/Hyper Reactor、GTCE 多方块覆写、Mega 系列、Stellar Forge、Battery Tower、Qubit Computer。 | 后期。很多需要重新设计结构和 UI。 |
| 核工业系统 | `machines/multi/nuclear/`, `machines/multi/impl/HotCoolant*`, `recipes/impl/nuclear/`, `recipes/categories/handlers/NuclearHandler.java`, `recipes/chain/NuclearChain.java`, `materials/RadioactiveMaterial.java`, `materials/IsotopeMaterial.java` | 核反应堆、核增殖、气体离心机、热冷却液涡轮、同位素/核废料、热冷却液 recipe map。 | 专项迁移。需要先写现代设计文档。 |
| 采矿、虚空矿机和油藏 | `machines/multi/miner/`, `machines/multi/drill/`, `worldgen/PumpjackHandler.java`, `worldgen/DimensionChunkCoords.java`, `network/IPSaveData.java`, `network/MessageReservoirListSync.java`, `recipes/categories/handlers/VoidMinerHandler.java` | Large/Chunk/Steam/Void Miner、Fluid Drilling Plant、油藏生成、油藏同步和存档数据。 | 中后期。世界数据和网络要一起设计。 |
| 世界生成 | `worldgen/`, `src/main/resources/assets/gregtech/worldgen/` | 自定义矿脉、矿石 filler、石头生成事件、bedrock fluid/油藏配置。 | 中期。现代版应转成 datapack/datagen。 |
| 网络、能力与持久化 | `network/`, `capabilities/`, `capabilities/impl/` | 包注册、按键包、结构包、插件同步、油藏同步、世界存档、配方逻辑 trait、多 recipe/qubit 能力。 | 按依赖迁移。不要提前搬旧能力层。 |
| 客户端、UI 与显示 | `client/`, `widgets/`, `gui/GAGuiTextures.java`, `input/`, `sound/RecipeMapSoundFixer.java` | 模型重贴图、prospecting map、结构预览、监视器 widgets、HUD、按键绑定、GUI 纹理、进度条和声音修正。 | 中后期。先迁移服务端闭环。 |
| JEI 显示 | `jei/`, `jei/multi/`, `recipes/compat/jei/`, `integrations/jei/` | 多方块信息页、recipe wrapper/category、fusion/central monitor/void miner/nuclear/mega 等 JEI 页面。 | 配方和机器后置。现代可评估 EMI/JEI 路线。 |
| The One Probe | `theoneprobe/` | 能源转换、二极管、多 recipe、qubit 信息显示。 | 后置集成。 |
| AE2 集成 | `machines/multi/multiblockpart/appeng/MetaTileEntityMEStockingBus.java`, `coremod/hooks/AppliedEnergistics2Hooks.java` | ME Stocking Bus、多方块部件和旧 coremod hook。 | 后置。现代 AE2 API 需要重新设计。 |
| Forestry / 蜜蜂集成 | `integrations/bees/`, `integrations/bees/alveary/`, `integrations/bees/effects/`, `integrations/bees/mutation/`, `recipes/compat/ForestryCompat.java`, `src/main/resources/assets/forestry/` | GT bees、蜂巢、蜂巢 GUI、蜂产品、Forestry 机器配方桥。 | 暂缓候选。TODO 已要求单独评估。 |
| Ex Nihilo 集成 | `integrations/exnihilocreatio/` | Electric/Steam Sieve、Steam Rock Breaker、碎块/小石子、筛子掉落、recipe map。 | 后置集成。 |
| Mystical Agriculture 集成 | `integrations/mysticalagriculture/` | 作物、精华、种子、模型工厂、MA 配方移除/oredict。 | 暂缓候选。 |
| Tinkers Construct 集成 | `integrations/tconstruct/` | GT 材料进 Tinkers 工具、熔炼、合金、玻璃处理、casting recipe。 | 暂缓候选。 |
| OpenComputers 集成 | `integrations/opencomputers/` | 机器/cover/多方块 OC driver 和 environment。 | 暂缓候选。现代替代需要评估。 |
| FE 兼容 | `integrations/FECompat/` | EU/FE wrapper 和 energy provider。 | 与能量转换一起评估。 |
| Coremod/ASM hooks | `coremod/`, `coremod/transform/`, `coremod/hooks/` | 旧 ASM 修改 GTCE/AE2/XNet/RS/MBT 行为。 | 默认不直接迁移。先判断现代 API 是否已有等价扩展点。 |
| Covers | `covers/` | Digital Interface 和 Infinite Water cover。 | 后置，依赖 cover API。 |
| 旧文档 | `docs/multiblock/voidminer/`, `docs/nuclear reactor/` | 虚空矿机和核反应堆旧说明图文。 | 对应 TODO 21 的文档迁移项。 |

## 资源归档

| 资源路径 | 内容 | 对应模块 |
| --- | --- | --- |
| `assets/gtadditions/lang/` | 旧本地化文件。 | 所有注册物、机器和 tooltip；后续 language datagen 的来源。 |
| `assets/gtadditions/blockstates/`, `assets/gtadditions/models/`, `assets/gtadditions/textures/` | Gregicality 自身方块、物品、装甲、casing、线缆材质。 | 方块、外壳、装备、物品、光纤。 |
| `assets/gtadditions/sounds/` | 旧自定义声音资源。 | 客户端/recipe map sound。 |
| `assets/gregtech/blockstates/`, `assets/gregtech/models/`, `assets/gregtech/textures/` | 挂在 GregTech namespace 下的 meta item、机器、GUI、材料集、工具、自动生成资源。 | GTCE 扩展内容、物品、机器、GUI。迁移时需决定是否仍借用 `gregtech` namespace。 |
| `assets/gregtech/worldgen/overworld`, `assets/gregtech/worldgen/nether`, `assets/gregtech/worldgen/end` | 旧 GTCE worldgen JSON。 | 世界生成/datapack/datagen。 |
| `assets/forestry/` | Forestry 蜜蜂/蜂巢相关模型和材质。 | Forestry 集成。 |
| `mcmod.info`, `pack.mcmeta`, `gregicality_at.cfg` | 旧 mod metadata、资源包信息、access transformer。 | 现代 metadata 和访问需求评估。 |

## 迁移顺序建议

1. 生命周期与 GTCEu addon 接入：从 `Gregicality.java`、`CommonProxy.java`、`GATileEntities.java` 和 `GARecipeMaps.java` 抽出现代注册阶段。
2. 材料和 TagPrefix：先迁移 `GAMaterials.java` 与 `materials/` 的材料/flag，再建立旧 `OrePrefix` 到现代 `TagPrefix` 的对照。
3. 静态内容闭环：迁移基础外壳、普通 meta item、一个 recipe type、一个机器和 datagen，形成最小游戏内可见闭环。
4. 配方链按文件迁移：从 `recipes/categories/` 和 `recipes/chain/` 分批迁移，避免混入暂缓集成。
5. 大机器和专项系统：按 `machines/multi/simple`、`machines/multi/nuclear`、`machines/multi/miner`、`machines/multi/advance` 分组推进。
6. 集成和客户端显示后置：JEI/TOP/AE2/Forestry/Ex Nihilo/Mystical/Tinkers/OpenComputers 在核心玩法稳定后再逐项评估。

## 下一批索引任务

- 建立旧 API 到新 API 对照表：材料、物品、机器、配方、世界生成、集成。
- 给每个模块标注迁移策略：保留、延后、重设计、不迁移。
- 为材料、RecipeMap、机器注册各做一张细粒度清单，记录旧 id/name、旧源文件、新实现路径和验证状态。
