# 旧 API 到现代 API 对照表

本文记录旧 `gregicadditions` API、注册入口和实现习惯到现代工程的初始映射。
它用于指导后续实现，不是最终的保留/放弃/重设计决策表。

迁移基线：

- 旧端：Minecraft 1.12.2 / Forge / GTCE TJ Fork / `gregicadditions`。
- 新端：Minecraft 1.20.1 / Forge 47.3.0 / GTCEu Modern 7.5.3 / Scala 3。
- 当前工程使用 `net.neoforged.moddev.legacyforge` 构建 Forge 1.20.1，因此 NeoForge
  primer 只作为现代迁移概念参考；落地 API 以 Forge 47 和 GTCEu 7.5.3 源码为准。

## 现代入口原则

| 领域 | 现代入口 | 迁移约束 |
| --- | --- | --- |
| Forge 注册 | `DeferredRegister` 或 `RegisterEvent`，注册在 mod event bus。 | 不恢复旧 1.12 的散落 `RegistryEvent.Register<T>` 风格；普通 Forge 对象优先延迟注册。 |
| GTCEu addon | `@GTAddon` + `IGTAddon`。 | addon 提供 `GTRegistrate getRegistrate()`，并在 mod class 调用 `GTRegistrate#registerRegistrate()`。 |
| GTCEu 内容注册 | `IGTAddon` hooks：`initializeAddon`、`registerTagPrefixes`、`registerElements`、`registerSounds`、`registerCovers`、`registerRecipeCapabilities`、`registerWorldgenLayers`、`registerVeinGenerators`、`registerIndicatorGenerators`。 | 旧 `CommonProxy` 的集中注册要拆到明确生命周期；不要把旧 proxy 当作现代架构照搬。 |
| 材料注册 | `MaterialRegistryEvent`、`MaterialEvent`、`PostMaterialEvent`。 | `IGTAddon#registerMaterials()` 在 GTCEu 7.5.3 中已标记 deprecated，后续材料不走这个入口。 |
| 数据生成 | Forge `GatherDataEvent`，GTCEu `GTRegistrate` 也会挂载 datagen provider。 | 资源分成 `assets/<modid>/` 和 `data/<modid>/`；旧运行时代码生成资源的做法要迁到 datagen。 |
| 能力系统 | Forge `RegisterCapabilitiesEvent`，GTCEu `GTCapability` 已注册常用机器/能量/工作能力。 | 先复用 GTCEu 能力和 `MetaMachine` 同步机制；只有确实缺口才新增 capability。 |

## 生命周期与注册

| 旧 API / 模式 | 现代 API / 模式 | 迁移说明 |
| --- | --- | --- |
| `Gregicality.java` 的 `@Mod`、`@SidedProxy`、`FMLPreInitializationEvent`、`FMLInitializationEvent`。 | Forge 1.20.1 `@Mod` 构造器、mod event bus、`FMLCommonSetupEvent`。 | 现代无 `SidedProxy` 主路径；客户端内容用 dist-safe event subscriber 或明确 client setup。 |
| `CommonProxy#preInit/init/postInit` 串联材料、机器、配方、世界生成。 | 按 GTCEu addon hooks、Forge 注册事件、datagen 事件拆分。 | 旧顺序只用来理解依赖，不迁移为一个新的全局 proxy。 |
| `RegistryEvent.Register<Item/Block/...>`。 | Forge `DeferredRegister` / `RegisterEvent`；GTCEu 内容优先 `GTRegistrate`。 | 普通 Forge 注册用 Forge API，GTCEu 机器/方块/物品用 GTCEu builder。 |
| `GameRegistry.registerTileEntity`、旧 tile entity id。 | `BlockEntityType` registry；GTCEu `MachineDefinition` + `MetaMachineBlockEntity`。 | GTCEu 机器不要手写独立 TE 注册，除非是非 GTCEu 方块实体。 |
| 旧 numeric meta id、`GregTechAPI.registerMetaTileEntity(id, ...)`。 | `ResourceLocation` registry name、`MachineDefinition`、`GTRegistrate.machine/multiblock`。 | 不保留数字 id 作为主键；最多在迁移表里记录旧 id 方便追踪。 |
| Access Transformer / coremod hook 补 API 缺口。 | 公开 Forge/GTCEu API；必要时再评估 mixin 或 AT。 | 默认不直迁 `coremod/`，先判断现代 API 是否已有扩展点。 |

## 材料

| 旧 API / 概念 | 现代 API / 概念 | 迁移说明 |
| --- | --- | --- |
| `GAMaterials.java` 中的 `IngotMaterial`、`DustMaterial`、`FluidMaterial`、`GemMaterial` 等子类构造。 | `Material.Builder(ResourceLocation)`，链式声明 `ingot()`、`dust()`、`fluid()`、`gem()`、`ore()` 等属性。 | 现代材料以 builder + property 组合表达，不按旧材料子类重建类型层级。 |
| 旧材料 numeric id。 | 材料 registry + namespaced `ResourceLocation`。 | id 改为 `gregicality:<material_name>`；旧数字只保存在迁移清单或注释中。 |
| `Material.MatFlags`、自定义 flag 修改。 | `MaterialFlags`、`PropertyKey`、材料 property classes。 | 先迁移影响生成物、配方和机器行为的 flags/properties；纯显示 flag 后置校对。 |
| `Element.valueOf`、反射/枚举扩展。 | GTCEu `Element`、`GTElements`、addon `registerElements()` 或元素注册事件。 | 不再反射改枚举；新元素按 GTCEu 公开入口声明。 |
| `RadioactiveMaterial`、`IsotopeMaterial`、`NuclearMaterial` 等旧包装。 | `Material` + `HazardProperty` / 自定义 property / tooltip 与 recipe condition。 | 核工业材料先记录行为需求，再决定是否需要新 property。 |
| 旧流体材料和 `GAMetaFluids`。 | `Material.Builder#fluid(...)`、`FluidStorageKeys`、`GTRegistrate#createFluid`。 | 跟随材料迁移；独立非材料流体才考虑直接 Forge/Registrate fluid。 |
| `MaterialStack` / 旧 composition。 | GTCEu `MaterialStack`、builder composition API。 | 化学链配方依赖 composition，迁移时要和材料处理链一起核对。 |

## TagPrefix 与自动生成物

| 旧 API / 概念 | 现代 API / 概念 | 迁移说明 |
| --- | --- | --- |
| `OrePrefix`。 | `TagPrefix`。 | 旧 prefix handler 要拆成 `TagPrefix` 生成规则、material flags 和配方 datagen。 |
| `OreDictUnifier`、`OreDictionary`。 | `ChemicalHelper`、`TagKey`、Forge/Minecraft tags。 | 旧 oredict 名称不再作为主协议；统一迁到 tags 与 GTCEu material/prefix 查询。 |
| `OrePrefix.runMaterialHandlers`。 | addon `registerTagPrefixes()` + GTCEu prefix 初始化。 | 不恢复全局 handler 扫描；每个 Gregicality 特有形态要有明确 prefix 定义。 |
| `GADustItem`、`GAOredictItem`、`BasicMaterial`。 | `TagPrefixItem`、`ItemMaterialInfo`、`MaterialEntry`。 | 能由 `TagPrefix` 自动生成的 dust/plate/rod 等不要手写普通 item。 |
| round、double plate、metal casing、nuclear compound、isotope compound。 | 自定义 `TagPrefix`，或普通 `GTRegistrate.item/block`。 | 若物品语义只是“材料形态”，优先 `TagPrefix`；若有特殊行为，改为普通 item/block。 |
| 手工 oredict/语言/模型拼装。 | datagen tags、lang、models。 | 每个 prefix 迁移时同步定义生成资源策略。 |

## 物品、方块与流体

| 旧 API / 概念 | 现代 API / 概念 | 迁移说明 |
| --- | --- | --- |
| `MetaItem`、`MetaValueItem`、`GAMetaItem`、`GAMetaItem2`。 | `GTRegistrate.item`、`ComponentItem`、`IComponentItem`、`IItemComponent`。 | 不复刻旧 meta item 大表；按功能分组注册独立 registry item。 |
| `ArmorMetaItem`、旧电物品行为。 | GTCEu item components、`ElectricStats`、现代 armor item/component。 | 装备和电池类先确认现代能力与客户端行为，再迁移。 |
| `GAMetaBlocks`、`VariantItemBlock`、旧 casing enum。 | `GTRegistrate.block`、material casing collection、普通 block/item builder。 | 多方块外壳优先按现代 block registry 和 datagen 建立。 |
| `GAMetaFluids`、流体方块手工注册。 | 材料流体、`GTRegistrate#createFluid`、Forge fluid registry。 | 材料流体随 `Material` 注册；非材料流体才单独建 builder。 |
| `ItemMaterialInfo` 手工绑定与回收逻辑。 | GTCEu `ItemMaterialData`、`ItemMaterialInfo`、`MaterialEntry`。 | 需要回收/分解的 Gregicality item 在注册或 datagen 阶段补 material info。 |
| 旧 `assets/gregtech` 和 `assets/gtadditions` 下的模型、材质、语言。 | `assets/gregicality` + 必要的 `data/gregicality`，或明确选择兼容 `gtceu/gregtech` namespace。 | 新内容默认使用本 mod namespace；借用旧 namespace 必须有兼容理由。 |

## 机器

| 旧 API / 概念 | 现代 API / 概念 | 迁移说明 |
| --- | --- | --- |
| `MetaTileEntity`。 | `MetaMachine`。 | 机器逻辑从旧 MTE 迁到现代 machine class、trait 和 recipe logic。 |
| `SimpleMachineMetaTileEntity`、`SimpleGeneratorMetaTileEntity`。 | `SimpleTieredMachine`、`SimpleGeneratorMachine`、`WorkableTieredMachine`。 | 高阶单方块机器优先使用 GTCEu 现成 tiered builder。 |
| `GATileEntities.java` 全局注册大表。 | `MachineDefinition`、`GTRegistrate.machine(...)`、`GTRegistrate.multiblock(...)`。 | 按模块拆分注册表；旧文件只作 id/name/分组索引。 |
| `RecipeMap` 绑定机器工作逻辑。 | `MachineBuilder#recipeType`、`recipeModifier`、`RecipeLogic`、`MachineTrait`。 | 简单机器直接绑定 `GTRecipeType`；特殊机器用 recipe modifier 或自定义 logic。 |
| 旧多方块 structure pattern。 | `MultiblockMachineBuilder#pattern`、`FactoryBlockPattern`、`Predicates`、`PartAbility`。 | 多方块迁移时同时写结构、shape info、part ability 和 tooltip。 |
| 旧 overlay、front facing、render code。 | GTCEu model helpers、`workableTieredHullModel`、dynamic render helper。 | 先用现成 GTCEu 模型闭环，特殊渲染后置。 |

常见旧机器到现代入口的初始对应：

| 旧功能 | 现代可用入口 | 迁移状态提示 |
| --- | --- | --- |
| 高阶基础单方块机器。 | `GTMachines.registerSimpleMachines`、`MachineDefinition[]`。 | 先确认 GTCEu 是否已有对应 tier；缺的再补 Gregicality 定义。 |
| Rock Breaker。 | GTCEu 已有 `RockCrusherMachine` / rock crusher recipe 路线；是否复刻旧 Rock Breaker 行为待确认。 | 需要行为比对，不能只按名字迁移。 |
| World Accelerator。 | `WorldAcceleratorMachine`。 | 可优先复用现代实现，再补 Gregicality 配方/等级差异。 |
| Pump / Air Collector。 | `PumpMachine`，必要时自定义机器。 | Pump 可复用；Air Collector 需确认现代是否已有等价行为。 |
| Miner / Large Miner / Void Miner。 | `MinerMachine`、`LargeMinerMachine`、`BedrockOreMinerMachine`。 | Void Miner 产物池和白名单要单独设计。 |
| Fluid Drilling Plant。 | `FluidDrillMachine`、bedrock fluid definitions。 | 和油藏/worldgen/存档同步一起迁移。 |
| Transformer / Diode / Battery Buffer / Charger / Energy Converter。 | GTCEu `TRANSFORMER`、battery buffer、charger、converter 相关 definitions。 | 先复用现代能量能力；旧 FE/EU wrapper 后置评估。 |
| Assembly Line / Large Mixer / Large Chemical Reactor 等。 | `GTMultiMachines`、`GTMachines`、`MultiblockMachineBuilder`。 | 现代已有者优先扩展配方；Gregicality 特有结构再新建。 |
| Central Monitor。 | `CentralMonitorMachine`、monitor capabilities。 | UI/同步复杂，后置。 |
| AE2 Stocking Bus/Hatch。 | GTCEu AE2 integration 中的 ME stocking part machine。 | 作为集成模块迁移，不进入核心闭环。 |

## Recipe Type 与配方

| 旧 API / 概念 | 现代 API / 概念 | 迁移说明 |
| --- | --- | --- |
| `RecipeMap`。 | `GTRecipeType`。 | 每个旧 recipe map 迁移为现代 recipe type，设置 IO 上限、EU IO、UI、声音和 category。 |
| `SimpleRecipeBuilder`、`LargeRecipeBuilder`、`CircuitAssemblerRecipeBuilder`、`NuclearReactorBuilder`。 | `GTRecipeBuilder` + recipe capabilities + 自定义 recipe condition/data。 | 先用通用 builder；只有旧 builder 的额外语义确实存在时才新增 helper。 |
| `RecipeMap#setSlotOverlay`、progress bar、sound。 | `GTRecipeType#setSlotOverlay`、`setProgressBar`、`sound`。 | UI 设置可直映射，但材质路径要按现代 assets/datagen 整理。 |
| item/fluid/EU/CWU 输入输出。 | `ItemRecipeCapability`、`FluidRecipeCapability`、`EURecipeCapability`、`CWURecipeCapability`。 | 不手写 JSON 字符串；用 `GTRecipeBuilder#inputItems`、`inputFluids`、`EUt`、`duration` 等 API。 |
| `RecipeHandler`、`recipes/categories`、`recipes/chain` 静态注册。 | addon `addRecipes(Consumer<FinishedRecipe>)`、datagen recipe providers。 | 配方迁移按链路分批；每批要有稳定 recipe id。 |
| 旧 staged removal / remove recipes。 | addon `removeRecipes(Consumer<ResourceLocation>)` 或 Forge/GTCEu recipe removal 数据。 | 删除 vanilla/GTCEu 配方必须单列原因，避免静默破坏整合包兼容。 |
| CraftTweaker compat。 | GTCEu KubeJS integration、`GregTechKubeJSPlugin` 暴露的 builders/bindings。 | TODO 已倾向 KubeJS 替代旧 CT 主路径；CT 是否保留放到后续决策表。 |

## 世界生成

| 旧 API / 概念 | 现代 API / 概念 | 迁移说明 |
| --- | --- | --- |
| `assets/gregtech/worldgen/*` 旧 GTCE worldgen JSON。 | GTCEu ore vein data / datagen，`GTOreDefinition`。 | 迁移为 namespaced datapack 数据，必要时用 datagen 生成。 |
| `WorldGenRegister`、自定义矿脉注册。 | addon `registerOreVeins()` + `GTOres.create(ResourceLocation, Consumer<GTOreDefinition>)`。 | ore vein 用 builder 配置 cluster size、density、weight、layer、dimensions、height range、biomes、generator、indicators。 |
| `GABlockFiller`、`GAFillerUtils`、旧 filler。 | `IWorldGenLayer`、`VeinGenerator`、GTCEu ore block/material 映射。 | 旧 filler 若只是矿石层规则，映射到 layer/generator；特殊替换逻辑需要单独设计。 |
| `StoneGenEvents`。 | datapack worldgen / Forge biome modifier / GTCEu worldgen layer。 | 不优先迁移事件式石头替换；先找数据驱动等价机制。 |
| `PumpjackHandler`、油藏概率和维度配置。 | `BedrockFluidDefinition#builder(ResourceLocation)`、bedrock fluid veins。 | 油藏和 Fluid Drilling Plant、网络同步、世界存档要成组迁移。 |
| Void Miner 白名单和产物池。 | GTCEu ore/bedrock ore definitions、机器 recipe/custom logic。 | 虚空矿机不等同普通世界矿脉，需要独立产物池设计。 |

## 网络、能力与持久化

| 旧 API / 概念 | 现代 API / 概念 | 迁移说明 |
| --- | --- | --- |
| `SimpleNetworkWrapper`、`IMessage` 包。 | Forge 1.20 networking API，或 GTCEu/LDLib 已有同步机制。 | 机器状态优先走 `MetaMachine` synced fields/trait；只有跨系统数据才新增网络包。 |
| `WorldSavedData`。 | Minecraft `SavedData`。 | 油藏、全局进度等需要世界持久化的数据再迁移。 |
| 自定义 `capabilities/`。 | Forge `RegisterCapabilitiesEvent`、GTCEu `GTCapability`。 | 先复用 `IEnergyContainer`、`IWorkable`、`RecipeLogic`、monitor/laser/optical 等能力。 |
| 旧客户端按键和 HUD 包。 | Forge client events + scoped packets。 | 客户端功能后置，先保证服务端专用环境不加载 client 类。 |

## 跨模组集成

| 旧集成 | 现代 API / 路线 | 迁移说明 |
| --- | --- | --- |
| JEI recipe/category/multiblock info。 | JEI 1.20.1 runtime、GTCEu `integration/jei`。 | 当前工程 dev runtime 有 JEI；核心 recipe type 稳定后再补显示和 catalysts。 |
| The One Probe。 | GTCEu `integration/top` 或 Jade integration。 | 现代生态可能偏 Jade；先记录旧显示信息，再决定 TOP/Jade 目标。 |
| AE2/AE2UEL。 | GTCEu AE2 integration、现代 AE2 API。 | ME Stocking Bus/Hatch 优先映射到 GTCEu 现有 machine part。旧 coremod hook 不直迁。 |
| CraftTweaker。 | KubeJS 主路径，GTCEu `GregTechKubeJSPlugin` 暴露材料、prefix、机器、配方、worldgen bindings。 | 除非后续明确需要 CT 兼容，否则旧 CT API 只作脚本功能参考。 |
| Forestry / bees。 | 1.20 可用 Forestry 等价 API 待确认。 | 暂缓候选；旧蜜蜂系统规模大，需单独保留/重设计决策。 |
| Ex Nihilo。 | 对应现代 Ex Nihilo fork API 待确认。 | Electric/Steam Sieve 可先作为 Gregicality 机器保留，掉落/碎块集成后置。 |
| Mystical Agriculture。 | 现代 MA API。 | 作物/精华属于集成内容，不进入最小核心闭环。 |
| Tinkers Construct。 | TConstruct 1.20 API。 | 材料进工具/熔炼/casting 需要现代 API 复查。 |
| OpenComputers。 | 现代替代通常不是同一 API。 | 默认不直迁；需要用户后续决定目标替代模组。 |
| FE compat。 | GTCEu `FeCompat`、energy converter machines、Forge energy capability。 | 能量转换先走现代 GTCEu/Forge 能力，不复刻旧 wrapper 结构。 |

## 不要直接迁移的旧机制

| 旧机制 | 处理原则 |
| --- | --- |
| numeric meta id 作为机器/物品主身份。 | 改为 registry name；旧 id 只做追踪字段。 |
| 旧 oredict 名称。 | 改为 tags + `ChemicalHelper`。 |
| 旧 `CommonProxy`/`ClientProxy` 总线式架构。 | 拆到 mod event bus、addon hooks、datagen provider 和 client-only setup。 |
| 旧 ASM/coremod 修改 GTCE、AE2、XNet、RS 等行为。 | 先找现代公开 API；确实无 API 再写设计说明评估 mixin/AT。 |
| 旧 `RecipeMap` 大量反射或 slot 修改。 | 用 `GTRecipeType` builder 和 UI API 明确配置。 |
| 旧资源挂 `assets/gregtech` 的惯性。 | 默认迁到 `gregicality` namespace；只有兼容旧 GTCEu/GT 资源查找时才保留。 |

## 后续细化表

这张表只建立 API 层映射。后续仍需要分别建立：

- 材料细表：旧材料名、旧 id、旧类型、现代 `Material.Builder` 路径、flags/properties、验证状态。
- prefix 细表：旧 `OrePrefix`、现代 `TagPrefix`、生成 item/block、tag、配方 handler。
- recipe type 细表：旧 `RecipeMap`、现代 `GTRecipeType`、IO、UI、声音、机器绑定。
- 机器细表：旧 MTE class/id、现代 machine definition、是否复用 GTCEu、是否重设计。
- 集成决策表：保留、延后、不迁移、需要替代模组或重新设计。

## 已查证来源

- 本工程版本：
  - `gradle/libs.versions.toml`：Minecraft `1.20.1`，Forge `47.3.0`。
  - `gradle/deps.versions.toml`：GTCEu `7.5.3`，JEI `15.20.0.133`。
  - `build.gradle.kts`：`modImplementation(deps.gtceu)`，JEI 仅作为 dev runtime。
- GTCEu 7.5.3 sources jar：
  - `/Users/pixdane/.gradle/caches/modules-2/files-2.1/com.gregtechceu.gtceu/gtceu-1.20.1/7.5.3/e6d5064d3c2a46ef50ada9a18ad77edbcf40c643/gtceu-1.20.1-7.5.3-sources.jar`
  - 重点类：`IGTAddon`、`GTRegistrate`、`MaterialEvent`、`MaterialRegistryEvent`、`TagPrefix`、`Material`、`GTRecipeType`、`GTRecipeBuilder`、`GTMachines`、`GTOreDefinition`、`GTOres`、`GTCapability`、`GregTechKubeJSPlugin`。
- Forge 1.20.1 docs：
  - Registries: <https://docs.minecraftforge.net/en/1.20.1/concepts/registries/>
  - Datagen: <https://docs.minecraftforge.net/en/1.20.1/datagen/>
  - Capabilities: <https://docs.minecraftforge.net/en/1.20.1/datastorage/capabilities/>
- NeoForge primer:
  - <https://docs.neoforged.net/primer/docs/>
  - 本文只使用它作为跨版本变化索引；当前工程仍按 Forge 47.3.0 API 落地。
