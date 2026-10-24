# #78/#93 运行时验证 — 成功的后继运行

本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
[英文原件](../../../test-runtime-evidence-20261021-078.md)；原文 SHA-256：`d4498cd6cbcddf59540b5bf33e531c1d12bace460b7e5a060a4989c39ca1e147`（见 [source-manifest.json](source-manifest.json)）。

日期：2026-10-21。独立测试者；唯一 Gradle 执行负责人。
结果：对于下列身份，**聚焦契约验证通过，精确完整 AGENTS 命令通过**。这是本地工作树验证，不是 G5、硬件验收、发布或 #92 隔离候选验证。

## 源码及机械修正沿革

| 输入 | SHA-256 |
| --- | --- |
| CarPlayController.kt | c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414 |
| BydNavigationOutputs.kt | 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d |
| BydClusterSong.kt | 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c |
| RouteSongRetirementTest.kt | 561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e |

在聚焦和完整执行前后均核对了全部四个身份；未发生漂移。测试／产品路径仍为获批执行简报中的路径。第 1 次尝试保留在 test-runtime-evidence-20261021-078-attempt1.md：该次测试编译失败，未执行新的契约用例。未使用其中的旧 XML。

对于 #97，测试者独立比较了保留的修正前测试快照和当前文件：唯一差异是将 controllerLocks 的初始化器限定为 kotlin.collections.emptyList()，以避开同名路线帧辅助方法。**机械源码充分性：接受（ACCEPT）**；未改变断言、契约、夹具调度或产品源码。修正证据为 test-mechanical-correction-20261021-078-097.md，SHA-256 为
45e49c8e4d655e37311d2d3d04af8b1675b76122ac508e3300a63737b93bb343。
此前 #94/#95 的源码充分性沿革保持完整。

## 已执行命令

两个命令均使用本地环境中配置的 JAVA_HOME 和 ANDROID_HOME。执行前已确认 SDK 二进制文件存在。未修改源码或构建配置。

```sh
./gradlew :shared:testDebugUnitTest --tests 'com.shilapi.xcertplay.hud.RouteSongRetirementTest' --rerun-tasks
```

第 2 次聚焦尝试：本地时间 02:15:25.596932–02:15:38.161609，退出码 0，
BUILD SUCCESSFUL in 12s，17/17 个可执行任务均已执行。新生成的 XML 包含
**27 项测试、0 失败、0 错误、0 跳过**，包括 #95。在完整运行可能覆盖 shared 报告之前，已将 XML/HTML/日志复制到 focused-artifacts。

```sh
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :home:testDebugUnitTest \
  :mobile:lintDebug :home:lintDebug :maphost:lintDebug \
  :mobile:assembleDebug :home:assembleDebug :maphost:assembleDebug
```

完整命令调用一次：本地时间 02:16:19.630362–02:19:49.087324，退出码 0，
BUILD SUCCESSFUL in 3m 29s。250 个可执行任务：50 个已执行，200 个为 up-to-date。

| 单元测试任务／报告 | 测试数 | 失败／错误／跳过 | 证据新鲜度 |
| --- | ---: | --- | --- |
| shared | 1573 | 0 / 0 / 1 | 本次完整运行新写入 240 个 XML 套件 |
| common | 1527 | 0 / 0 / 0 | 本次完整运行新写入 200 个 XML 套件 |
| home | 6 | 0 / 0 / 0 | 任务 UP-TO-DATE；复用既有的单个 XML 套件，不是新执行 |

已在 settings.gradle.kts 核实模块映射：home 和 maphost 位于 samples/ 下。报告及输出元数据均从其实际目录收集。完整 shared 报告也包含新契约的 27/0/0/0 结果，独立于已保留的聚焦执行。

唯一跳过的是既有的
AirPlayPortSelectorTest.fallsBackWhenAnotherListenerOwnsTheWildcardPort。
其源码第 22–35 行探测通配地址／特定地址监听器重叠，并在宿主允许重叠时使用 assumeTrue。该宿主相关假设并未豁免任何新退役契约用例。该跳过没有掩盖任何失败／错误。不需要额外测试运行。

| Lint／组装 | 观察结果 |
| --- | --- |
| mobile lintDebug | 完成；新生成 XML：20 条警告，无 error/fatal 问题 |
| home lintDebug | 完成；lintReportDebug 为 UP-TO-DATE：既存 XML 有 7 条警告，无 error/fatal 问题 |
| maphost lintDebug | 完成；lintReportDebug 为 UP-TO-DATE：既存 XML 有 1 条警告，无 error/fatal 问题 |
| mobile assembleDebug | 与 packageDebug 一同执行；新写入 mobile-debug.apk，24,881,628 字节 |
| home assembleDebug | UP-TO-DATE；既存 samples/home/build/outputs/apk/debug/home-debug.apk，5,478,841 字节 |
| maphost assembleDebug | UP-TO-DATE；既存 samples/maphost/build/outputs/apk/debug/maphost-debug.apk，2,537,766 字节 |

通过输出元数据及文件系统的存在性／大小检查 APK；未安装或发布任何 APK。复用任务／报告已明确区别于新执行。编译器／JVM／弃用及 lint 警告仍可在日志中查看；BUILD SUCCESSFUL 不意味着没有警告。

## 冻结回归的执行

本次完整运行新生成的 XML 证明 quickstart 中列出的每个回归类均已执行，这些类的失败／错误／跳过均为零：

| Shared 类 | 测试数 |
| --- | ---: |
| BydClusterSongTest | 7 |
| BydClusterSongNoteTest | 5 |
| BydClusterSongTimerTest | 13 |
| ClusterSongWriteResultTest | 4 |
| ClusterTurnOverlayExpiryTest | 7 |
| BydHudRouteStateTest | 15 |
| NavigationOutputWorkerTest | 3 |
| RetirementTokenPlumbingTest | 4 |
| CarPlayControllerLifecycleRetirementTest | 22 |

这些测试保持不变；因此完整运行提供了必需的既有行为及控制器回归证据，无需冗余地按类重跑。

## 保留证据及剩余边界

所有成功运行证据位于
checks/078-execution/runtime-20261021-021513。完整 XML 路径／哈希及所有已执行测试名称均保留在 JSON 摘要／清单中。

| 该目录中的产物 | SHA-256 |
| --- | --- |
| source-identities.json | 89080afcd679e89d69688a581d3ac62427a8f4edbb668cd6a44cb0154cfb51fd |
| focused.log | a4a11d3d2f23060a9c86481bf1b103c43c32a2154c757204cf7fb41eabeea18c |
| focused-result.json | 389d1a50802cb66ddf8315299989b5b9bfe78b151bebd3888bc4be1d69027595 |
| focused-xml-summary.json | 004a8c510a64f97225730231bb206bc5c74207049e9ebcb78b2b9c8e870c98b8 |
| 聚焦契约 XML | d1f33adf3c5a491d282d582e3be6966c58bb9135b514cff13dda5b5a282813ac |
| full.log | 967366d4591dfd3911eac72bca102f71eaae203aea38bbc764f28f36b1403d2e |
| full-result.json | ef556dd9cc4d178857d36d8a149b79447737fcb6f5e53d6e94a3a214929fcfe5 |
| full-xml-summary.json | 06364f201ad763847ac0bfc977a800b0c3afc127e33aa1db98aaef128fb8325e |
| full-xml-manifest.json | 033c4069c3c9d5fc99c55c44b00247ec2e8e6953d6b6f7e5c3fd8d0c887872db |
| requested-task-outcomes.json | 501b90a378a518c3e1745a4f463edf221509bfa14fc1c02846416438364da967 |
| lint-apk-summary.json | 30732fa2e65c801f51e593bee7d2bcba7676d7dbaf23cd8ce5b35a23cd78522d |

独立源码／运行时充分性审查见后续的
review-post-implementation-pe-20261021-078.md。测试者还验证了四个冻结区域、未改变的 token 声明／构造器，以及其余全部 46 个 G4 清单输入。PM 负责的公开源码树／空白格式／上游检查及正式 SecOps 证据保存在独立记录中。本验证者未执行 Git/GitHub、凭据、第三方 APK、真实设备或发布操作。结果不解决 #91 的历史来源限制，也不替代 #92 后续的隔离精确候选验证。
