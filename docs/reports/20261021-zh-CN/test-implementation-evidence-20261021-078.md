# #78/#93 实际测试源码交付 — T006

本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
[英文原件](../../../test-implementation-evidence-20261021-078.md)；原文 SHA-256：`8a0fc572f2c78642a4312154fc8c8ff0ec1035a677e2112bb0030b2d23dcde7e`（见 [source-manifest.json](source-manifest.json)）。

日期：2026-10-21。角色：有界测试编写者。授权依据：`receipt-20261021-078-g4.md` 和 `execution-brief-20261021-078-r3.md`。
契约：C-078-R3，`specs/001-lifecycle-display-privacy/increments/078/contracts/retirement-r3.md`，SHA-256 `b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83`。
根目录规范修订版 2：`1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585`。

仅创建了获批的实际测试文件 `shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt`（905 行，26 个 @Test 方法，覆盖全部 16 个计划分组及其变体）。
测试 SHA-256：`dd28704cc8eb47d9cfcbbc245543ee8297211acbeef0b4511849d7c63b9f075f`。

## 源码覆盖情况，不是执行证据

| 验收条件 | 实际方法／观察内容 |
| --- | --- |
| AC078-R3-01 | `currentOwnersProduceRouteAndSong`、`lateProducerCannotChangeReplacement`、`replacementClearsSongMetadataAndTimerIntent`：真实 A/B token，分别验证路线／歌曲有效输出，输出禁用时的缓存，以及 standalone/hud/cluster/FCE 分支观察。 |
| AC078-R3-02 | 六个 `routeMutationBarrierRejects*`／`songMutationBarrierRejects*` 方法覆盖替换／结束／关闭；显式结束后的迟到输入；精确完整快照、隐藏路线字段、计数器及实际任务观察。 |
| AC078-R3-03 | `retirementGuardSequentialOrdersRetirementFirst` 和 `...MutationFirst`：有限的受保护闭包、监视器被持有的观察，以及实际结束／关闭退役。仍须进行独立的同一监视器源码审计。 |
| AC078-R3-04 | `nullProducerIsRejectedOnlyWhileOwned`：无所有者时导航／直接歌曲输入的正向用例、有所有者时拒绝 null、真实签发的 token 仍存储但已退役的状态，以及独立的管理性 start/refresh/settings/note 行为。 |
| AC078-R3-05 | `acceptedDuplicatesAndHiddenRouteTimersKeepPolicy`、`acceptedHiddenNoRouteAndEmptyListGraceAndRetiredFramesKeepPolicy`、替换时清理、计时器／管理操作用例、`wirelessEndRetainsRouteUntilOriginalExpiry`、重复夹具重置及独立运行状态相等性。 |
| AC078-R3-06 | `staleSongTimersCannotRestoreOrClearB`；独立的排队写入／清理；已进入的写入、相同文本写入及清理完成方法；过期返回前后的精确 owner/cache/wanted/shown/timer/firstLogged 状态，随后验证 B 的有效完成。 |
| AC078-R3-07 | `queuedStaleRouteWorkDoesNotEnterVendor`、`enteredRouteActionMayFinishWithoutLocalSuccessor`、`staleEndRefreshAndPublicationCannotEraseB`；真实有界工作线程及有效的最新 main-handler 输出。 |
| AC078-R3-08 | 共享夹具在生产 runCatching 之外记录回调失败；在钩子／监听器／外部动作处验证当前线程的 nav/overlay/song/token/controller 锁；在保护区之外进行有界等待，使用真实单写入线程，不声称并发保护区发生重叠。 |
| AC078-R3-09 | `fixtureResetRestoresLegacyStandaloneBehavior` 及显式清理。完整 Gradle／静态／冻结回归／运行时／PII 审查仍属于 T012/T013，不能由源码编写满足。 |
| AC093-01 | `delayedStartCannotRetireReplacement`：锁外 A start 钩子、实际控制器替换、释放 A 前后的有效 B 输出、不出现过期 start/clear/initialize，重复 B start 保留状态／输出。 |

夹具在构造控制器之前安装两个模拟外部环境。token 来自获批的 start 钩子，从不通过构造器／私有字段获取。只读反射仅限于控制器锁观察和路线解码器状态。直接 `token.retire()` 仅用于真实控制器签发 token 的显式“已退役但仍被存储”状态；替换／结束／关闭集成用例使用实际控制器入口。

导航任务观察使用标准 ThreadPoolExecutor 的 taskCount，减去已计数的夹具 drain/block 任务，并结合真实 initialize/frame/clear 入口记录。B 设置完成后，在具名的稳定负向调度点检查相等性；不将 taskCount 表述为跨 queue.clear 的历史累计计数器。歌曲立即任务／计时器入队计数由受控异步调度器记录；一个真实写入线程执行实际回调体，包括 future 已取消的已捕获过期计时器回调体。回调捕获异常，以便在测试线程断言；不把被吞掉的回调断言视为成功。

清理过程释放屏障，等待有界夹具工作结束，关闭控制器／会话，在模拟环境仍安装时排空任务，取消计时器，仅终止测试执行器，然后重置单例环境，恢复完整偏好设置键／值集合和流控制。构造失败也会触发清理。无法进入静止状态时，拒绝恢复真实端口，并使测试失败。

## 依赖与限制

这仅是实际源码产物。未执行 Gradle、编译器、JUnit、Robolectric、设备、shell／厂商命令或运行时测试。精确契约所需的后续支持 API／类型目前尚不存在（token 保护／锁探针、导航环境／钩子／时钟／快照／锁观察、歌曲环境／快照／锁观察以及感知 token 的 onFrame 重载）。独立 PE 必须先接受实际源码充分性，之后才可进行配套产品工作；缺失 API 不是 RED、跳过或通过证据。

本报告不对物理取消、实车兼容性、冻结厂商缓存、强制同时 token 争用或生产队列总体上限作出声明。路线完成仍是本地“无后继动作”的观察，加上必需的源码审计；歌曲完成通过合成结果执行其实际本地写入／清理路径。未使用私密材料、APK 或网络连接。#91 的历史重建基线限制保持不变；本轮保留三个原始源码快照。

编写后重新计算的产品哈希与获批原件完全一致：

- BydNavigationOutputs.kt：`2d7a6a88b30457ca64cd435ef39d48cdba8b4db0c166051299140bcc195d87d5`。
- BydClusterSong.kt：`b8e2fdf64a31fe93baabee1bdadaab4ae8a4bbf3da31113a6c8b48d5ae7a68e7`。
- CarPlayController.kt：`439b9349cf2ca312fcf7a4264c35c49a85498d89bfcdeded58f82c17ba164d1f`。

本任务未修改任何冻结测试／计划／源码文件、Git 暂存区／HEAD、远程或 issue。下一项依赖：独立 T007 实际测试源码充分性审查。
