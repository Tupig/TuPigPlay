# #97 测试编译的机械修正

本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
[英文原件](../../../test-mechanical-correction-20261021-078-097.md)；原文 SHA-256：`45e49c8e4d655e37311d2d3d04af8b1675b76122ac508e3300a63737b93bb343`（见 [source-manifest.json](source-manifest.json)）。

日期：2026-10-21。在聚焦测试第 1 次尝试之后由 PM 授权：产品源码编译通过；测试第 212 行将未限定的 emptyList 调用解析为返回 Iap2Frame 的路线帧辅助方法。

唯一修改：将 `controllerLocks: List<Any> = emptyList()` 改为 `controllerLocks: List<Any> = kotlin.collections.emptyList()`。

未改变产品代码、测试断言、调度、契约或语义判定标准。本代理未调用 Gradle；仍须进行独立的精确差异 PE 审查，并由指定测试者重新运行。这是编译修正，不是行为 RED 或 PASS 证据。

前代快照：`checks/078-execution/RouteSongRetirementTest-before-097.kt.txt`。
前代 SHA-256：`b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42`。
后继测试 SHA-256：`561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e`。

原实施／测试证据保持不变。
