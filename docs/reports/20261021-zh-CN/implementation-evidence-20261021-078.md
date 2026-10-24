# #78/#93 实现证据 — 2026-10-21

> 本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
> [英文原件](../../../implementation-evidence-20261021-078.md)；原文 SHA-256：`3d79e7e4f0ddafe66fc74e5aef62577b2edb7d839cd95d34c03457736c4e50ee`，见 [source-manifest.json](source-manifest.json)。

状态：T009–T011 已编写；仅完成源码／边界检查。不声称已完成编译、运行检查、行为 RED（修复前失败）／通过、设备、凭据、Git 修改或发布。PM（项目负责人）指定 `commit_pe_audit` 在本次交接后作为唯一 Gradle 负责人。

依据：`receipt-20261021-078-g4.md`、`execution-brief-20261021-078-r3.md`、C-078-R3、ADR r3.2（架构决策记录）、根规范第 2 版。原始实际测试的 PE（独立工程审查）接受记录为 `review-actual-test-adequacy-20261021-078.md`；#95 新增检查在对应产品修复之前已由 `review-actual-test-adequacy-20261021-078-095.md` 接受。

## 绑定源码身份

- `shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt`：`c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414`
- `shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt`：`1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d`
- `shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt`：`00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c`
- `shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt`：`b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42`

## 改动与调用方／锁审计

- 令牌：retire 与 withAdmittedMutation 共享同一个私有监视器；布尔准入、有限的本地闭包，不新增外部操作。所有其他控制器字节（包括 #89 遮罩处理）保持冻结。
- 导航启动：在锁外预先计算应用上下文和可用性；在独立区段中分别使用 navigation → token → song（导航 → 令牌 → 歌曲）及 navigation → token → overlay（导航 → 令牌 → 覆盖层）。所有权变更推进导航世代；同一所有者保持世代不变。工作器 start/submit/clear 仅在令牌／状态释放后使用 navigation → worker（导航 → 工作器）。启动钩子在初次所有者注册和最终工作器所有者重检之间无锁执行（#93）。
- 路线输入：严格处理显式空所有权；先捕获准入，再执行无锁修改钩子；navigation → token → overlay 提交将未修改的解析器应用一次。已接受的重复／NONE 帧保持解析器的新鲜度行为。Handler 投递在锁外进行。
- 覆盖层刷新／监听器：管理刷新捕获当前所有者；显式过期刷新被拒绝。发布携带所有者／世代及监听器／修订戳。Handler 记账在 navigation → token → overlay 下验证；监听器回调位于所有锁之外。终止交付使用精确的无所有者终止世代，不使用已退役的普通准入。
- #95：已投递 runnable（可运行任务）未通过令牌准入后，navigation → overlay 仅清除其精确已消耗票据／待处理身份。它绝不交付内容，也不更新路线／最后已交付状态。这防止替换／终止发布继承一个实际不存在的已投递 runnable。同契约测试覆盖替换和关闭后继。
- 导航结束：在 navigation → token 下退役，然后分别进行本地歌曲终止转换和覆盖层转换。不嵌套 song/overlay。工作器 clear 仅在令牌／状态释放后入队。歌曲终止意图派发和通话结束包装调用均无锁执行。
- #96：初始转换返回不可变的原始已结束令牌；只有该令牌非空且终止所有权重检通过时，通话结束才接收它。不重新读取可变 lastEndedToken，不通过空值回退进入冻结通话子系统。生产调用方搜索发现，唯一会话启动调用位于 CarPlayController，并传入真实签发令牌；真正无所有者的导航清理没有需要退役的有所有者通话会话。冻结的 BydCarPlayCall 未修改，其内部实现未获得新的认证。
- 直接歌曲路径：在状态锁下捕获所有者，释放后进入 token → state（令牌 → 状态），并精确重检所有者／世代。帧准入钩子、偏好、execute/schedule、命令边界和日志保持无锁。普通帧／空令牌接受规则使用已存储所有者，包括已退役但仍存储的所有者。
- 歌曲 B 转换清除解码器元数据／wanted（期望值）／note（提示）／change（变更）／announced（已宣告）意图，并推进本地世代／修订号；同所有者启动是幂等的。最后成功显示的卡片、所有者戳及 firstLogged 仍属于物理完成记账；它们不是 B 继承的解码器元数据。有所有者的同文本输出要求所有者／世代匹配。B 无元数据或输出禁用时，不承诺物理清除卡片。之后有效的清除仍能看到先前物理卡片。
- 歌曲意图：在无锁调度器派发前、外部调用进入前、接受结果后，检查所有者／世代／修订号／卡片／上下文。计时器另需匹配精确存储的计时器身份；提交前获取当前偏好快照。在派发检查之后退役的狭窄竞态窗口可能使无效任务入队；执行／完成检查会拒绝它。终止意图使用精确无所有者世代。任何过期成功完成均不会修改 shown（已显示状态）／所有者戳／firstLogged。
- 歌曲 attach 只是赋值已解析的应用上下文。ownerStarted/ownerEnded 仅含本地状态操作；返回的终止派发稍后在导航／状态锁外执行。
- 夹具 API 保留真实导航工作器及真实歌曲写入器／计时器／完成逻辑。测试导航端口强制使用精确的标准异步有界执行器结构。辅助地图／通话初始化／启动／结束及 OEM 桥接动作，仅在模拟环境安装期间被替换／抑制。生产调度器绝不关闭；静止状态下的夹具重置会清除既有／新增状态，并恢复上下文／handler／时钟／端口／默认委托。

使用的允许锁边为：controller resource/lifecycle → token（控制器资源／生命周期 → 令牌，冻结调用方）；call lifecycle → token（通话生命周期 → 令牌，冻结调用方）；worker monitor → token（工作器监视器 → 令牌，冻结调用方）；navigation → token → overlay 或 song；直接歌曲路径 token → song；令牌／状态释放后的 navigation → worker。未引入 state → token/navigation、token → controller/call/worker/navigation 或嵌套 song+overlay 锁边。

## 冻结边界检查

基线：`checks/078-execution/source-baseline-20261021.json`。已验证以下区域 SHA 精确相等：

- 令牌类声明之前的所有字节：`66d64253b89fd863beac924dadce9f1fc08107df76258849ca60b78b60ece93b`。
- 从 internal fun isWirelessHandoffInProgress 前的换行开始的所有字节：`3b5ac25d15149de4c5c839406186a6a169eeea82128e2a4da88200d45acd38b7`。
- internal object BydClusterSong 声明之前的所有字节（纯解码器）：`e4006fd1cab98fafde467f81c246180d3b00c67c6335bbd90e06978ef13aec4e`。
- 从 internal object ClusterSongWriteResult 声明开始的所有字节（供应商结果／工具）：`a966291fa94da85e9ead3965b878edbbf60ee2aff30b74ad277eef153071934e`。

`git diff --check` 通过。解码器／结果解析器／工具后缀以及令牌类外的所有控制器字节均与基线匹配。原始 26 测试文件保留在 `checks/078-execution/RouteSongRetirementTest-before-095.kt.txt`，SHA 为 `765eb427c28aa5892f211d9909269300dadf898723f93150f18065bbc1ca7a06`。当前文件只新增已接受的 #95 回归（27 个 @Test 方法）。原始测试编写证据和机械性修正证据保持不变。本实现代理未编辑其他产品／测试源码。

## 剩余验证

指定测试人员必须编译／运行全部 27 项实际契约检查、未修改的歌曲提示／计时器及相关回归，以及精确的 AGENTS Gradle 命令。独立 PE/SecOps 必须绑定实际最终源码／结果身份。本地证据不能证明手机／车辆／OEM 行为或已进入的外部命令的物理取消能力。
