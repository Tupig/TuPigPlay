# 安全政策

**简体中文** | [English](#security-policy-english)

## 支持的版本

仅**最新发布版本**接收安全修复；更早预览版请先升级。

| 版本 | 支持 |
| --- | --- |
| 最新 GitHub Release | ✅ |
| 更早预览版 | ❌（请升级） |

## 报告漏洞

请**不要**通过公开 Issue 报告可被利用的安全问题，使用 GitHub 私密漏洞报告：

- 仓库页 **Security → Report a vulnerability**
- 或：<https://github.com/Tupig/TuPigPlay/security/advisories/new>

建议包含：描述、复现步骤、影响范围（版本 / 车机 / Android）、可选修复建议。

## 响应约定

- 一般会在合理时间内确认收到并做初步评估；修复周期视严重程度而定
- 修复发布前请勿公开披露；发布说明中可致谢（需匿名请注明）

## 发布与凭据边界

- 公开分发的 APK **可能故意包含**实验性配件证书与对应密钥（见 [docs/THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md)）。任何人拿到 APK 即可提取。本地编译、删 Git 历史或混淆**不能**让已分发的共享密钥重新保密。
- Git 树与对应源码包**不包含**配件私钥与 Android 发布签名密钥。CI 应拒绝把凭据容器 / 私钥块提交进库。
- 测试使用运行时生成的合成身份。源码构建默认不自动导入私有资源；发布打包需显式本地目录，且仅允许预期的运行时文件。
- Android APK 签名密钥与配件身份分离，留在本地，不打进 APK 内容以外的「保密幻想」。
- 诊断报告发到公开 Issue 前请人工审阅；**永远不要**贴热点密码、配对材料或私钥。

## 范围说明

预期行为（非漏洞）：在已授权前提下使用网络 ADB 做可选车辆集成；本地保存配对与设置；导出由用户触发的诊断文本。漏洞范围包括但不限于：任意代码执行、路径穿越、更新被劫持、敏感信息非预期泄露等。

---

# Security Policy (English)

[简体中文](#安全政策) | **English**

Only the **latest GitHub Release** receives security fixes. Report vulnerabilities via GitHub **Private Vulnerability Reporting** (not public issues): <https://github.com/Tupig/TuPigPlay/security/advisories/new>.

Public APKs may **intentionally** embed the experimental accessory identity described in [THIRD_PARTY_NOTICES](docs/THIRD_PARTY_NOTICES.md); extractability is expected once shipped. Git excludes accessory and Android signing secrets. Review diagnostic reports before posting; never include hotspot passwords or pairing secrets.
