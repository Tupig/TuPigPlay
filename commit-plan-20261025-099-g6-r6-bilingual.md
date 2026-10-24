# Commit Plan — G6-098-099-BILINGUAL-REDACTION-R6

日期：2026-10-25（Asia/Shanghai）

## 决策与目标

用户明确要求报告提供完整中文版本，并委托 PM 决定后续门禁。PM 拟在 PE/SecOps 接受精确候选后，按委托选择 G6 选项 1：只对本计划列出的 detached 候选作本地提交；本计划本身不代表 G6 已批准。

本 R6 替代未提交的 R5 草案。复核发现目录 README 含指向本地治理记录的失效链接和过期“当前结论”；已在 #98 记录范围内，仅修正 README 的时点说明和链接，不改 16 对冻结报告、原件、清单或验证结果。其余候选仍含完整 16 对源码/译文，六份 #99 报告使用 G5 接受的脱敏 blob，其他映射文件保持已审查内容。R5 未提交且不属于本提交。先前 11 路径试验失败记录保留。

## 身份与边界

- 根规范：`spec.md` 修订 2，SHA-256 `1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585`；该本地规范仅作身份引用。
- #99 G5 包：`acceptance-packet-20261025-099-r3.md`，SHA-256 `ecd358c36e1cd977e67986323e86de37eeb57d6b605bceea6738a0b99bd3e842`。
- #99 G5 回执：`receipt-20261025-099-g5-r3.md`，SHA-256 `2490de4625306f80677f4a25d6320ddb9d5477cd1d964110afb3700dd2d6935a`。
- #98 G5 回执：`receipt-20261025-098-g5-r1.md`；接受 16 对中英报告及其映射/哈希证据。README 修正属于同一目录可用性验收；待独立复核通过后，由 PM 将其记录为非语义机械修正，不改变 #98 翻译验收范围。
- 内容 blob 取自已审查候选 `c6149afe9c6b717fa065f0cd59bdae775eb040f8`；该候选原属更广的本地历史。本次只复制本表内容文件，避免继承其他 issue 代码或带有临时路径的旧 `journal.md`。
- 新候选父提交：`origin/main`=`448bc1d63fadc496450c7c01e33c3feb4b66f7ee`。
- detached HEAD；不创建本地命名分支。远程目标如获 G9 批准，仅为 `origin`，绝不触及 `upstream`。

## 精确提交范围

| 路径 | 内容 SHA-256 |
| --- | --- |
| `acceptance-packet-20261021-078.md` | `56a404318c28f52784f1d9d5c0ddd406875c85685e013773860a96f7576b4728` |
| `build-package-20261021-092-successor-plan.md` | `ce671b3991c01876ddabbed2f8f594f8aa8f0ccdfba7c23a221976f07dffce33` |
| `checks/test_doc_privacy_099.py` | `da4630352e1c7c1f048c227fee22ea0d7f28bf6ad21ea23b55de50dc7d4c6522` |
| `docs/reports/20261021-zh-CN/README.md` | `59aa3d6d77451a298f19a672f5644ca6f9ab549bc3c9799591dc90fa086e42b8` |
| `docs/reports/20261021-zh-CN/acceptance-packet-20261021-078.md` | `977be0d0d00ee93ff1f4965ab3cf80cc28ab3ac4728514083341e7dfb7b3a78e` |
| `docs/reports/20261021-zh-CN/build-package-20261021-092-successor-plan.md` | `454e87dcca37e217666b1642557eb9c696c7ed9f6afa8af5c07404976bd377a9` |
| `docs/reports/20261021-zh-CN/execution-brief-20261021-078-r3.md` | `f7d76418d33e759b23569450b626196b3f0861fd95c236364c67feb008605c78` |
| `docs/reports/20261021-zh-CN/implementation-evidence-20261021-078.md` | `055396392595c9133d23c6e5f1969cf7f9f6cec8ab106edc4658dba0dde11b3c` |
| `docs/reports/20261021-zh-CN/review-actual-test-adequacy-20261021-078-095.md` | `13914b9c5d6a3c14443c1d55308d5c239fecfb5de4cfb270157af0a40bbd1416` |
| `docs/reports/20261021-zh-CN/review-actual-test-adequacy-20261021-078.md` | `e21d44461195e530f517adc395223cd33fed61f3f71f3c5485aab885c5c40037` |
| `docs/reports/20261021-zh-CN/review-execution-readiness-20261021-078-r3.md` | `a954f74134021d24c84415842a21ef6f1b3bb135fa1de4d6dd07bdf266318612` |
| `docs/reports/20261021-zh-CN/review-post-implementation-pe-20261021-078.md` | `e0007cbbf80ebe80753b50aa6d81a119c973cbc3c8c3a1380d1d34d6e241ad2e` |
| `docs/reports/20261021-zh-CN/review-post-implementation-secops-20261021-078-097.md` | `4411b2a9ea2506ebb3a78f79628541e513844874932f8d86f33ce7c707f3f3eb` |
| `docs/reports/20261021-zh-CN/review-post-implementation-secops-20261021-078-runtime.md` | `38af9d14525422694f80ae4eb2e86eb95f1393763b63a23225209b954b673cd0` |
| `docs/reports/20261021-zh-CN/review-post-implementation-secops-20261021-078.md` | `df06c427e3b5b22ff9e2cb4761e5cf6ed3173b84008c767ece0ce2f91cf1a294` |
| `docs/reports/20261021-zh-CN/source-manifest.json` | `27c8058cd7bde74aed005493545c5b8644a9c7ba23330ffb140afad91c4185a7` |
| `docs/reports/20261021-zh-CN/test-implementation-evidence-20261021-078.md` | `aefcce2c127b1a20af143cbd3f9544e18c3f3f10c77e25034fc395c90ef82e97` |
| `docs/reports/20261021-zh-CN/test-mechanical-correction-20261021-078-097.md` | `1b22ee235c4f3ef569904589f86a02fc6368ba81bcc809c8e359f7a8219537cb` |
| `docs/reports/20261021-zh-CN/test-mechanical-correction-20261021-078-r1.md` | `46c2bd419157b310bb4eb6f2a2bb779fc5b795f6257ca189ae0260da2e5628ec` |
| `docs/reports/20261021-zh-CN/test-runtime-evidence-20261021-078-attempt1.md` | `3b2ad3b8e01cb2f9744ee7ef182eeac5f519d1d1f3499a17726b2f4c855b4266` |
| `docs/reports/20261021-zh-CN/test-runtime-evidence-20261021-078.md` | `3f29dcfc5053710f955eeac71e3577d64a287c4cb075ab5726dbef9170a29e8e` |
| `docs/reports/20261021-zh-CN/translation-validation.json` | `115ba93936b87b82c0c43710a74a656f382cc58f904c94fc71049bafb5a7b800` |
| `execution-brief-20261021-078-r3.md` | `366e6039323fcc649b2331b21ed8d0b580c279a77572ffa71cbb4644dc5319c3` |
| `implementation-evidence-20261021-078.md` | `3d79e7e4f0ddafe66fc74e5aef62577b2edb7d839cd95d34c03457736c4e50ee` |
| `review-actual-test-adequacy-20261021-078-095.md` | `7476a039903a07fe785259934e507433b5a07d5a2f75397ee5395ce196fe6989` |
| `review-actual-test-adequacy-20261021-078.md` | `02ab0cf82cf1d86af27d009d9bf045a2f7e3e93589f4d5ff66f0844fd6834897` |
| `review-execution-readiness-20261021-078-r3.md` | `23cb7a4e2d9a06dadee9d22d44f43e1504459127524f41d3c49ebd96cfc77cdc` |
| `review-post-implementation-pe-20261021-078.md` | `6b6c3a5b1ed499f13870f1b4540a4da19757df238cd9a82a381f72469dde1f16` |
| `review-post-implementation-secops-20261021-078-097.md` | `cb7d93edb49c604dbf37aa4bb81b75a65d0c51c9dad1a23d972e7767de6fd4c2` |
| `review-post-implementation-secops-20261021-078-runtime.md` | `13cb266641fe83fc145f29121389238f02c99e1b4923e2d8633c31914dda981f` |
| `review-post-implementation-secops-20261021-078.md` | `6e112d463b491602409e95f2870c47f07b6d9b52ef3bde8309f045c67ac07d5c` |
| `test-implementation-evidence-20261021-078.md` | `8a0fc572f2c78642a4312154fc8c8ff0ec1035a677e2112bb0030b2d23dcde7e` |
| `test-mechanical-correction-20261021-078-097.md` | `45e49c8e4d655e37311d2d3d04af8b1675b76122ac508e3300a63737b93bb343` |
| `test-mechanical-correction-20261021-078-r1.md` | `b05c8df5df3c66a95af35d3b1f91f51367fdd11d20786e1e2ed3cdfe1d8a5284` |
| `test-runtime-evidence-20261021-078-attempt1.md` | `8ff08e367e3c8665de08a51c85999fe1c5f0e4376f51ef3fb3533bce2a2c627e` |
| `test-runtime-evidence-20261021-078.md` | `7b9903b39126713efe7cadef662561e2e0dbfd2302ec68f75a371ef95dce21b7` |


上述 36 项包含 16 对英文/中文报告、目录、两份映射清单和隐私检查器。另有 `receipt-20261025-098-g5-r1.md` 和本 R6 计划，共 38 个远程候选路径。PE/SecOps 接受精确候选后、提交之前，PM 将在原工作区创建本地 G6 决定回执，绑定 `origin/main` 父提交、暂存 tree SHA 与 38 路径清单；提交后再把生成的 commit SHA 追加到本地 `status.md` 和 `journal.md`。这些含完整项目状态/历史的记录明确不进入本次远程候选。产品源码、APK、签名/MFi 资产及其他 issue 的可执行改动均排除。

## 验证与退出条件

- 精确暂存上述 38 个路径；验证 36 个内容哈希与本计划一致。
- `python3 -B -m unittest -v checks.test_doc_privacy_099`：2/2 通过；`python3 scripts/check_public_tree.py`：通过；README 本地链接 35/35 可达；`git diff --cached --check`：通过。
- 按 `AGENTS.md` 执行完整 Gradle 命令：退出 1；共 2,997 项测试，3 项失败、1 项跳过，246 个 actionable tasks 中 51 executed、195 UP-TO-DATE。失败为 `AirPlayDualListenerTest.acceptsBothFamiliesOnTheSamePort`、`AirPlayTcpAcceptTest.bothAddressFamiliesReportOwnerAndOldCleanupPreservesNewListener`、`CarPlayBonjourDualStackTest.dualLanThenSingleP2pThenDualLanServesAirPlayInfo`，与文档候选无关，已由现有 #100 记录。此次源为 `448bc1d63fadc496450c7c01e33c3feb4b66f7ee`，不含用户工作区未提交的 #78/#93 源码改动；远程 CI 若失败，不合并。
- 独立 PE 审查完整翻译映射、README 可达链接、脱敏范围及精确树；独立 SecOps 扫描 16 对报告、候选提交历史及所有治理文件，记录范围和限制。
- 本地 G6 决定回执在提交前创建，提交身份在提交后追加到本地 `status.md`/`journal.md`；三者均不加入 38 路径。仅授权本地 detached 提交。G9 仍须重新核对最终历史和远程 scope，不能由 G6 推断。无 push、PR、merge、issue closure、release 或硬件验收授权。
