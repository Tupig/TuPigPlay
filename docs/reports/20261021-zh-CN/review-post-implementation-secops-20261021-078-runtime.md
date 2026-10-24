# 独立 SecOps 运行证据补充 — #78/#93

> 本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
> [英文原件](../../../review-post-implementation-secops-20261021-078-runtime.md)；原件 SHA-256：`13cb266641fe83fc145f29121389238f02c99e1b4923e2d8633c31914dda981f`。

日期：2026-10-21。审查人：`secops_078`，独立于产品/测试作者和 Gradle 执行者。范围：T013，对保留的本地执行证据及未改变的已审查源码进行审查。本审查人未重新运行测试。

**结论：接受（ACCEPT）以下身份对应的限定范围 SecOps 源码和运行证据审查。** 在下述明确的复用与硬件限制下，此前仅完成源码审查时保留的运行验收事项，现已由检查过的聚焦/完整证据解决。未发现未解决的安全/隐私问题。这不授予 G5、关闭 issue、暂存/提交/推送、公开发布证据、发布版本或 #92 隔离候选验收。

## 源码与审查沿革

| 当前源码 | 独立重新校验的 SHA-256 |
| --- | --- |
| shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt | c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414 |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt | 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt | 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c |
| shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt | 561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e |

保留的源码审查：`review-post-implementation-secops-20261021-078.md`，SHA-256 `6e112d463b491602409e95f2870c47f07b6d9b52ef3bde8309f045c67ac07d5c`。机械修正 #97 补充：`review-post-implementation-secops-20261021-078-097.md`，SHA-256 `cb7d93edb49c604dbf37aa4bb81b75a65d0c51c9dad1a23d972e7767de6fd4c2`。其中的源码/锁/冻结区域/夹具分析仍适用；未发现新的源码变更。

已检查的最终测试者证据：`test-runtime-evidence-20261021-078.md`，SHA-256 `d4498cd6cbcddf59540b5bf33e531c1d12bace460b7e5a060a4989c39ca1e147`。审查人还检查了底层结果 JSON、日志和 XML，而非仅依赖测试者结论。

## 绑定的执行证据

成功证据根目录：`checks/078-execution/runtime-20261021-021513`。

| 相对此根目录的产物 | 已验证 SHA-256 |
| --- | --- |
| source-identities.json | 89080afcd679e89d69688a581d3ac62427a8f4edbb668cd6a44cb0154cfb51fd |
| focused.log | a4a11d3d2f23060a9c86481bf1b103c43c32a2154c757204cf7fb41eabeea18c |
| focused-result.json | 389d1a50802cb66ddf8315299989b5b9bfe78b151bebd3888bc4be1d69027595 |
| focused-artifacts/test-results/testDebugUnitTest/TEST-com.shilapi.xcertplay.hud.RouteSongRetirementTest.xml | d1f33adf3c5a491d282d582e3be6966c58bb9135b514cff13dda5b5a282813ac |
| full.log | 967366d4591dfd3911eac72bca102f71eaae203aea38bbc764f28f36b1403d2e |
| full-result.json | ef556dd9cc4d178857d36d8a149b79447737fcb6f5e53d6e94a3a214929fcfe5 |
| full-xml-manifest.json | 033c4069c3c9d5fc99c55c44b00247ec2e8e6953d6b6f7e5c3fd8d0c887872db |

已独立检查清单中全部 441 个 XML 路径，确认文件存在、未越出范围、不是符号链接且哈希匹配。当前四个源码哈希与 source-identities.json 一致。聚焦/完整结果记录显示退出码为 0，源码没有漂移。

较早的失败尝试单独保留：`checks/078-execution/runtime-20261021-021303/focused.log`，SHA-256 `5213cf82152651b3a10c484358816cc94295735a1c5334a3c24c837a59f9b5c9`。它记录的是测试编译失败，不是已执行的契约失败，也不是预期 RED。其旧 XML 未计入执行总数。已接受的 #97 限定名修正是该次尝试测试与成功后继版本之间唯一的源码增量。

## 独立检查的结果

| 证据 | 结果与解释 |
| --- | --- |
| 强制聚焦契约运行 | 日志显示 BUILD SUCCESSFUL，用时 12s；实际 XML 含 27 个不同用例，0 失败/错误/跳过，system-out/system-err 为空。涵盖两种令牌顺序、所有 route/song 生命周期屏障、同文本完成、#93、#95 和夹具重置。 |
| 精确 AGENTS 命令 | 结果 JSON 任务列表与所需命令一致；日志显示 BUILD SUCCESSFUL，用时 3m 29s。任务结果区分实际执行和 UP-TO-DATE。 |
| Shared 完整运行 | 240 个新生成 XML 套件，1573 条记录，0 失败/错误，1 项既有宿主 socket 假设跳过。新契约再次报告 27/0/0/0。 |
| Common 完整运行 | 200 个新生成 XML 套件，1527 条记录，0 失败/错误/跳过。 |
| Home 完整任务 | UP-TO-DATE；一个复用 XML 套件中有 6 条通过记录。它们不是新执行记录。 |
| 新生成记录总数 | Shared 加 common 共 3100 条记录，包含那 1 项假设跳过。不得将合并存储的 3106 条记录描述为全部新执行。 |
| Lint | Mobile 新生成 20 个警告；复用的 home/maphost 报告分别有 7/1 个警告。XML 严重性检查仅发现 warning，没有 error/fatal。BUILD SUCCESSFUL 不表示没有警告。 |
| Assemble | Mobile 打包/组装实际执行；home/maphost 输出为 UP-TO-DATE。本审查人依据任务/输出元数据，未读取 APK 载荷或安装软件包。 |

跳过的 `AirPlayPortSelectorTest.fallsBackWhenAnotherListenerOwnsTheWildcardPort` 与源码中既有宿主通配/特定地址 socket 假设有关。它不免除任何退役契约用例。冻结的 song note/timer 套件分别新报告 5 项和 13 项通过，控制器生命周期报告 22 项通过且没有跳过。既有诊断隐私 canary 套件报告 shared 2 项和 common 4 项通过；合成夹具/canary 输入不被视为实际个人数据。

运行结果支持已批准的本地防护/所有权/完成/清理测试。它们不会把顺序令牌检查变成强制同时竞争实验；互斥论证仍结合这些已执行结果与此前独立的同监视器及调用方锁审计。#96 的具体支持证据仍是不可变 ended-token 源码证明和冻结的精确身份 call 准入，不是虚构的新增 call/OEM 运行测试。

## 运行隐私与安全审查

**限定范围检查已完成；未发现疑似产品 PII 泄露、凭据披露或新引入安全问题。** 范围为成功的聚焦/完整日志、聚焦测试 XML、所有保留的完整运行测试 XML、三份 lint XML 报告、此前失败的编译日志及测试者最终报告。其中共 448 个日志/XML 文件，派生摘要用于交叉核对。未审计二进制测试结果存储、重复的 HTML/SARIF 呈现、APK 内容、设备数据及无关仓库文件。

方法：解析全部测试 XML 结果和 stdout/stderr；分类非空输出流及异常/诊断类型；对照已审查的合成源码检查聚焦套件的空输出流和失败格式；有界筛查私钥块、常见凭据前缀、类似邮箱/MAC 的字面量和网络标识；核查源码/证据身份及清单完整性。仅位置的初筛没有把疑似值复制到持久记录中。

报告含有标准本地测试运行器主机名元数据及本地工具路径。它们是本地构建环境元数据，并非 route/song 载荷遥测，也不能证明报告可安全公开。完整原始 XML/日志仍是本地验收证据，不在拟议源码交付范围内；本审查不授权公开它们，也不授权将其纳入 #92。这里没有复述任何主机名/地址值。未来如提议发布原始证据，需要另行进行有界披露审查。

非空旧套件输出流是框架/资源诊断、固定样式/intent 警告、JVM 插桩通知及 socket 关闭堆栈。新增聚焦套件不输出 stdout/stderr。有界日志/XML 扫描未发现私钥/令牌/邮箱/MAC 字面量，也未发现新增 route/song 快照转储或业务载荷日志。合成断言/canary 仍以测试来源为识别依据；实际疑似泄露仍需遵循 Aegis P0/HITL 流程，不能以元数据或 canary 为由豁免。

## 本次接受结论的最终范围

安全和可观测性结论现由未改动的源码审查与已检查的本地执行证据共同支持。真实工作器/定时器、受控单写入器、回调失败报告和重复夹具重置均在已批准本地测试中成功执行。既有 lint/框架警告和复用任务仍明确保留。不作实测车辆延迟、真实 iPhone/OEM 兼容性、外部物理取消或总队列内存保证。

源码审查中的文档、回滚、源码边界和监控评估保持不变。PM 负责的公共目录树/空白/上游检查、PE 最终运行充分性、人工 G5 及后续 #92 隔离精确候选验证仍是各自独立的证据/决定。本补充不关闭任何 issue，#91 的历史来源限制不受影响。本审查人仅执行只读证据/源码检查并编写本补充。
