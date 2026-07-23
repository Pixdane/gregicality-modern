# LV 阶段（32 EU/t）对比报告与迁移决策

> 状态：基于源码分析的初版报告。数据来源为 GTCE-TJ-Fork（1.12.2）、gregicality-TJFork（1.12.2 附属）
> 和 GregTech-Modern 7.5.3（1.20.1）的源码。

---

## 1. 电力建立

| 对比项 | GTCE | TJFork | GTM |
|--------|------|--------|-----|
| 蒸汽涡轮 LV | 蒸汽 60mB -> 32 EU/t | 同 GTCE | 蒸汽 640mB -> 32 EU/t（10t） |
| 柴油发电机 LV | Oil/Fuel/BioDiesel 等 | 同 GTCE | 同 GTCE + 高辛烷值汽油/火箭燃料等扩展 |
| 燃气涡轮 LV | Hydrogen/Methane/Ethylene 等 | 同 GTCE | 同 GTCE + 扩展燃料 |
| FE->EU 转换器 | 无 | 有（LV 可用，默认启用 FE->EU） | 有（ENERGY_CONVERTER，全等级） |

---

## 2. LV 机器

### 2.1 单方块机器

GTCE 和 GTM 都在 LV 注册了几乎全部加工机器（约 35+ 种）。GTM 额外有 Rock Crusher（碎石机）、Gas Collector（气体收集机），且 Extruder（挤压机）在 LV 就有（GTCE 从 MV 起）。

### 2.2 TJFork 新增的 LV 机器

| 机器 | 用途 | 合成特点 | 迁移决策 |
|------|------|---------|---------|
| **Cluster Mill（簇轧机）** | 箔片自动生产（替代手动 Bender） | 4 电机+电路+外壳；需 `BendingFoilsAutomatic=true` | 待定（取决于 GT6 工艺是否保留） |
| **Circuit Assembler（电路组装机）** | 专用电路组装 | 需高一等级电路 | **不需要**（GTM 已有 CIRCUIT_ASSEMBLER） |
| **Chemical Dehydrator（化学脱水机）** | 流体脱水/干燥/浓缩 | 双线圈+四线缆+机械臂 | **高优先级**（GTM 无等价，79 个配方依赖它） |
| **Simple Ore Washer（简易洗矿机）** | 快速廉价洗矿（7 EUt, 5t） | 仅 LV 版 | **不需要**（GTM 洗矿机已优化到 4 EUt 8t） |
| **Disassembler（拆解机）** | 拆解物品返还组件 | 机械臂+传感器+传送带+泵+电路 | **低优先级** |
| **Green House（温室）** | 植物种植自动化 | 玻璃+机械臂+电路 | **低优先级** |
| **Energy Converter** | EU<->FE 转换 | 全等级 | **不需要**（GTM 已有 ENERGY_CONVERTER） |

### 2.3 GTM 独有的 LV 辅助机器

Pump（LV~EV）、Miner（LV~HV）、World Accelerator（LV~UV）、Battery Buffer（全等级）、Charger（全等级）、Transformer（全等级）、Buffer（LV~HV）、Super Chest/Tank（LV~EV）、Fisher（LV~LuV）、Block Breaker（LV~EV）、Item Collector（LV~EV）

---

## 3. LV 电路

### 决策：直接用 GTM 的

| 对比项 | GTCE | TJFork | GTM（采用） |
|--------|------|--------|-----|
| LV 电路名 | Basic Circuit | Primitive Circuit | Electronic Circuit LV (T1) + Integrated Circuit LV (T2) |
| 电路板 | 涂层板（1层） | 涂层板 -> 基础电路板（2层） | 涂层板 -> 基础电路板（2层） |
| 板材料 | 钢板 | 锻铁板 | 钢板 |
| 电阻 | 纸+铜线+煤粉=3个 | 橡胶+铜细线+铜单线+煤粉=1个 | 树脂+纸+铜线+煤粉=2个 |
| 真空管 | 纸+玻璃管+铜线 | 同 GTCE | 纸+玻璃管+铜线+钢螺栓，组装机 2~4 个 |
| 二极管 | 锡细线+染料+镓=4个 | 铜细线+镓=1个 | 组装机：铜细线+砷化镓+玻璃=1~4个 |
| 化学蚀刻 | 无 | 过硫酸钠/氯化铁 | 过硫酸钠/三氯化铁 |
| 两条电路线 | 无 | Primitive + Electronic | T1 Electronic + T2 Integrated |

TJFork 独有但放弃迁移的内容：
- Refined SMD 系列 -> GTM 用基础/高级 SMD 替代
- 电路组装机作为独立机器 -> GTM 已有 CIRCUIT_ASSEMBLER
- 锻铁板替代钢板 -> 用 GTM 默认的钢板

---

## 4. EBF 与钢/铝生产

### 4.1 钢的生产

三个版本的 EBF 制钢配方 EUt 都超过 LV（GTCE 120 EUt/HV，GTM 64 EUt/MV），LV EBF 无法直接炼钢。LV 阶段用原始高炉（PBF）产钢。

| 方法 | GTCE | TJFork | GTM |
|------|------|--------|-----|
| PBF 铁+煤 | 1500t | 同 GTCE | 1800t |
| PBF 锻铁+煤 | 600t | 同 GTCE | 800t |
| EBF 铁+O2 | 500t, 120 EUt (HV) | 同 GTCE | 500t, 64 EUt (MV) |
| EBF 锻铁+O2 | 100t, 120 EUt (HV) | 同 GTCE | 300t, 64 EUt (MV) |

### 4.2 铝的生产

#### 决策：使用 TJFork 的拜耳法铝链，后续可调高产量数值

TJFork 的铝链是完整拜耳法 + 霍尔-埃鲁法工艺（AluminiumChain.java，125 行，13 步）：

```
铝土矿(Bauxite)
  │
  ├─[混合机 30 EUt LV]─> NaOH-Bauxite 溶液
  │     └─[流体加热器 30 EUt LV]─> 不纯 Al(OH)3 溶液
  │           └─[化学反应器 120 EUt HV]─> 红泥 + 纯 Al(OH)3 溶液
  │                 │
  │                 ├─[化学脱水机 120 EUt HV]─> Al(OH)3 固体 + 镓(副产)
  │                 │     └─[EBF 1100K 120 EUt HV]─> Alumina(氧化铝) + 水
  │                 │           └─[化学反应器+NaOH+HF 120 EUt HV]─> 冰晶石(Na3AlF6)
  │                 │                 └─[电解机 120 EUt HV]─> 铝粉×4 + NaF + AlF3 + O2
  │                 │                       └─[化学反应器 120 EUt HV]─> 冰晶石（闭环回收）
  │                 │
  │                 └─[混合机+HCl 120 EUt HV]─> 中和泥
  │                       └─[离心机 120 EUt HV]─> 红渣 + 铁REE氯化物 + 盐水
  │                             ├─[离心机 480 EUt IV]─> 稀土氯化物 + 氯化铁
  │                             └─[化学反应器+H2SO4 120 EUt HV]─> 硫酸氧钛
  │                                   └─[化学反应器+HCl 960 EUt IV]─> TiCl4 + 硫酸（闭环）
```

TJFork 铝链特点：
- 完整拜耳法：Bauxite -> NaOH 溶出 -> Al(OH)3 沉淀 -> Alumina 煅烧
- 霍尔-埃鲁法电解：Alumina + 冰晶石 -> 铝粉（冰晶石闭环回收）
- 红泥处理：红泥 -> 中和 -> 离心分离 -> 稀土 + 钛回收
- 镓副产：高效脱水时 7500 概率出镓
- 钛副产：红泥中的 TiO2 经硫酸+HCl 处理得 TiCl4
- 化学脱水机是关键机器（步骤 4 必须用）

电压需求：步骤 1-2 在 LV（30 EUt），步骤 3-10 在 HV（120 EUt），步骤 11/13 在 IV（480/960 EUt）

当前产量效率：39 Bauxite -> 4 铝粉（~0.1 铝/矿），后续可调高。

GTM 对比的铝链（不采用）：
- 浆料裂解法：Bauxite + 苏打 + 氯化钙 -> 浆料 -> 蒸汽裂解 -> 硫酸浸出 -> 铝粉×24
- 简化电解：Bauxite×15 直接电解 -> 铝粉×6（LV 32 EUt）
- 效率更高（32 Bauxite -> 24 铝粉）但工艺简化，不需要化学脱水机

选择 TJFork 铝链的理由：
1. 完整拜耳法工艺更真实，与 TJFork 其他材料处理链风格一致
2. 依赖化学脱水机（高优先级迁移机器），形成工艺闭环
3. 副产镓、钛（TiCl4）、稀土，与 TJFork 其他链（钛链、稀土链）衔接
4. 后续可调高产量数值以平衡效率

---

## 5. 化学脱水机（Chemical Dehydrator）

### 决策：高优先级迁移

| 属性 | 值 |
|------|-----|
| RecipeMap | `chemical_dehydrator` |
| IO 规格 | 0 物品输入 / 9 物品输出 / 2 流体输入 / 2 流体输出 |
| 等级 | LV~UXV（14 级） |
| 合成 | 双层加热线圈 + 四线缆 + 机械臂 + 齿轮 + 电路 + 外壳 |
| 配方总数 | 79 个，分布在 20+ 条链中 |

实际功能涵盖：溶液脱水成固体、流体浓缩、化学干燥、溶液蒸发结晶、核废料分离干燥、聚合物缩合。

电压分布：LV 14 个配方（18%）、HV 13 个（17%）、IV 11 个（14%）、UV 6 个（8%）。

GTM 无等价机器。迁移成本低：注册 recipe type + 机器 + 79 个配方。

---

## 6. 矿石处理链

| 对比项 | GTCE | TJFork | GTM |
|--------|------|--------|-----|
| 粉碎机 | 12 EUt，400t | 同 GTCE | 2 EUt，400t |
| 洗矿机 | 16 EUt | 同 GTCE | 4 EUt，8t（快速模式） |
| 热力离心机 | 60 EUt (MV) | 60 EUt (MV)，改产出 2x+3x副产 | LV 可用 |
| 化学浴 | 8 EUt | 同 GTCE | 16 EUt (VA[LV]) |
| Simple Ore Washer | 无 | 7 EUt, 5t（仅 LV） | 无（洗矿机已优化） |
| Poor/Rich/Pure 矿石 | 无 | 新增矿石变体 | 无 |

---

## 7. 存储系统

| 对比项 | GTCE | TJFork | GTM |
|--------|------|--------|-----|
| 桶（Drum） | 无 | 6 种 | **8 种**（采用） |
| 箱（Crate） | 无 | 6 种 | **7 种**（采用） |
| 超级箱/罐 | 无 | 无 | Super Chest/Tank（LV~EV） |
| 多方块储罐 | 无 | 无 | 3 种（木/铜/钢） |

---

## 8. 化学处理

| 对比项 | GTCE | TJFork | GTM |
|--------|------|--------|-----|
| 石油脱硫 | 化学反应器 | 同 GTCE | 16 EUt (LV) 可用 |
| 裂解 | 裂化器 (HV) | 同 GTCE | 轻度裂解 30 EUt / 中度裂解 16 EUt (LV) 可用 |
| 蒸馏塔 | 多方块 (HV) | 同 GTCE | 多方块 (不锈钢 HV) |
| 蒸馏室 | LV 可用 | 同 GTCE | LV 可用 |

---

## 9. LV 组件

| 组件 | GTCE/TJFork | GTM（采用） |
|------|-------------|-----|
| 电机 LV | 锡电缆×2+铜线×4+钢杆×2+磁化铁杆 | 同 |
| 活塞 LV | 钢板×3+锡电缆×2+钢杆×2+钢小齿轮+电机 | 同 |
| 传送带 LV | 橡胶板×6+锡电缆+电机×2 | 同 |
| 泵 LV | 锡螺栓+锡转子+青铜管+橡胶环×2+锡电缆+电机 | 同 |
| 机械臂 LV | 锡电缆×3+钢杆×2+电机×2+活塞+LV电路 | 同 |
| 传感器 LV | 钢板×4+黄铜杆+石英岩宝石+LV电路 | 同 |
| 发射器 LV | 黄铜杆×4+锡电缆×2+石英岩宝石+LV电路×2 | 同 |
| 场发生器 LV | 锇线+末影珍珠+电路×4 | **锰磷化物四线**+钢板×2+末影珍珠+电路×2 |

---

## 10. 向 MV 过渡

| 准备项 | 来源 |
|--------|------|
| 铝 | TJFork 拜耳法链（LV 步骤 1-2 + HV 主链） |
| MV 外壳 | 8 铝板 |
| MV 电路 | GTM Electronic Circuit MV（LV×2+二极管+Good Board） |
| MV 线缆 | 铜电缆（128V） |
| 变压器 | LV↔MV 变压器 |

---

## 11. LV 阶段迁移决策汇总

| 项目 | 决策 | 优先级 |
|------|------|--------|
| LV 电路 | 用 GTM 的 T1 Electronic + T2 Integrated | 已确认 |
| MV 电路 | 用 GTM 的 T1 Electronic MV + T2/T3 | 已确认 |
| 化学脱水机 | 迁移 | **高** |
| 拆解机 | 迁移 | **低** |
| 温室 | 迁移 | **低** |
| 簇轧机 | 待定（取决于 GT6 工艺决策） | 待定 |
| 电路组装机 | 不迁移（GTM 已有） | - |
| 简易洗矿机 | 不迁移（GTM 已优化） | - |
| 桶/箱 | 不迁移（GTM 已有 8 桶+7 箱） | - |
| Energy Converter | 不迁移（GTM 已有） | - |
| 铝产线 | 用 TJFork 拜耳法链，后续调高数值 | 已确认 |
| 银产线 | 无需额外处理（方铅矿脉直接含银矿） | - |
| 矿石处理 | 用 GTM 的（洗矿机已优化、热力离心 LV 可用） | 已确认 |
| 存储系统 | 用 GTM 的 | 已确认 |
| GT6 工艺 | 待讨论 | 待定 |
