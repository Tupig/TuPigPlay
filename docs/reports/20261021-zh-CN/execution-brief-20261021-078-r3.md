# 执行简报 — #78/#93 R3

> 本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
> [英文原件](../../../execution-brief-20261021-078-r3.md)；原文 SHA-256：`9dde0269992c273c32d5adaba667a168bcccb0ed8b83741dc0e87b4644c7349e`，见 [source-manifest.json](source-manifest.json)。

创建时间：2026-10-21。风险：high/critical（高／关键）。人工 G1/G2/G3 已批准；请求的下一决策是执行授权（Execution Authorization，G4-078-R3）。本简报创建时尚无 G4 授权。这是已批准不可变工作包中的 T005。

## 目标与依据

交付限定范围内的旧会话路线／歌曲修改修复及 #93 延迟导航启动修复，保持当前会话行为有效，并提供完整本地证据。根目录 spec.md 第 2 版是唯一产品依据。本简报应用已批准的 C-078-R3 和 T006–013，不更改需求、断言、API 或源码边界。历史记录保留其创建时的状态；可变进度仍记录在 status.md 中。

| 绑定输入 | SHA-256 |
| --- | --- |
| spec.md | 1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585 |
| build-package-20261020-078-r3.md | 96d021ceb04978f997faf71b12803f424f7a2246b9057c7e74fcc153afa9b625 |
| receipt-20261021-078-g3.md | aaa5e7a25e7b440a1b1e1ae6116d263316f73614adad659416817ee0234bbdc2 |
| inventory-20261020-078-g3.json | faccfcf9b3effb5cbe4bea1c10e4b73d52bd9eed183802ec5fe35775fa698365 |
| review-pre-code-pe-20261020-078-r3.md | d1cff53082b27acbc2a48cfc92d2e21a597f52af19bcf85153e739b936fa841d |
| review-pre-code-secops-20261020-078-r3.md | 68ef918697381cca4f0c8d8e72be7f0b91ff691df1282f4e75e0677286cc1af1 |
| review-pre-code-secops-20261020-078-r3-erratum.md | 4ffad1ac29d66bdd8fa5258eb74597fea04a6c9ae2c2cfbdb30cb4c4538b0e48 |
| review-pre-code-reconciliation-20261020-078-r3.md | fd6e7d5fac5bc3bfb2fc14cbc8c5ca7f2c0df7a6e57012303dcad4f1c741f3f7 |
| specs/001-lifecycle-display-privacy/increments/078/contracts/retirement-r3.md | b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83 |
| specs/001-lifecycle-display-privacy/increments/078/tasks-r3.md | 770c3d9696bdbc0ded79e4780b63fe229ab7ae447c77864a0eff9ae45237c900 |
| specs/001-lifecycle-display-privacy/increments/078/quickstart-r3.md | 7f97a1b8e1d732c33f1d7d7cbe58dcd031777d4f8f3f90849ce8bab70246bbd8 |
| checks/078-execution/source-baseline-20261021.json | 03c276c931230dad55ee37b684226bb741a7aeff1750ae9afbf24c1bb504a553 |

## 可编辑文件与冻结区域

1. shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt：唯一新增测试文件，目前不存在。测试作者只能在明确 G4 批准后、任何产品改动之前创建它。
2. shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt：仅允许修改 CarPlayRetirementToken 类体。类外每个字节均须保留，包括导入、控制器生命周期和 #89 遮罩行为。
3. shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt：已批准的所有者／状态／发布／启动／终止顺序及精确测试支持。
4. shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt：对象内部已批准的歌曲所有权／缓存／计时器／完成逻辑及测试支持。保留 BydClusterSong 之前的整个前缀（包括导入／解码器）以及从 ClusterSongWriteResult 开始的后缀（供应商／工具）。必要时在允许区域内使用新类型的完全限定名。

所有其他产品代码／测试均冻结，包括工作器、路线解析器、既有夹具、#71 隐私改动、构建配置和设置。保留无关工作树改动和 .deveco。治理／验证记录单独编写；它们不授予新的产品文件边界。既有原始三个文件的字节快照和四个冻结区域摘要位于绑定清单中，其 SHA 与已审查源码相等。实际创建测试之前、首次编辑产品代码之前都应立即重新核查；出现漂移则停止。

## 工作顺序、角色、进入与退出条件

各角色使用 HITL（人工决策者）已批准的当前会话等价模型。PM（项目负责人）不直接实现，只验证证据。测试人员编写实际检查；限定范围的编码人员修改产品；独立 PE（工程审查）／SecOps（安全审查）审查证据。不并发编辑产品文件；由一名测试人员独占管理 Gradle 输出。

| 阶段／Spec Kit 任务 | 角色 | 进入条件 | 必须执行的操作与退出条件 |
| --- | --- | --- | --- |
| 记录执行／T005 | PM | 明确人工 G4 | 编写带日期的 G4 回执，包含本简报及规范／工作包／清单身份；验证原始快照及新增测试不存在。 |
| 实际检查／T006 | 测试人员 | G4 回执及身份检查 | 在唯一允许文件中用精确计划 API 编写完整 C-078-R3。覆盖全部 10 项 AC（验收条件）及 16 个方法组，包括各变体。保持三个产品输入不变。 |
| 检查充分性／T007–008 | 独立 PE、PM | 完整实际测试源码 | 对照契约检查实际断言／正向对照／夹具／清理；在配对编辑之前接受源码充分性。明确记录对计划 API 的编译依赖。源码充分性不等于运行证明。 |
| 令牌基础机制／T009 | 编码人员 | 实际检查获 PE 接受（ACCEPT），产品基线未变 | retire 与 withAdmittedMutation 使用共享监视器，isAdmitted 非阻塞，并提供有界持锁观察；控制器类外字节不变。 |
| 导航及 #93／T010 | 编码人员 | T009 | 保护状态／意图提交；传递生产者令牌；拒绝有所有者时的空令牌输入；实现精确的所有者／世代启动／结束／刷新／发布顺序及已审查辅助处理。真实工作器实现保持不变。 |
| 歌曲生命周期／T011 | 编码人员 | T010 | 在缓存、设置／提示／计时器／写入／清除完成处理中保持所有者／世代／修订号校验，实现替换清理及精确命名的支持。保留冻结字节和既有字段／包装方法签名。 |
| 实际验证／T012 | 测试人员 | 配对实现完成 | 编译／运行完整聚焦契约、有意义的未修改回归测试及完整 AGENTS 任务列表。检查 XML／日志／lint／打包输出。不跳过契约检查，不捏造 RED（修复前失败）结果。 |
| 独立结果审查／T013 | 独立 PE/SecOps、PM | 完整执行证据及最终哈希 | 核查运行充分性、全部修改／调用方、最终限定范围 PII（个人身份信息）、冻结边界及 AC->证据对应。准备本地 G5 验收包；不自动验收或交付。 |

G4 批准后，应推进这些内部阶段，不对范围内的普通实现选择反复提问。独立充分性审查仍是硬性依赖。范围／契约冲突或未解决隐私发现须进入规定的变更控制；保持已批准契约的常规实现修正需接受适当独立审查并重跑验证。

## 不可放宽的实现与检查细节

- 使用由真实控制器签发、通过合法且未持锁的导航钩子捕获的 A/B 令牌。不得通过构造令牌／反射私有令牌／写入所有者来制造新测试场景。既有冻结底层测试保留历史夹具，不得用于替代本次新增证明。
- 所有暂停钩子、监听器、handler 投递、偏好读取、外部调用和等待均须位于受保护的令牌／状态提交之外。顺序令牌操作和持锁观察结合独立的共享同一监视器源码证明；不得虚构强制同时争用证据。
- 保持已批准锁 DAG（有向无环图），禁止 overlay（覆盖层）／song（歌曲）嵌套。仅在释放令牌／状态锁后，才允许导航 -> 工作器入队，且使用标准有界异步工作器。非内联执行意味着回调线程本身不持有相关监视器；它并不意味着另一线程开始运行前提交线程已释放所有监视器（适用 SecOps 勘误）。
- 暂停的旧生产者必须在实际替换／结束／关闭后失去最终本地修改准入；有效 B 输出是负向检查成功的前提。获接受的重复／NONE 路线应用可刷新隐藏时间状态；只有被拒绝的输入必须完全不产生应用／状态／意图效果。
- 严格拒绝有所有者会话的空生产者，与管理操作捕获所有者及有效独立输入分别处理。替换会清除 A 的歌曲元数据和计时器／提示意图；仅含播放状态的 B 不能继承 A 的标题／歌手。
- #93 钩子位于初次导航准入之后、最终工作器派发重检之前，且在锁外执行。迟到 A 释放前后，B 必须已启动、持续获准并产生有效帧；重复启动 B 保持行为。不得仅使用模拟工作器。
- 通过模拟外部命令结果运行真实歌曲写入／清除逻辑。在单写入器上，阻塞命令不能与虚构的 shown=B 状态同时存在。B 的 cache/wanted（缓存／期望值）可以推进；必须在 A 返回后证明 B 有效完成。
- 零新增入队断言适用于契约中的拒绝输入／过期完成调度。在解锁派发后发生竞态的先前已捕获包装任务可以入队，但执行时必须拒绝。不新增生产总队列内存保证，不声称物理取消供应商操作。
- 在构造控制器前安装模拟外部边界。保留目标真实本地生命周期／工作器／歌曲逻辑。保持模拟边界安装状态直至排空回调；只终止测试自有执行器，再恢复单例字段、handler／时钟／钩子／上下文／偏好。不得泄出 shell／OEM／设备动作。保留既有独立运行 shown 同值行为及反射可见的精确签名；即使文本相同，有所有者的 B 仍需自身有效命令完成。

尚不存在的未来支持 API 可能使新增测试在配对实现前无法编译。不得引入不安全桩件或单独的测试接缝补丁来制造基线。在源码充分性审查中明确标注该依赖。不承诺新契约修复前的运行 RED。实现后，所有检查必须真实编译并运行；夹具／编译／超时问题均属失败。

## 必需命令与结果证据

使用计划中的本地 JBR 和 Android SDK、唯一的运行日志位置及一位 Gradle 负责人。执行前核实二进制／SDK 存在，不改动项目配置。保留每次运行的退出状态和 XML。聚焦运行：

```sh
JAVA_HOME="${JAVA_HOME:?Set JAVA_HOME}" \
ANDROID_HOME="${ANDROID_HOME:?Set ANDROID_HOME}" \
./gradlew :shared:testDebugUnitTest \
  --tests 'com.shilapi.xcertplay.hud.RouteSongRetirementTest' --rerun-tasks
```

随后执行 quickstart-r3.md 中精确列出的相关未修改测试类；若本次完整套件执行已证明要求的回归，则复用该执行证据，避免无故重复运行。强制完整 AGENTS 命令：

```sh
JAVA_HOME="${JAVA_HOME:?Set JAVA_HOME}" \
ANDROID_HOME="${ANDROID_HOME:?Set ANDROID_HOME}" \
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :home:testDebugUnitTest \
  :mobile:lintDebug :home:lintDebug :maphost:lintDebug \
  :mobile:assembleDebug :home:assembleDebug :maphost:assembleDebug
```

要求聚焦契约的实际 XML 中失败／错误／跳过均为零，包含有效正向对照及完整 10-AC 追踪。报告实际方法数，不将 16 个文字分组假设为 16 项已执行用例。记录完整命令退出码／BUILD SUCCESSFUL（构建成功）以及各单元测试／lint／打包结果，区分 UP-TO-DATE（已是最新）／no-source（无源码）与实际执行。说明无关的既有跳过；新契约不允许跳过。执行公开树筛查（含拟新增文件）、git diff --check、冻结区域／边界检查和上游推送禁用验证。最终 PE 验证共享同一监视器／调用方的源码证明及每条本地修改路径，而不只看测试数量。最终 SecOps 检查实际源码、夹具和证据中的可识别信息日志泄漏。

交付简洁的 AC->用例／源码审计->执行证据矩阵，以及精确源码／测试哈希。这只是本地验收证据。工作树验证通过不能取代 #92 后续刷新的隔离候选验证，也不能证明车辆／iPhone／OEM 兼容性。

## 停止、修正、回滚与授权限制

出现第四个产品文件、冻结测试／导入／解码器／供应商区域变更、语义断言／API／契约变更、真实外部动作、身份漂移或疑似隐私泄漏时，停止受影响工作。报告精确证据，并单独跟踪每个新需求／缺陷；属于已有范围的发现应在既有 issue 中评论。不得为适配代码而降低预期。同契约机械性修正需要审查者确认及新的完整契约证据；语义变更触发 Aegis 下游重建控制及受影响门禁。

回滚前验证精确差异，只恢复本增量捕获的原始三个文件字节及其新增测试改动；不得重置到 HEAD、清除前序改动或更改 .deveco。保留全部旧证据，包括 #91 的历史重建限制及 #89 的独立本地验收。

请求的 G4 涵盖完整限定范围的本地实际测试／修复／验证及实现后审查，直到形成可审查的 G5 验收包。G5 验收、刷新后的 #92 隔离交付、暂存／提交、推送／PR／合并、issue 关闭、签名／发布、运行时晋级及真实车辆／设备操作仍属后续决策。绝不访问第三方 MFi／私钥／APK 载荷，绝不向 upstream 推送。
