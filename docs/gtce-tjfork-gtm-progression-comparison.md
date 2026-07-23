# GTCE / TJFork / GTM 7.5.3 电压流程对比报告

> 状态：基于源码分析的初版报告。数据来源为 GTCE-TJ-Fork（1.12.2）、gregicality-TJFork（1.12.2 附属）
> 和 GregTech-Modern 7.5.3（1.20.1）的源码。后续按电压阶段单独讨论时再逐段细化。

本文档按电压等级对比三套实现的游戏流程，覆盖维度包括：电路体系、线缆/材料、矿石/世界生成、
机器（单方块+多方块）、能量与燃料、产线转折，以及 TJFork 完全更改的配方链。

---

## 0. 电压体系总览

| 段位 | EU/t | GTCE (10级) | TJFork (15级) | GTM (15级) |
|------|------|-------------|---------------|------------|
| 0 | 8 | ULV | ULV | ULV |
| 1 | 32 | LV | LV | LV |
| 2 | 128 | MV | MV | MV |
| 3 | 512 | HV | HV | HV |
| 4 | 2048 | EV | EV | EV |
| 5 | 8192 | IV | IV | IV |
| 6 | 32768 | LuV | LuV | LuV |
| 7 | 131072 | ZPM | ZPM | ZPM |
| 8 | 524288 | UV | UV | UV |
| 9 | 2097152 | MAX (2^31-1) | UHV (2M) | UHV (2M) |
| 10 | 8388608 | — | UEV (8M) | UEV (8M) |
| 11 | 33554432 | — | UIV (34M) | UIV (34M) |
| 12 | 134217728 | — | **UMV** | **UXV** |
| 13 | 536870912 | — | **UXV** | **OpV** |
| 14 | 2147483648 | — | MAX (2^31-1) | MAX (2^31) |

关键差异：

- GTCE 原版只有 10 级（ULV~MAX），MAX 是理论值，无 UHV+ 实际内容。
- TJFork 和 GTM 都扩展到 15 级，但 index 12/13 命名不同：TJFork `UMV`/`UXV`，GTM `UXV`/`OpV`。
- GTM index 14 用 `2^31`（2147483648L），TJFork 用 `Integer.MAX_VALUE`（2147483647）。
- GTM 高电压段（UHV+）有完整的机器壳体材料映射（Neutronium 等），但 `isHighTier()` 默认 false，
  需配置开启。UHV 段有 Wetware Mainframe 电路，UEV+ 无实质内容。
- TJFork 的 UHV+ 是核心扩展内容，有完整的 Optical/Exotic/Cosmic/Supracausal 电路层和对应产线。

迁移决策（已在 `gcy-material-migration-decisions.md` 第 0.1 节确认）：

- 迁移时按 GTM tier 命名和数值标准实现。
- TJFork `UMV` -> GTM `UXV`，TJFork `UXV` -> GTM `OpV`。
- 旧 id 只保留在 id map 中作为迁移输入键。

---

## 1. 蒸汽时代 (Bronze Age)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | 真空管 (VACUUM_TUBE) | 新增 Primitive Circuit 层，重写配方：真空管+电阻+锻铁板+基础电路板+红石合金线 | 同 GTCE 基础电路体系，T1 Electronic 层 |
| **材料** | 青铜(铜+锡)、锻铁、钢(初步) | 同 GTCE + 新增油藏系统（区块级流体储层） | 同 GTCE：青铜、钢(EBF 1000K) |
| **矿石** | 煤/铁/锡/铜/方铅矿等主世界矿脉 | 同 GTCE + 可选 Rich/Poor/Pure 矿石变体 | 同 GTCE 矿脉体系，按 STONE/DEEPSLATE/NETHERRACK/ENDSTONE 四层分布 |
| **机器** | 蒸汽锅炉(煤/岩浆/太阳能)、蒸汽粉碎/熔炉/压缩机/锤/合金炉、原始高炉、焦炉 | + Cluster Mill（簇轧机，自动压箔）、Chemical Dehydrator、Fluid Drilling Plant（采油） | + 蒸汽固体/液体/太阳能锅炉、Primitive Pump（原始水泵）、Charcoal Pile Igniter |
| **能量** | 蒸汽锅炉烧煤/岩浆 | + 油藏抽取（Oil/Raw Oil/天然气），早期需建采油设施 | 蒸汽锅炉（青铜/钢/钛/钨钢四级大型锅炉） |
| **工艺改动** | — | **GT6 曲面板**：桶/铁甲/管道/转子改用 `plateCurved` 而非 `plate`；**弯曲圆筒**：环/管道需 BendingCylinder 工具；**昂贵扳手**：板替代锭；**簇轧机**：自动压箔 | 无 GT6 工艺改动 |

TJFork 关键差异：GT6 工艺改动贯穿全游戏，曲面板和弯曲圆筒增加早期制作复杂度。油藏系统让石油化工成为早期必建产线。

---

## 2. LV (32 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Basic Circuit / Basic Electronic Circuit (Tier.Basic) | **重写为 Primitive + Electronic 两层**：Primitive Circuit（真空管+电阻+板）-> Electronic Circuit（8电阻+8电容+Good Phenolic Board+CPU+铜细线）。移除原版电路配方 | T1 Electronic: `ELECTRONIC_CIRCUIT_LV` (Basic) + T2 Integrated: `INTEGRATED_CIRCUIT_LV` (Basic)。使用树脂板/塑料板 + 真空管/晶体管/SMD |
| **线缆** | 锡 (32V, 1A, 损耗1) | 同 GTCE | 同 GTCE：锡 (EV 级 2A) |
| **材料** | 钢 (EBF 1000K)、青铜、铜 | + 电路板蚀刻工艺：SodiumPersulfate 或 IronChloride（氯化铁）蚀刻 | 同 GTCE：钢 (EBF 1000K, MV gas tier) |
| **矿石** | 磁铁矿(铁/钒/金)、铝土矿(铝前置) | 同 GTCE | 同 GTCE：MAGNETITE_VEIN_OW、MICA_VEIN(含 Bauxite) |
| **机器** | 全套 LV 单方块机器、柴油/蒸汽/燃气发电机 | + Cluster Mill、Circuit Assembler (LV~IV)、Chemical Dehydrator | 全套 LV 单方块（`ELECTRIC_TIERS` LV~UV）+ 燃烧/蒸汽/燃气发电机 (LV/MV/HV) |
| **能量** | 柴油发电机(油/Fuel)、蒸汽涡轮、燃气涡轮 | + 油藏系统提供 Oil/Raw Oil | 同 GTCE |
| **产线转折** | EBF 生产钢，电解分离矿石，化学链起步 | + 电路板蚀刻链（SodiumPersulfate 生产）、油藏抽取链 | EBF+钢，建立电力基础 |

TJFork 关键差异：电路体系完全重写，引入 13 级体系替代 GTCE 的 9 级。电路板需要化学蚀刻，增加化工依赖。

---

## 3. MV (128 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Good Integrated Circuit / Advanced Circuit (Tier.Good) | **Refined 层**：Processor/Assembly/Computer/Mainframe，用 Good Plastic Board + CPU + **MVSuperconductor** 线缆 | T1 Electronic: `ELECTRONIC_CIRCUIT_MV` (Good) + T2 Integrated: `INTEGRATED_CIRCUIT_MV` (Good) + T3 Processor: `PROCESSOR_MV` (Microprocessor) |
| **线缆** | 铜 (128V, 损耗2) | 同 GTCE | 同 GTCE：铜 (EV 级 2A) |
| **材料** | 铝 (EBF 1700K)、铜镍合金(线圈) | + **MVSuperconductor** (Cd5Mg1O6, 1200°C)、Ruridit (Ru2+Ir1, 4500°C) | 同 GTCE：铝 (EBF 1700K, LOW gas)、铜镍合金 (线圈 1800K) |
| **矿石** | 铝土矿(铝/钛铁矿)、镍(镍/钴)、方铅矿(银/铅) | 同 GTCE | 同 GTCE + MICA_VEIN(Bauxite+Pollucite) |
| **机器** | 全套 MV 机器、大型青铜/钢锅炉、挤压机(MV起) | + Large Chemical Reactor (MV起) | 全套 MV + 大型青铜/钢锅炉、Fluid Drilling Rig (MV/HV/EV) |
| **能量** | 大型锅炉+蒸汽涡轮 | + 油藏深度加工 | 同 GTCE |
| **产线转折** | 铝生产，钛前置，蒸馏塔 | + 超导材料起步（MVSuperconductor）、铂族泥链开始 | 铝量产，铜镍合金线圈 |

TJFork 关键差异：超导材料在 MV 段就引入（GTCE 原版要到 LuV 段才有超导），铂族金属处理链提前开始。

---

## 4. HV (512 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Processor Assembly / Nano Processor (Tier.Advanced) | **Micro 层**：用 Advanced Board(Electrum 箔) + SMD + Titanium 板 + **HVSuperconductor** (Ti1Ba9Cu10O20, 3300°C) | T2 Integrated: `INTEGRATED_CIRCUIT_HV` (Advanced) + T3 Processor: `PROCESSOR_ASSEMBLY_HV` + T4 Nano: `NANO_PROCESSOR_HV` |
| **线缆** | 金 (512V, 损耗2) / Kanthal | 同 GTCE | 同 GTCE：金 (EV 级 2A) + Kanthal (HV 4A 线圈 2700K) |
| **材料** | 不锈钢 (Fe+Cr+Mn+Ni)、退火铜、铬 | + **HVSuperconductor**、TantalumHafniumSeaborgiumCarbide (Ta12Hf3Sg1C16, 5200°C, 聚变线圈) | 同 GTCE：不锈钢 (EBF 1700K, HV) + 铬 + Kanthal (线圈 2700K) |
| **矿石** | 红石(铬/红宝石)、锰(锰/钽)、钨酸盐(钨/锂) | 同 GTCE | 同 GTCE：REDSTONE_VEIN_OW(铬)、MANGANESE_VEIN_OW(钽)、SCHEELITE(钨/锂) |
| **机器** | 全套 HV 机器、EBF、真空冷冻机、大型钛锅炉、转子支架[0] | + Large Centrifuge、Large Mixer | + GCYM 大型多方块（LargeCentrifuge/Mixer/Electrolyzer 等 24 个，带 Parallel Hatch） |
| **能量** | 大型锅炉+涡轮 | 同 GTCE | 大型蒸汽涡轮 (HV, Steel 壳) |
| **产线转折** | **EBF+真空冷冻闭环**、不锈钢量产、硅片链 (1784K) | + 大型离心/混合机并行、超导线缆 HV 级 | EBF+真空冷冻闭环、不锈钢、硅片链、GCYM 大型并行 |

TJFork 关键差异：大型并行机器在 HV 段引入（GTM 的 GCYM 也有类似功能）。超导材料持续提前。

---

## 5. EV (2048 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Nano Processor Assembly / Quantum Processor (Tier.Extreme) | **Nano 层**：Extreme Board(AnnealedCopper 箔) + NANO_CPU + **EVSuperconductor** + TungstenSteel 板。Wafer: Rutherfordium Boule (7200°C, Krypton) | T4 Nano: `NANO_PROCESSOR_ASSEMBLY_EV` + T5 Quantum: `QUANTUM_PROCESSOR_EV` |
| **线缆** | 铝 (2048V, 损耗1) / 钛 / Nichrome | 同 GTCE | 同 GTCE：铝 (EV 1A) + Nichrome (EV 4A 线圈 2700K) |
| **材料** | 钛 (EBF 2341K, 需 Mg+TiCl4)、铬、Nichrome | + Rutherfordium/Dubnium/Seaborgium/Bohrium 新元素、MetastableHassium (11240°C)、Neutronium、Trinium(矿石, 8600°C) | 同 GTCE：钛 (EBF 1941K, MID gas) + 钨 (EBF 3600K) + BlackSteel + HSSG (EBF 4200K) |
| **矿石** | 沥青铀矿(铀)、独居石(钕/稀土)、铂(铂/钯/铱)、钼 | 同 GTCE | 同 GTCE：PITCHBLENDE(铀)、MONAZITE(稀土)、SHELDONITE(铂族)、SCHEELITE(钨) |
| **机器** | 全套 EV 机器、内爆压缩机、热解炉、蒸馏塔、裂化器、大型钨钢锅炉 | + Assembly Line 扩展、Chemical Plant | + GCYM AlloyBlastSmelter + 大型内燃机 (EV, Titanium 壳) + Distillation Tower |
| **能量** | 大型钨钢锅炉、柴油引擎、石油裂解 | + Naquadah 反应堆(单方块) | 大型燃气涡轮 (EV, Stainless 壳) + 大型内燃机 (EV) |
| **产线转折** | **钛量产**(ilmenite->TiCl4->EBF)、铀/钍核燃料、铂族精炼、Glowstone Wafer(2484K) | + Naquadah 深度加工链(30+步化学)、超重元素链、核工业 EV 段 | 钛量产、钨(3600K)、HSSG(4200K)、石油蒸馏/裂解链、Cleanroom 引入 |

TJFork 关键差异：Naquadah 加工链大幅扩展（GTCE 原版只是简单离心）。核工业在 EV 段开始有实质内容。Trinium 矿石作为新材料引入。

---

## 6. IV (8192 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Data Control Circuit / Crystal Processor (Tier.Elite) | **Quantum 层**：Elite Board(Platinum 箔) + QBIT_CPU + **IVSuperconductor** (V1In3, 5200°C) + Osmium 板 + Ruridit 框架。Quantum Mainframe 需 Assembly Line + QUANTUM_STAR | T5 Quantum: `QUANTUM_ASSEMBLY_IV` + T6 Crystal: `CRYSTAL_PROCESSOR_IV` |
| **线缆** | 铂 / 钨 / 钨钢 / 锇 | 同 GTCE | 同 GTCE：铂 (IV 2A) + TungstenSteel (IV 3A) + 钨钢 (EBF 3000K) |
| **材料** | 钨钢 (W+Steel, EBF 3000K)、Osmiridium (Ir3+Os1, 2900K)、铂、钨 | + Adamantium/Vibranium/Taranium(虚构材料, 10000~11220°C) | 同 GTCE：钨钢 (EBF 3000K) + Osmiridium + Naquadah (EBF 5000K, HIGH gas) |
| **矿石** | 铂(铱/锇/铂)、钨酸盐(钨)、独居石(钕) | 同 GTCE | 同 GTCE：SHELDONITE(铂族)、SCHEELITE(钨) + 末地 NAQUADAH_VEIN |
| **机器** | 装配机/高压釜/激光雕刻机(IV)、量子箱/罐 | + Assembly Line 大量扩展（Qubit 消费型） | + Assembly Line (需 Data Hatch) + 大型等离子涡轮 (IV, TungstenSteel 壳) + 极限内燃机 (IV) |
| **能量** | 大型涡轮、等离子涡轮起步 | + Naquadah 反应堆 IV 级 | 大型等离子涡轮 (IV) |
| **产线转折** | **钨钢量产**、Osmiridium、装配线建立、Naquadah Wafer(5400K) | + 虚构材料链(Adamantium 等)、Quantum Mainframe(QBIT+量子星) | 钨钢量产、Naquadah(5000K)、装配线+研究系统引入 |

TJFork 关键差异：装配线改为 Qubit 消费型（GTCE 原版无 Qubit 概念）。虚构材料（Adamantium/Vibranium/Taranium）引入，为 UHV+ 段做铺垫。GTM 引入研究系统（Scanner LV 起，Assembly Line IV 起）。

---

## 7. LuV (32768 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Energy Flow Circuit / Wetware Processor (Tier.Master) | **Crystal 层**：Kapton Circuit Board(VanadiumGallium 箔) + Crystal CPU + NANO_CPU + **LuVSuperconductor** (In4Bronze8Ba2Ti1O14, 6000°C)。Crystal Mainframe 需 HSSE 框架 + QUANTUM_STAR×4 | T6 Crystal: `CRYSTAL_ASSEMBLY_LuV` + T7 Wetware: `WETWARE_PROCESSOR_LuV` |
| **线缆** | 铌钛 / HSSG / 钒镓 / YBCO (超导过渡) | 同 GTCE | 同 GTCE：NiobiumTitanium (LuV 4A) + IndiumTinBariumTitaniumCuprate (**超导** LuV 8A 0损耗) |
| **材料** | HSSG (EBF 4500K)、铌钛、YBCO | + Kapton 板(Polyimide+FEP)、Crystal CPU 链 | 同 GTCE：HSSG (EBF 4200K) + RhodiumPlatedPalladium (LuV 壳体) + YBCO (EBF 4500K, 超导) |
| **矿石** | 末地 naquadah、铂、钨酸盐、铍 | 同 GTCE | 同 GTCE：末地 NAQUADAH_VEIN + SHELDONITE + SCHEELITE |
| **机器** | 装配线 LuV 组件、转子支架[1](LuV) | + 先进聚变反应堆(线圈/真空/偏滤器层级) | + **聚变反应堆 MK I (LuV)** + HPCA (LuV) + Data Bank (LuV) + 大型矿机 (EV/IV/LuV) |
| **能量** | 大型等离子涡轮、聚变等离子 | + 先进聚变 | **聚变反应堆 MK I** (D+T->He plasma, 4096 EU/t) |
| **产线转折** | **装配线时代**(HSSG 4500K)、超导材料、Wetware 电路起步、聚变起步、末地矿探索 | + Kapton 板产线、先进聚变 | 聚变 MK I、HPCA 计算系统、研究站(ZPM 前置)、Wetware 电路(需 Cleanroom) |

TJFork 关键差异：先进聚变反应堆是独立于 GTCE 聚变的更复杂系统。Kapton 板引入 Polyimide 产线依赖。GTM 的研究/计算系统在 LuV 段引入（HPCA 产生 CWU/t）。

---

## 8. ZPM (131072 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Wetware Processor Assembly (Tier.Ultimate) | **Wetware 层**：Crystal SoC + CYBER_PROCESSING_UNIT + QBIT_CPU + **ZPMSuperconductor** (Nq4In2Pd6Os1, 8100°C) + NaquadahAlloy。Wetware Assembly 起需 Assembly Line + ARAM + QUANTUM_EYE×4 + SterileGrowthMedium | T7 Wetware: `WETWARE_PROCESSOR_ASSEMBLY_ZPM` + T6 Crystal: `CRYSTAL_COMPUTER_ZPM` |
| **线缆** | Naquadah (损耗2) | 同 GTCE | 同 GTCE：Naquadah (ZPM 2A) + UraniumRhodiumDinaquadide (**超导** ZPM 8A 0损耗) |
| **材料** | HSSE (EBF 5400K)、Naquadah、钒镓 | + Wetware 生物链：生长培养基->干细胞->基因疗法->神经处理器。Naquadah 深度加工(Naquadria/Enriched) | 同 GTCE：HSSE (EBF 5400K) + NaquadahAlloy (UV 壳体) + Naquadah (EBF 5000K) |
| **矿石** | 末地 naquadah(主) | 同 GTCE | 同 GTCE：末地 NAQUADAH_VEIN(Naquadah+Pu239) |
| **机器** | 装配线 ZPM 组件(HSSE+Platinum)、Field Generator ZPM(需 QUANTUM_STAR×4) | + Hyper Fuel I~IV 产线、火箭引擎 | + **研究站 (ZPM)** + 计算传输/接收仓 (ZPM) + 聚变 MK II (ZPM) |
| **能量** | 核聚变、Naquadah 燃料 | + Hyper Reactor I~III + HyperFuel | **聚变反应堆 MK II** (需 CWUt=16 研究站研究) |
| **产线转折** | HSSE 量产(5400K)、Naquadah 供应链(末地)、Naquadah 离心富集 | + Wetware 生物链(干细胞/基因/神经处理器)、Hyper Fuel 产线 | 研究站引入(需 CWU/t)、聚变 MK II、Wetware 电路 |

TJFork 关键差异：Wetware 电路链大幅扩展为完整生物产线（干细胞、基因疗法、神经处理器），GTCE 原版只是简单物品。Hyper Fuel/Hyper Reactor 是 TJFork 独有能源系统。GTM 的研究站在 ZPM 段引入，需 HPCA 提供 CWU/t。

---

## 9. UV (524288 EU/t)

| 维度 | GTCE 原版 | TJFork 更改 | GTM 7.5.3 |
|------|----------|-------------|-----------|
| **电路** | Wetware Super Computer (Tier.Superconductor) | **Bioware 层**：QBIT_CPU×4 + NEURO_PROCESSOR + HASOC×4 + NaquadahAlloy 细线 + **UVSuperconductor**。Bioware SMD 用 Dubnium/GermaniumTungstenNitride/PEDOT/Osmiridium/BismuthRuthenate 等 | T7 Wetware: `WETWARE_SUPER_COMPUTER_UV` + T6 Crystal: `CRYSTAL_MAINFRAME_UV` |
| **线缆** | NaquadahAlloy (损耗4) / Duranium | 同 GTCE | 同 GTCE：NaquadahAlloy (UV 2A) + EnrichedNaquadahTriniumEuropiumDuranide (**超导** UV 16A 0损耗) |
| **材料** | Darmstadtium (硬度155360)、NaquadahAlloy (7200K EBF)、超导体 | + Dubnium/Seaborgium/Bohrium、PEDOT/PEEK/Zylon/FullerenePolymerMatrix、HastelloyX78、BlackTitanium/TitanSteel 等 6~7 级材料 | 同 GTCE：Darmstadtium (UV 壳体) + NaquadahAlloy + Trinium (线圈 9001K) + Tritanium (线圈 10800K) |
| **矿石** | 末地 naquadah(大量) | 同 GTCE | 同 GTCE：末地 NAQUADAH_VEIN |
| **机器** | 装配线 UV 组件(全 Darmstadtium+超导)、Field Generator UV(需 GRAVI_STAR) | + **Stellar Forge**、**Plasma Condenser**、核反应堆末期(Fermium/Mendelevium)、Gas Centrifuge、Hot Coolant Turbine、Microwormhole Generator | + **聚变反应堆 MK III (UV)** + Network Switch (UV) |
| **能量** | 高级聚变、NaquadahAlloy | + Stellar Forge(恒星锻造)、Hyper Reactor III、核工业末期 | **聚变反应堆 MK III** (需 CWUt=96 研究站研究) |
| **产线转折** | **Darmstadtium 时代**、NaquadahAlloy(7200K)、全超导、Gravi Star 合成 | + 简并态物质(DegenerateRhenium)、Neutronium 前置、虫洞生成器起步 | 聚变 MK III、Wetware Mainframe (UHV 前)、Darmstadtium |

TJFork 关键差异：UV 段是 TJFork 内容爆发点——Stellar Forge/Plasma Condenser 引入恒星锻造产线，核工业达到末期，虫洞/奇点系统起步。Bioware 电路引入大量新材料。GTM 在 UV 段以聚变 MK III 和 Wetware 电路为主，无 Bioware/Optical 等更高电路线。

---

## 10. UHV+ 扩展段 (2M~537M EU/t)

GTCE 原版无 UHV+ 内容（MAX=9 仅为理论值，无实际机器/材料/配方）。

GTM 7.5.3 有 UHV 壳体材料映射（Neutronium），但 `isHighTier()` 默认 false。UHV 段有 Wetware Mainframe 电路，但 UEV+ 无实质内容。

TJFork 的 UHV+ 是核心扩展内容，按下表细分：

| 段位 | TJFork 电路 | TJFork 核心内容 | TJFork 关键材料 |
|------|------------|----------------|----------------|
| **UHV** | Optical (Optical SoC + Pikyonium + UHVSuperconductor + CladdedOpticalFiberCore) | 简并态物质(DegenerateRhenium)、Neutronium 锭(StellarForge+PlasmaCondenser)、Wormhole Generator 链、Advanced Fusion Reactor(coilTier 1) | Pikyonium(10400°C)、ZBLAN 光纤、MetastableHassium Wafer(11200°C) |
| **UEV** | Exotic (ExoticCore + Cinobite + UEVSuperconductor + MetastableFlerovium) | **首次引入 Qubit 消耗**、Cosmic 链起点(QuarkGluonPlasma)、Taranium 链、Advanced Fusion(coilTier 2)、SuperheavyHAlloy/LAlloy | Cinobite(11465°C)、MetastableFlerovium/Oganesson、Taranium(从 Stone 极低概率) |
| **UIV** | Cosmic (CosmicCore + UHASOC + AbyssalAlloy + UIVSuperconductor) | Cosmic ComputingMix、HeavyQuarkDegenerateMatter(AdvFusion coilTier 3)、超导体链(ActiniumSuperhydride 等)、Cosmic 闪烁体 | AbyssalAlloy(9625°C)、HastelloyK243(12100°C)、HeavyQuarkDegenerateMatter(13000°C) |
| **UMV** | (无新电路层) | **Periodicium**(混合全部 118 种元素)、CosmicNeutronium、CosmicMesh/Fabric、DenseNeutronPlasma、ExtremelyDurablePlasmaCell | Periodicium、CosmicNeutronium(14100°C)、CosmicMesh/Fabric、SuperfluidHelium |
| **UXV** | Supracausal (SupracausalCore + UMVSuperconductor + QCDMatter + CosmicMesh/Fabric) | **终极目标**：CTC(闭合类时曲线)计算单元、RecursiveFoldedNegativeSpace、ContainedExoticMatter、MicrowormholeGenerator | QCDMatter、UXVSuperconductor、Ultimate 末期材料 |

---

## 11. 横切维度对比

### 11.1 跨电压过渡机制

| 过渡 | GTCE | TJFork | GTM |
|------|------|--------|-----|
| 蒸汽->LV | PBF/EBF 生产钢 | + GT6 曲面板/弯曲圆筒增加复杂度 + 油藏系统 | 同 GTCE + Primitive Pump/Charcoal Igniter |
| LV->MV | 铝生产(bauxite) | + 超导材料提前(MVSuperconductor) + 铂族泥链开始 | 同 GTCE |
| MV->HV | EBF+真空冷冻闭环 | + 大型并行机器(Large Centrifuge/Mixer) | + GCYM 大型并行多方块 |
| HV->EV | 钛量产(TiCl4->EBF) | + Naquadah 深度加工链 + 核工业 EV 段 | + Cleanroom 引入 + 石油蒸馏/裂解 |
| EV->IV | 钨钢(3000K)、装配线 | + 虚构材料(Adamantium 等) + Assembly Line Qubit 消费 | + 研究系统(Scanner->Assembly Line) |
| IV->LuV | HSSG(4500K)、超导 | + Kapton 板(Polyimide) + 先进聚变 | + 聚变 MK I + HPCA 计算系统 |
| LuV->ZPM | HSSE(5400K)、Naquadah | + Wetware 生物链 + Hyper Fuel | + 研究站(需 CWU/t) + 聚变 MK II |
| ZPM->UV | Darmstadtium、NaquadahAlloy | + Stellar Forge + 核工业末期 + 虫洞起步 | + 聚变 MK III |
| UV->UHV+ | 无(GTCE 终止) | + 简并态物质 + Optical/Exotic/Cosmic/Supracausal 电路 | Wetware Mainframe (UHV)，再上无内容 |

### 11.2 能量与燃料系统

| 能源类型 | GTCE | TJFork 新增 | GTM |
|---------|------|------------|-----|
| 蒸汽 | 锅炉(煤/岩浆/太阳能) | + 油藏系统 | 锅炉(4级大型)+Primitive Pump |
| 燃油 | 柴油发电机(LV/HV) | + 油藏深度加工 | 燃烧发电机(LV/MV/HV) + 大型内燃机(EV/IV) |
| 燃气 | 燃气涡轮(LV/HV) | 同 GTCE | 燃气涡轮(LV/MV/HV) + 大型燃气涡轮(EV) |
| 蒸汽涡轮 | 蒸汽涡轮(LV/HV) | 同 GTCE | 蒸汽涡轮(LV/MV/HV) + 大型蒸汽涡轮(HV) |
| 等离子 | 大型等离子涡轮 | + 核反应堆/增殖堆/热冷却液涡轮 | 大型等离子涡轮(IV) |
| 聚变 | 无(GTCE 无聚变) | + 先进聚变(coilTier 1~5) + Hyper Reactor I~III | 聚变 MK I(LuV)/II(ZPM)/III(UV) |
| 核能 | 无 | + 完整核工业(12种燃料棒 + 9类废料) | 无 |
| 恒星锻造 | 无 | + Stellar Forge(简并态物质制造) | 无 |

### 11.3 世界生成与矿石获取

| 维度 | GTCE | TJFork | GTM |
|------|------|--------|-----|
| 矿脉系统 | JSON worldgen | 同 GTCE + Rich/Poor/Pure 矿石变体 | `GTOreDefinition` + 3种生成器(layered/cuboid/dike) |
| 主世界 | 煤/铁/锡/铜/铝土/镍/方铅/红石/锰/铍/钼/钨酸盐/铂/沥青铀矿/独居石/橄榄石 | 同 GTCE | 同 GTCE + DEEPSLATE 层(铜/钻石/青金石/锰/云母/橄榄石/红石/蓝宝石) |
| 下界 | 镍/ tetrahedrite/铁/石英/红石/硫/磁铁矿/黄铜矿 | 同 GTCE | 同 GTCE + 铍/钼/独居石/硝石/黄玉/石英 |
| 末地 | naquadah/铂/钨酸盐/铍/锰/钼/镍/橄榄石 | 同 GTCE | 同 GTCE + 铝土/磁铁矿/沥青铀矿/白钨 |
| 油藏 | 无 | **区块级油藏系统**(维度/生物群系权重/补给率/耗尽) | 基岩流体(`GTBedrockFluids`) |
| 维度标记 | 无 | 无 | `GTDimensionMarkers` |

### 11.4 跨模组集成影响

| 集成 | GTCE | TJFork | GTM |
|------|------|--------|-----|
| JEI | 基础配方显示 | + 核反应堆/聚变/虚空矿机 JEI 页面 | 基础配方显示 |
| AE2 | 无 | + ME Stocking Bus + 数字接口覆盖板 + CoreMod hook | ME Stocking Bus/Hatch（内置） |
| CraftTweaker | 无 | 全配方图 ZenRegister | KubeJS 替代(`GregTechKubeJSPlugin`) |
| Forestry | 无 | 完整 GT 蜜蜂系统 | 无 |
| Tinkers | 无 | GregsConstruct(GT 材料进 TConstruct) | 无 |
| ExNihilo | 无 | 电动筛配方 | 无 |
| TOP | 无 | 能源/二极管/Qubit 信息显示 | 无（偏 Jade） |

---

## 12. TJFork 完全更改的产线汇总

下表列出 TJFork 相比 GTCE/GTM 完全更改或全新引入的产线，以及迁移到 Modern 时需要注意的差异点。

| 产线 | 更改性质 | 具体变化 | 迁移注意 |
|------|---------|---------|---------|
| **全部电路产线** | TJFork 完全重写 | 13 级体系(Primitive->Supracausal) + 磁共振支线(12级)，移除所有 GTCE 原版电路配方。GTM 只有 7 层(Electronic->Wetware) | 决定是否保留 TJFork 13 级体系还是用 GTM 7 层；高阶层(Bioware+)需新建 |
| **全部电路板产线** | TJFork 新增蚀刻工艺 | Good/Advanced/Extreme/Elite/Kapton/Wetware/Master Board，用 SodiumPersulfate/IronChloride 蚀刻。GTM 用树脂/酚醛/塑料/环氧/纤维/多层板/Wetware 板 | 蚀刻材料(SodiumPersulfate/IronChloride)需迁移为 GTM 材料 |
| **Wafer 产线** | TJFork 大幅扩展 | Rf/Db/Neutronium/Hassium Boule + 激光雕刻(特殊透镜)。GTM 有 Silicon/Phosphorus/Naquadah/Neutronium Wafer | GTM 已有基础 Wafer 链；TJFork 的高阶 Boule 和特殊透镜需新建 |
| **Naquadah 产线** | TJFork 完全更改 | 30+步化学链(AquaRegia->FluoroantimonicAcid->FluoronaquadricAcid->...)。GTM 仅简单 EBF/离心 | TJFork 的 Naquadah 深加工链是独立设计，需整体迁移 |
| **核废料产线** | TJFork 独有 | 9 路分类回收(镧系/碱金属/重金属/...)。GTM 无核工业 | 核工业整体暂缓(`deferred/nuclear`) |
| **铂族泥产线** | TJFork 大幅扩展 | 6 种贵金属(Pt/Pd/Rh/Ru/Ir/Os)独立分离链。GTM 有基本铂族处理 | 材料组分差异见 `gcy-material-migration-decisions.md` |
| **Wetware 生物产线** | TJFork 独有 | 基因/干细胞/神经处理器完整链。GTM 仅 Wetware 电路物品 | TJFork 的生物链是独立系统，需新建 recipe type + 材料 |
| **简并态/Cosmic 产线** | TJFork 独有 | StellarForge+PlasmaCondenser 制造简并态物质。GTM 无 | 需新建 Stellar Forge/Plasma Condenser 机器 + recipe type |
| **Hyper Fuel 产线** | TJFork 独有 | NaquadahFuel+ENaquadahFuel+NaquadriaSolution+超重元素->4级超能燃料。GTM 无 | 需新建 Hyper Reactor 机器 + 燃料配方 |
| **Taranium 产线** | TJFork 独有 | 从 Stone 经 20+步化学提取。GTM 无 | 需新建完整化学链 |
| **Periodicium 产线** | TJFork 独有 | 混合全部 118 种元素。GTM 无 | 末期内容，需全部前置材料就绪 |
| **虫洞/奇点产线** | TJFork 独有 | NeutroniumSphere->奇点->虫洞生成器。GTM 有 Quantum Star/Gravi Star 但无虫洞 | 需新建虫洞生成器机器 + 材料链 |

---

## 13. GTM 独有系统（TJFork/GTCE 没有的）

以下系统是 GTM 7.5.3 独有的，TJFork 和 GTCE 原版都没有。迁移时这些可以作为 Modern 的原生能力保留，不需要从 TJFork 移植。

| 系统 | 引入电压 | 说明 |
|------|---------|------|
| **研究系统** | Scanner LV 起，研究站 ZPM | Scanner 扫描物品获研究数据 -> Data Stick/Orb/Module -> Research Station 消耗 CWU/t 研究解锁配方 |
| **计算系统 (CWU/t)** | HPCA LuV 起 | HPCA 产生 CWU/t，供 Research Station 和装配线消耗；Advanced Component 16 CWU/t |
| **洁净室 (Cleanroom)** | HV+ | 多方块结构，Wetware 电路等需洁净室条件 |
| **GCYM 大型并行多方块** | HV+ | 24 个大型并行机器 + Parallel Hatch(IV~UV)，含 Mega Blast Furnace/Mega Vacuum Freezer |
| **主动变压器/变电站** | LuV+ | 64A 大电流分配 + 激光能量支持 |
| **基岩矿机** | MV/HV/EV | 采掘基岩矿石层 |
| **长距离管道** | 任意 | 物品/流体长距离管线终端 |
| **多方块储罐** | 任意 | 木/青铜/钢 3 种材质多方块储罐 + Tank Valve |
| **数据银行/网络交换机** | LuV/UV | 共享研究数据、分配计算资源 |
| **危险系统** | 全程 | `HazardProperty` + `MedicalCondition` + `EnvironmentalHazardCondition` 配方条件 |
| **维度标记** | 世界生成 | `GTDimensionMarkers` 维度矿石指示 |
| **KubeJS 集成** | 全程 | `GregTechKubeJSPlugin` 暴露材料/prefix/机器/配方/worldgen bindings |
| **Curios/Heracles/FTBQuests** | 全程 | 饰品栏 + 任务/阶段系统（配方条件集成） |

---

## 后续工作

本文档是初版总览。后续按电压阶段单独讨论时，可以逐段细化：

1. 每个电压段的具体机器注册列表和合成配方
2. 每个电压段的材料获取路线（矿石->中间产物->最终材料）
3. TJFork 完全更改产线的逐步迁移方案
4. 统一材料在 GTCE/TJFork/GTM 三者间的组分/形态差异
5. 能量系统的具体燃料配方和发电效率对比
