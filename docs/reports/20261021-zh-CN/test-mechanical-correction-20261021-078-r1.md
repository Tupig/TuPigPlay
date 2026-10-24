# T007-01／#94：稳定的导航任务观察

本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
[英文原件](../../../test-mechanical-correction-20261021-078-r1.md)；原文 SHA-256：`b05c8df5df3c66a95af35d3b1f91f51367fdd11d20786e1e2ed3cdfe1d8a5284`（见 [source-manifest.json](source-manifest.json)）。

日期：2026-10-21。范围：仅对实际测试作机械修正，C-078-R3 不变。Issue：[第 94 号 issue](https://github.com/Tupig/TuPigPlay/issues/94)。
前代交付：`test-implementation-evidence-20261021-078.md` 保持不变；原测试 SHA-256 为 `dd28704cc8eb47d9cfcbbc245543ee8297211acbeef0b4511849d7c63b9f075f`。
修正后测试：`shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt`，972 行，仍为 26 个 @Test 方法。
修正后 SHA-256：`765eb427c28aa5892f211d9909269300dadf898723f93150f18065bbc1ca7a06`。

独立 PE 发现，即使 Future 已完成，也可能在执行器出队／已完成任务记账期间采样 taskCount。因此，原来的自由采样观察不足以支撑精确的负向入队判定。

夹具现在通过未改动的标准 ThreadFactory 注册 Thread 到工作线程的身份映射。每个夹具 Barrier 都会先注册自身，再发出已进入信号，并在退出时注销。counts() 仅在工作线程已有屏障已进入且未释放时复用该屏障；这包括 workerBlock 和已进入的模拟外部动作。它从不排空该被阻塞的旧动作。对于其他工作线程，counts() 提交并记录一个短暂的快照屏障，然后等待所有工作线程均被观察到位于屏障内，再采样任何 taskCount。采样前后都检查相同的注册／未释放状态。这些任务体被保持时，工作线程的出队／完成记账不能推进。

辅助任务提交计数仅在执行器成功接受提交后递增。在 finally 中，只释放新引入的快照屏障；每个快照 future 都接受有界完成／错误检查，回调错误会在测试线程暴露。现有场景屏障仍由各自测试控制。精确的标准 ThreadPoolExecutor／ArrayBlockingQueue／AbortPolicy 实现、真实工作线程逻辑、源码 API 依赖及完整状态／入队／发布断言保持不变。未引入自定义执行器子类，也未削弱预期。

未运行 Gradle／编译器／运行时测试；这仍是修正后的测试源码证据，等待独立 PE 复查。未修改产品文件或冻结的测试／计划文件。三个产品原始哈希分别保持为 `2d7a6a88b30457ca64cd435ef39d48cdba8b4db0c166051299140bcc195d87d5`、`b8e2fdf64a31fe93baabee1bdadaab4ae8a4bbf3da31113a6c8b48d5ae7a68e7` 和 `439b9349cf2ca312fcf7a4264c35c49a85498d89bfcdeded58f82c17ba164d1f`。
