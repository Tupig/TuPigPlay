# #78/#93 运行时验证 — 第 1 次尝试

本文件为对应英文原件的中文阅读版；不替代冻结原件，不构成新的独立审查或审批。
[英文原件](../../../test-runtime-evidence-20261021-078-attempt1.md)；原文 SHA-256：`d50771c6c06d1f21394fe8b98a4016503aa5777f37a9ae227e4e7b714e018bb4`（见 [source-manifest.json](source-manifest.json)）。

日期：2026-10-21。角色：独立测试者、唯一 Gradle 执行负责人。
结果：**在测试源码编译阶段失败；未执行任何聚焦测试。**
这是实际失败，不是预期 RED，也不是验收证据。

执行获批的聚焦命令时，使用了本地环境中配置的 JAVA_HOME 和 ANDROID_HOME。

```sh
./gradlew :shared:testDebugUnitTest --tests 'com.shilapi.xcertplay.hud.RouteSongRetirementTest' --rerun-tasks
```

开始：本地时间 2026-10-21T02:13:21.594170。结束：02:13:49.214552。
退出码：1。Gradle：BUILD FAILED in 27s，执行了 15 个可执行任务。
产品 Kotlin 编译完成；:shared:compileDebugUnitTestKotlin 失败。

运行前后核对了源码输入，无漂移：

- Controller：c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414。
- Navigation：1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d。
- Song：00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c。
- RouteSongRetirementTest：b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42。

## 精确失败原因及修正路径

日志第 58–59 行报告测试第 212:50 行：初始化器预期 List<Any>，实际收到 Iap2Frame。controllerLocks 初始化器调用 emptyList()，解析为该测试类的路线帧辅助方法，而不是 Kotlin 集合函数。限定为 kotlin.collections.emptyList() 是保留所有契约断言的有界机械修正。已通知 PM；测试者未修改源码。下一次聚焦尝试前，须取得修正后的身份及审查结果。

## 保留证据

目录：checks/078-execution/runtime-20261021-021303。

| 产物 | SHA-256 |
| --- | --- |
| focused.log | 5213cf82152651b3a10c484358816cc94295735a1c5334a3c24c837a59f9b5c9 |
| focused-result.json | ca5aaf1cb6f0d2f80ca882bdde3e32e6ca2008bc5d66014837e5df2698d79f0a |
| source-identities.json | d603e78806cf9407c858b7bcb3d9989e255ca58c0116f9ce68062e1cdcda1896 |

保留证据时复制了 239 个既存 XML 文件。**自本次尝试开始以来，这些文件均未修改。** 它们是旧报告，不能提供本次尝试的已执行用例总数。不得据此推导聚焦测试的失败／错误／跳过数量。由于聚焦编译失败，未运行完整 AGENTS 命令、lint 或 APK 组装。未执行 Git/GitHub 或设备操作。
