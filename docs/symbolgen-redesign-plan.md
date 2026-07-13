# symbolgen 重构计划

本计划基于对当前 symbolgen 实现的审查，对照 `docs/compile-time-scala-dsl-design.md` 的设计意图，
列出包组织、命名、抽象层次上的问题，并给出分步重构方案。每步落地时同步处理测试，
不留过时测试，不积累中间红状态。

## 背景与问题清单

当前 symbolgen 模块存在以下问题（详见审查讨论）：

1. **共享值类型物理位置与包名错位**：`Refs.scala` 物理位于 `src/symbolgen`，包名却是
   `com.pixdane.gregicality.codegen.dsl.model`，导致同一顶层包 `codegen` 的源码分裂在
   `symbolgen` 和 `codegen` 两个 source set 目录下。
2. **`GtceuScanDiagnostic.scala` 单文件承载多职责**：类型别名、诊断 enum、`GtceuScanResultOps`
   游离 object、`MaterialIdOccurrence` 辅助类挤在一起；`GtceuScanResultOps` 命名暗示伴生关系
   但实际是顶层 object，与类型别名 `GtceuScanResult` 无结构关联。
3. **`GtceuRefJob` 手写 match boilerplate**：`id`/`target` 用手写 pattern match 实现，
   未利用 Scala 3 enum 的 `override val` 自动合成。
4. **`gtceu` 包内层级混乱**：spec（`GtceuScanSpecs`）在 `gtceu` 根，消费 spec 的 scanner
   在 `gtceu.scan`/`gtceu.scan.materials`；dispatch（`GtceuRefScanner`/`GtceuRefProcessor`）
   在根，dispatch 目标在子包，边界规则不清。
5. **alias 机器过度设计**：`MaterialAliasScanner`/`MaterialAliasResolver`/`ScannedMaterialAliasRef`/
   `AliasCycle`/`UnresolvedAlias` 一整套为"材料字段互相别名"场景造的机器，对当前"生成 ref 供 DSL
   完成"的目标无实际贡献，引入 chain 解析、cycle 检测等真实复杂度。
6. **`GtceuRefProcessor`/`GtceuRefScanner` 是薄 dispatch 层**：按 job variant match 选 scanner/
   renderer，扩展时三处 match 需同步，是 enum 扩展痛点。
7. **`generateGtRefs` 任务输入声明不全**：只声明 sources jar 为 `@InputFiles`，symbolgen 代码
   变化不参与 up-to-date 检查，渲染格式改了可能不触发重生成。
8. **`CodeLayout` 投机抽象**：只被 `RefObjectRenderer` 用一次，可内联。
9. **`TagPrefixRef` 未使用**：`Refs.scala` 里的投机性预留类型，无消费方。
10. **测试组织**：`GtceuSourceScannersTest` 承担过多职责，且 alias 相关测试占约 6 个。

## 目标设计

### 依赖图与 source set 划分

```
core (src/core, 零外部依赖, 包 com.pixdane.gregicality.core.refs)
  <- symbolgen (src/symbolgen, 包 gregicality.symbolgen, 依赖 core + JavaParser + Cats)
       <- generateGtRefs 产出
            <- codegen (src/codegen, 包 gregicality.codegen, 依赖 core + 生成的 refs 目录 + main classpath)
  <- dsl (未来)
  <- codegen
  <- main (gregicality, 未来可能)
```

**core 的两条 invariant（写入设计文档）**：

1. 只放纯值类型和共享 ADT，不引任何库依赖（只有 Scala 标准库）
2. 不放任何"行为"（scanner、renderer、validator），那些属于上层各自的 source set

### 包命名调整

| 内容 | 旧包名 | 新包名 |
| --- | --- | --- |
| 手写稳定值类型 | `com.pixdane.gregicality.codegen.dsl.model` | `com.pixdane.gregicality.core.refs` |
| 生成产物目录 | `com.pixdane.gregicality.codegen.dsl.refs.gtceu` | `com.pixdane.gregicality.core.refs.gtceu` |

手写 refs 基础类型归 `com.pixdane.gregicality.core.refs`，生成目录延续 `core.refs.gtceu` 前缀，
未来公共类型（enum、helper、共享 ADT）归 `com.pixdane.gregicality.core` 其他子包。

### job 三段式类型参数化

```scala
final case class SymbolJob[E, A, B](
  id: String,
  target: RefObjectTarget,
  scan: SourceArchive => IorNec[E, A],
  preprocess: A => IorNec[E, B],
  render: (RefObjectTarget, B) => GeneratedScalaFile
):
  def run(archive: SourceArchive): IorNec[E, GeneratedScalaFile] =
    scan(archive).flatMap(preprocess).map(b => render(target, b))
```

三个中间件可替换，类型从 `SourceArchive` 流到 `GeneratedScalaFile`，诊断 `E` 贯穿
scan 和 preprocess。同一 backend（GTCEu）下多个 job 可引用同一函数值实现 preprocess 共享。

### 删除清单

- alias 机器：`MaterialAliasScanner`、`MaterialAliasResolver`（含 `AliasResolution`、
  `ChainOutcome`、`resolveChain`）、`ScannedMaterialAliasRef`、`GtceuScanDiagnostic.AliasCycle`、
  `GtceuScanDiagnostic.UnresolvedAlias`、`MaterialDiagnostics.duplicateAssignments` 里 alias 部分、
  `MaterialExpressionParsers.materialReferenceName`（如只被 alias 用）、`LocatedMaterialAlias`、
  `rejectedValueReason` 的 alias 分支
- `GtceuScanResultOps`（调用点改 `Ior.right`/`Ior.both`）
- `GtceuScannedRefs` enum（dispatch 由 job 字段决定，不需要了）
- `GtceuRefProcessor`（同上）
- `GtceuRefScanner`（同上）
- `CodeLayout`（内联回 `prefix ++ joinWith(separator)(items) ++ suffix`）
- `TagPrefixRef`（未使用）
- `Pipeline`（被 `SymbolJob.run` 替代，若 `SymbolgenDomain` 改用 `SymbolJob` 则 `Pipeline` 无消费方）

### 保留与改名

- `ScannedRegisteredMaterialRef` -> `ScannedMaterialRef`（不同种类 material ref 本质都是
  name + id + path，无结构差异）
- `ScannedRef` sealed trait 保留，只声明 `name`/`path`，作为"扫描产出"的类型抽象
- `GtceuSourceScanners` 保留，被 job 的 scan 字段引用
- `MaterialDeclarationScanner`/`MaterialAssignmentScanner` 保留，归 scan 侧
- `MaterialDiagnostics`/`MaterialExpressionParsers` 保留（删 alias 部分后），归 preprocess 侧
- `MaterialIdOccurrence` 挪到 `GtceuScanDiagnostic` enum 定义前

## 执行步骤

执行前确认：当前分支 `codex-symbolgen-javaparser-refs`，工作树有未提交的 `build.gradle.kts`
改动（依赖顺序调整，用户之前的工作，保留不动）。在本分支上继续重构，不开新分支
（若用户要求开新分支则切换）。

### Step 1：抽出 core source set

最底层基础，影响包名和依赖图，必须先做。

**代码改动**：

- 新建 `src/core/scala/com/pixdane/gregicality/core/refs/Refs.scala`，内容来自
  `src/symbolgen/scala/com/pixdane/gregicality/codegen/dsl/model/Refs.scala`
- 包名 `com.pixdane.gregicality.codegen.dsl.model` -> `com.pixdane.gregicality.core.refs`
- 删除 `TagPrefixRef`（未使用）
- 删除原 `src/symbolgen/scala/com/pixdane/gregicality/codegen/dsl/model/Refs.scala`
- 生成产物包 `com.pixdane.gregicality.codegen.dsl.refs.gtceu` ->
  `com.pixdane.gregicality.core.refs.gtceu`，改 `GtceuRefJobs.scala` 和 `GtceuPipelines.scala` 的
  `OutputPackage` 常量
- `RefObjectRenderer.scala` 第 75 行生成的 import 行
  `import com.pixdane.gregicality.codegen.dsl.model.*` ->
  `import com.pixdane.gregicality.core.refs.*`
- 全局更新所有 `com.pixdane.gregicality.codegen.dsl.model` import（涉及
  `ScannedRefs.scala`、`RefObjectRenderer.scala`、`StaticFieldScanner.scala`、
  `MaterialAliasResolver.scala`、`MaterialAssignmentScanner.scala`、
  `GtceuSourceScannersTest.scala`、`RefObjectRendererTest.scala`）

**构建改动**（`build-logic/convention/src/main/kotlin/codegen.gradle.kts`）：

- 新增 `core` source set：`src/core/scala`，零外部依赖
- `symbolgen` 的 `compileClasspath` 加 `core.output`
- `codegen` 的 `compileClasspath` 加 `core.output`（替换或补充现有 `symbolgen.output`，
  注意 `codegen` 仍需 `symbolgen.output` 用于生成的 refs 目录）
- `symbolgenTest` 的 `compileClasspath` 加 `core.output`
- `core` 不加任何 `dependencies`（只有 Scala 标准库，由 `scala-project` plugin 提供）

**测试同步**：

- `RefObjectRendererTest.scala`：硬编码的包名预期字符串改新包名
  - 第 19 行 `outputPackage = "com.pixdane.gregicality.codegen.dsl.refs.gtceu"` ->
    `"com.pixdane.gregicality.core.refs.gtceu"`
  - 第 53 行 `package com.pixdane.gregicality.codegen.dsl.refs.gtceu` ->
    `package com.pixdane.gregicality.core.refs.gtceu`
  - 第 55 行 `import com.pixdane.gregicality.codegen.dsl.model.*` ->
    `import com.pixdane.gregicality.core.refs.*`
  - 第 56 行附近 `com/pixdane/gregicality/codegen/dsl/refs/gtceu/GTMaterialsRef.scala` ->
    `com/pixdane/gregicality/core/refs/gtceu/GTMaterialsRef.scala`
  - 其他 `outputPackage` 硬编码处（第 96、125 行）同步
- `GtceuSourceScannersTest.scala`：import
  `com.pixdane.gregicality.codegen.dsl.model.{ResourceId, ScalaSymbolPath}` ->
  `com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}`
- `GeneratedSourceWriterTest.scala`：检查是否有包名硬编码（预期无，它测的是文件系统操作）

**验证**：

- `gradle compileSymbolgenScala` 通过
- `gradle testSymbolgen` 全绿
- `gradle compileCodegenScala` 通过
- 确认 core classpath 干净：core source set 不依赖 Minecraft/Cats/JavaParser
- `git diff --check` 无空白错误

### Step 2：清理扫描层

为 job 抽象扫清类型障碍，三件事关联紧密，一起做。

**2a. 删除 alias 机器**：

- 删 `src/symbolgen/scala/com/pixdane/gregicality/symbolgen/gtceu/scan/materials/MaterialAliasScanner.scala`
- 删 `src/symbolgen/scala/com/pixdane/gregicality/symbolgen/gtceu/scan/materials/MaterialAliasResolver.scala`
- `ScannedRefs.scala`：删 `ScannedMaterialAliasRef` case class
- `GtceuScanDiagnostic.scala`：删 `AliasCycle`、`UnresolvedAlias` 两个 case 及其 render 分支；
  `fromCategories` 删 `aliasCycles`/`unresolvedAliases` 参数
- `MaterialDiagnostics.scala`：`duplicateAssignments` 删 `aliases` 参数，只看 assignments
- `MaterialExpressionParsers.scala`：删 `materialReferenceName`（确认只被 alias 用）；
  `rejectedValueReason` 删 alias 分支（`case _: NameExpr | _: FieldAccessExpr` 那段）
- `MaterialScanModel.scala`：删 `LocatedMaterialAlias`
- `MaterialScanner.scala`：删 alias 相关逻辑（`aliases`、`aliasResolution`、
  `aliasDiagnosticNames`、`resolvedRefByName` 里 alias 部分）；`declaredAssignments` 直接用
  `builderAssignments` 的结果

**2b. 砍 `GtceuScanResultOps`**：

- `GtceuScanDiagnostic.scala`：删 `object GtceuScanResultOps`
- `MaterialScanner.scala`：`GtceuScanResultOps.clean(declaredAssignments)` ->
  `Ior.right(declaredAssignments)`；`GtceuScanResultOps.withDiagnostics(nec, declaredAssignments)`
  -> `Ior.both(nec, declaredAssignments)`；删 `GtceuScanResultOps` import

**2c. 类型改名与结构整理**：

- `ScannedRefs.scala`：`ScannedRegisteredMaterialRef` -> `ScannedMaterialRef`
- 全局更新引用：`RefObjectRenderer.scala`、`MaterialAliasResolver.scala`（本步已删）、
  `MaterialAssignmentScanner.scala`、`MaterialDiagnostics.scala`、`GtceuRefScanner.scala`、
  `GtceuSourceScanners.scala`、测试文件
- `GtceuScanDiagnostic.scala`：`MaterialIdOccurrence` 从文件末尾挪到 enum 定义前
- `GtceuRefJob.scala`：`Materials`/`Paths` case 的 `target` 加 `override val`，删
  `def id` 和 `def target` 两个手写 match（enum trait 声明 `def id: String`、
  `def target: RefObjectTarget`，case 用 `override val` 自动实现）

**测试同步**：

- `GtceuSourceScannersTest.scala` 删除 alias 相关测试（约 6 个）：
  - `scanGtMaterialsSkipsDeprecatedAliases`
  - `scanGtMaterialsRejectsAliasThroughAnotherOwner`
  - `scanGtMaterialsResolvesMaterialAliasesWithoutTreatingTheirIdAsDuplicate`
  - `scanGtMaterialsReportsAliasCycleAsDiagnostic`
  - `scanGtMaterialsReportsUnresolvedAliasTargetAsDiagnostic`
  - `scanGtMaterialsKeepsResolvedPartialRefsWhenDiagnosticsExist`
- `GtceuSourceScannersTest.scala`：`ScannedRegisteredMaterialRef` 引用改名
  `ScannedMaterialRef`；import 同步
- `RefObjectRendererTest.scala`：删 `ScannedMaterialAliasRef` import 和使用它的测试数据
  （第 39 行的 `ScannedMaterialAliasRef(...)` 构造）；`renderWithIdRefObject` 测试的预期
  字符串里删 `Charcoal` alias 成员的渲染段；`ScannedRegisteredMaterialRef` 改名
- `materialDiagnostics` 辅助方法：检查 `Ior.Both` 分支是否还有消费方（删 alias 测试后
  可能只剩 duplicate assignment/id 的 Both 场景，保留方法但确认仍有调用）
- `assertUnsupportedMaterialAssignment` 辅助方法：检查 `reason` 参数是否还包含 alias 相关
  字符串（如 "alias target is not a member"），如果是则删对应测试或调整断言

**验证**：

- `gradle testSymbolgen` 全绿
- 无残留 alias 引用（`rg -n 'Alias|alias' src/symbolgen src/symbolgenTest` 仅剩
  `GtMaterialsScanSpec.assignmentDir` 之类无关项）
- `git diff --check` 无空白错误

### Step 3：job 三段式类型参数化

核心结构变更。

**代码改动**：

- 新建 `src/symbolgen/scala/gregicality/symbolgen/job/SymbolJob.scala`：
  ```scala
  package com.pixdane.gregicality.symbolgen.job

  import cats.data.IorNec
  import com.pixdane.gregicality.symbolgen.archive.SourceArchive
  import com.pixdane.gregicality.symbolgen.render.{GeneratedScalaFile, RefObjectTarget}

  final case class SymbolJob[E, A, B](
      id: String,
      target: RefObjectTarget,
      scan: SourceArchive => IorNec[E, A],
      preprocess: A => IorNec[E, B],
      render: (RefObjectTarget, B) => GeneratedScalaFile
  ):
    def run(archive: SourceArchive): IorNec[E, GeneratedScalaFile] =
      scan(archive).flatMap(preprocess).map(b => render(target, b))
  ```
  （`SymbolJob` 放 `job` 子包，与 `pipeline` 平级但语义更明确；若用户倾向保留 `pipeline`
  包名则放 `pipeline`）

- `MaterialScanner.scala` 拆分：
  - scan 部分：提取 declarations（`MaterialDeclarationScanner.scan`）+ assignments
    （`MaterialAssignmentScanner.scanAssignments`）+ rejected
    （`MaterialAssignmentScanner.scanRejected`），产出原始集合作为类型 `A`
  - preprocess 部分：duplicate assignment 检测、duplicate id 检测、missing assignment 检测、
    rejected assignment 整理、排序，产出 `Vector[ScannedMaterialRef]` 作为类型 `B`
  - `A` 的具体类型：可以是 case class 封装三个集合，或直接 tuple；倾向 case class
    `MaterialScanRaw(declarations, assignments, rejected)` 放 `MaterialScanModel.scala`

- `GtceuRefJobs.scala` 改写：每个 job 构造为 `SymbolJob[GtceuScanDiagnostic, A, B]` 实例，
  scan/preprocess/render 字段填具体函数引用：
  - materials job：`scan = MaterialScanner.scanRaw`、`preprocess = MaterialScanner.preprocess`、
    `render = RefObjectRenderer.generateMaterialFile`
  - paths job：`scan = StaticFieldScanner.scan`、
    `preprocess = (refs: Vector[ScannedPathRef]) => Ior.right(refs)`（identity）、
    `render = RefObjectRenderer.generatePathFile`

- `GtceuPipelines.scala` 改写：用 `SymbolJob.run` 替代 `GtceuRefScanner.scan + GtceuRefProcessor.render`
  ```scala
  val jobs: Vector[SymbolJob[GtceuScanDiagnostic, ?, ?]] = GtceuRefJobs.jobs
  def generate(archive: SourceArchive): IorNec[GtceuScanDiagnostic, Vector[GeneratedScalaFile]] =
    jobs.traverse(_.run(archive)).map(refs => refs :+ aggregateFile)
  ```
  （注意 `SymbolJob[GtceuScanDiagnostic, ?, ?]` 的存在类型，traverse 需要类型推断辅助；
  若编译困难，可显式写两个 traverse 或用 `SymbolJob[GtceuScanDiagnostic, ?, GeneratedScalaFile]`
  统一 O 类型）

- 删 `GtceuScannedRefs`（在 `GtceuRefScanner.scala` 里）- 整个 enum 删除
- 删 `GtceuRefScanner.scala`（整个文件，dispatch 由 job scan 字段决定）
- 删 `GtceuRefProcessor.scala`（整个文件，dispatch 由 job render 字段决定）
- `GtceuSourceScanners.scala` 保留，被 job 的 scan 字段引用；可能需要调整导出方法签名
  以匹配 job 的 scan 字段类型

- 评估 `Pipeline` case class：若 `GtceuPipelines` 不再用 `Pipeline`（改用 `SymbolJob.run`），
  且无其他消费方，则删 `Pipeline.scala` 和 `pipeline/PipelineTest.scala`

**测试同步**：

- `PipelineTest.scala`：若 `Pipeline` 删除则删整个测试文件；若保留则检查测试是否仍反映
  `Pipeline` 语义（若 `Pipeline` 不再被生产代码使用，测试无意义，删）
- `GtceuSourceScannersTest.scala`：评估测试入口
  - 若 `GtceuSourceScanners` 仍作为 dispatch 入口暴露且语义不变，端到端测试保留
  - 若 dispatch 消失、改为直接调 `SymbolJob.run`，测试改调 job 的 `run` 方法或分阶段测
    `MaterialScanner.scanRaw` + `MaterialScanner.preprocess`
  - 倾向：保留 `GtceuSourceScanners` 作为 scan 实现的聚合入口（不删），端到端测试通过
    `GtceuSourceScanners.scanGtMaterials` 调用，语义不变，测试基本不动
- 新增 `SymbolJobTest`（若需要）：测 `SymbolJob.run` 的链式语义--scan 成功 +
  preprocess 成功 -> Right；scan 成功 + preprocess 产诊断 -> Both；
  scan 产诊断 + preprocess 成功 -> Both（诊断累积）

**验证**：

- `gradle compileSymbolgenScala` 通过
- `gradle testSymbolgen` 全绿
- `gradle generateGtRefs` 通过（端到端验证生成链路）
- `git diff --check` 无空白错误

### Step 4：gtceu 包层级整理 + 小清理

job 抽象后 dispatch 消失，层级自然简化。

**包层级调整**：

- `GtceuScanSpecs.scala`（`StaticFieldScanSpec`/`GtMaterialsScanSpec`）下沉到
  `gtceu.scan` 包，和 scanner 同包
- `gtceu` 根只留：`GtceuRefJobs`、`GtceuPipelines`、`GtceuSourceScanners`
  （`GtceuSourceScanners` 是否留在根或下沉 `gtceu.scan` 视 Step 3 后结构定，
  倾向留在根作为 backend 对外入口）
- 更新所有 import 路径

**小清理**：

- 内联 `CodeLayout`：`RefObjectRenderer.refObjectLayout` 里
  `CodeLayout(prefix, separator, suffix).apply(entries)` 改成
  `prefix ++ ScalaCode.joinWith(separator)(items) ++ suffix`
- 删 `CodeLayout.scala`
- `generateGtRefs` 任务加 `inputs.files(symbolgen.runtimeClasspath)`：
  ```kotlin
  val generateGtRefs = tasks.register<JavaExec>("generateGtRefs") {
      // ...existing config...
      inputs.files(symbolgen.runtimeClasspath)
  }
  ```

**测试同步**：

- 测试 import 路径跟随 spec 包调整
- 若 `PipelineTest` 已在 Step 3 删除，本步清理残留引用（预期无）

**验证**：

- `gradle testSymbolgen` 全绿
- `gradle generateGtRefs` 通过
- 全量 build 通过：`gradle build`（或项目约定的全量验证命令）
- `git diff --check` 无空白错误

### Step 5：测试评估、删改、整理

等代码结构稳定后统一处理，避免反复改测试。

**评估维度**：

1. **覆盖完整性**：对照设计文档第 976-988 行列的维度（扣除已删的 alias 维度）：
   - scanner completeness（static fields、material declarations + assignments）
   - rejected AST shapes（非合法 builder、owner 不对、非 string id、wrapped builder）
   - duplicate assignment / duplicate registry id 检测
   - missing assignment 检测
   - accumulated diagnostics with partial refs
   - custom id factory owner
   - pipeline/job mapping（`SymbolJob.run` 链式语义）
   - lookup-index rendering（chunk 分割）
   - aggregate exports（`GTRefs`）
   - transactional replacement of generated output

2. **测试组织**：
   - `GtceuSourceScannersTest` 删 alias 后剩约 15 个测试，是否仍过重？
     若 materials 相关测试和 static field 测试可清晰分组，拆成 `MaterialScannerTest` +
     `StaticFieldScannerTest`（或保持一个文件，视实际行数和内聚度定）
   - 辅助方法 `materialRefs`/`materialDiagnostics`/`assertUnsupportedMaterialAssignment`
     归位：若拆文件则移到对应测试文件
   - `SymbolJobTest`（Step 3 新增）检查是否充分

3. **缺口补充**（若评估发现）：
   - `SymbolJob.run` 诊断累积语义（scan Both + preprocess Both 的累积行为）
   - preprocess 共享场景（虽当前 GTCEu paths job 的 preprocess 是 identity，但可测
     `SymbolJob` 接受任意 preprocess 函数）
   - 新包结构下的边界（core 零依赖、symbolgen 不反向依赖 codegen）

**执行**：

- 通览所有测试文件，删冗余、补缺口
- 若拆分测试文件，更新 import 和辅助方法归属
- 重组后确认每个测试方法名清晰反映被测行为

**验证**：

- `gradle testSymbolgen` 全绿
- 覆盖设计文档列的所有维度（扣除 alias）
- 无过时测试（无 `@Ignore`、无注释掉的测试、无空测试方法）
- `git diff --check` 无空白错误

### Step 6：文档整理

**更新 `docs/compile-time-scala-dsl-design.md`**：

- 第 678-720 行附近（source set 与包结构描述）：
  - 新增 `core` source set 描述：`src/core/scala`，包 `com.pixdane.gregicality.core.refs`，
    零外部依赖
  - 包名 `codegen.dsl.model` -> `com.pixdane.gregicality.core.refs`，
    `codegen.dsl.refs.gtceu` -> `com.pixdane.gregicality.core.refs.gtceu`
  - 依赖图更新：`core <- symbolgen <- generateGtRefs <- codegen`
  - core 两条 invariant 写入

- 第 720-790 行附近（扫描模型与 job 描述）：
  - 删除 alias 相关描述（alias resolution、cycle、unresolved）
  - `GtceuRefJob` 描述更新：job 持有 scan/preprocess/render 三段函数，不再只持 spec + target
  - `GtceuScannedRefs`/`GtceuRefProcessor`/`GtceuRefScanner` 描述删除，替换为
    `SymbolJob.run` 链式描述
  - `ScannedRegisteredMaterialRef` -> `ScannedMaterialRef` 改名同步
  - `GtceuScanResultOps` 删除，调用点用 `Ior.right`/`Ior.both` 的描述

- 第 870-1000 行附近（source set 图与 generator requirements）：
  - source set 图更新（加入 `core`）
  - `generateGtRefs` 输入声明：`inputs.files(symbolgen.runtimeClasspath)`
  - `CodeLayout` 删除说明（若有提及）
  - `TagPrefixRef` 删除说明（若有提及）

- 第 976-988 行附近（测试维度）：
  - 删除 alias 相关测试维度（canonical material aliases、alias cycles、unresolved targets）
  - 保留其余维度

- 新增"被拒绝的方案"段落：
  - 类型别名 wrapper（`GtceuScanResult` 升级为 case class）：为名正言顺付 typeclass 实例
    维护成本，不划算，保留类型别名
  - alias 机器保留：为假设需求造抽象，无实际收益，删
  - `core` 寄进现有 source set：每个选项都污染宿主或成环，独立 source set 是最干净解

**同步**：

- 包结构表、依赖图、文件清单与代码实际一致
- 记录重构决策与理由，供未来 agent 继续

**验证**：

- 文档与代码一致（包名、文件路径、类型名、依赖关系）
- 无过时描述（alias、`GtceuScanResultOps`、`GtceuRefProcessor`、`CodeLayout`、
  `TagPrefixRef`、`Pipeline` 等已删项无残留提及）
- `git diff --check` 无空白错误

## 执行顺序的理由

Step 1 改包名是机械的但影响全局，先做避免和后续结构性改动叠加；Step 2 清理类型障碍
让 Step 3 的抽象面对干净类型；Step 3 是核心结构变更；Step 4 在结构定形后整理层级；
Step 5 等代码稳定统一处理测试；Step 6 最后同步文档。

## 验证命令汇总

每步执行后运行：

- `gradle compileSymbolgenScala` -- symbolgen 编译
- `gradle testSymbolgen` -- symbolgen 测试
- `gradle generateGtRefs` -- 端到端生成（Step 1 起需要）
- `gradle compileCodegenScala` -- codegen 编译（Step 1 起需要，验证 core 依赖）
- `git diff --check` -- 空白错误检查

全量验证（Step 4、Step 6 后）：

- `gradle build` 或项目约定的全量命令

## 风险与注意事项

1. **`SymbolJob[GtceuScanDiagnostic, ?, ?]` 存在类型的 traverse**：Cats `traverse` 对
   存在类型可能需要类型推断辅助。若编译困难，备选方案是统一 O 类型为
   `GeneratedScalaFile`（`SymbolJob[E, A, GeneratedScalaFile]`），或显式写两个 traverse。
   Step 3 实现时验证。

2. **`MaterialScanRaw` 类型设计**：scan 产出 declarations + assignments + rejected 三个集合，
   用 case class 封装还是 tuple？倾向 case class，放 `MaterialScanModel.scala`，字段名清晰。
   Step 3 实现时定。

3. **`GtceuSourceScanners` 去留**：Step 3 后若 job 直接引用 `MaterialScanner.scanRaw` 和
   `StaticFieldScanner.scan`，`GtceuSourceScanners` 作为中间 dispatch 层可能多余。但保留它
   作为 backend 对外聚合入口（测试端到端调用）有价值。Step 3 实现时定，倾向保留。

4. **`Pipeline` 去留**：若 `SymbolJob.run` 完全替代 `Pipeline`，且 `SymbolgenDomain` 改用
   `SymbolJob`，则 `Pipeline` 无消费方。但 `SymbolgenDomain.generate` 签名是
   `SourceArchive => IorNec[String, Vector[GeneratedScalaFile]]`，与 `Pipeline[I, E, O]` 不同构。
   需确认 `Pipeline` 是否真的无消费方再删。Step 3 实现时验证。

5. **测试拆分粒度**：Step 5 评估时若 `GtceuSourceScannersTest` 仍可读则不拆，避免过度拆分
   增加文件跳转成本。

6. **未提交的 `build.gradle.kts` 改动**：当前工作树有用户之前的依赖顺序调整，保留不动，
   不纳入本次重构 commit。若重构需要动 `build.gradle.kts`（预期不需要，构建改动在
   `codegen.gradle.kts`），单独说明。
