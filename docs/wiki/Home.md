# TuPigCarPlay 文档中心

**简体中文** | [English](#tupigcarplay-documentation-hub-english)

给零跑车机用的说明都收在这里，比仓库根上的 README 细一点。

[TuPigCarPlay](https://github.com/Tupig/TuPigPlay) 跑在 **零跑 Leapmotor** 安卓车机上，有线或无线连 iPhone 的 CarPlay。仓库名是 `Tupig/TuPigPlay`。

代码从 [DiPlay](https://github.com/shihabal3amri/DiPlay) 的公开预览版 **[v0.2.21](https://github.com/shihabal3amri/DiPlay/releases/tag/v0.2.21)** 改过来（分叉时还带了该版发布后大约到 2026-10-24 的一小段提交）。协议接收端最早是 [xcertplay](https://github.com/shilapi/xcertplay)，许可见 [第三方声明](../THIRD_PARTY_NOTICES.md)。

只做零跑；不是 Apple 认证产品。预览 APK 里的实验性配件身份说明也在第三方声明里。

---

## 先装起来

1. [Releases](https://github.com/Tupig/TuPigPlay/releases) 下 APK，装到**车机**。
2. 给权限，关掉别的投屏软件。
3. **设置 → 连接 → 连接方式**：先填**车机热点**（能 5 GHz 更好），或 USB / Wi-Fi Direct / 同一局域网。
4. **连接手机**。

卡住了先翻 [常见问题](常见问题.md)。要提 Issue：复现后 **设置 → 诊断 → 保存诊断报告**，自己看过再贴。

---

## 文档导航

### 使用者

| 页面 | 内容 |
| --- | --- |
| [安装与使用](安装与使用.md) | 下载、安装、权限、更新、诊断报告 |
| [连接方式](连接方式.md) | 车机热点、USB、Wi-Fi Direct、同一局域网 |
| [设置说明](设置说明.md) | 按「连接 / 显示 / 音频 / 导航 / 车辆 / 高级」分组的设置 |
| [常见问题](常见问题.md) | 连不上、卡顿、音频、Siri、图标缓存等 |
| [兼容性](兼容性.md) | Android / iOS / 零跑系统边界与已知限制 |
| [版本说明](版本说明.md) | 0.2.x 公开预览版变更摘要 |
| [版本与更新](版本与更新.md) | SemVer 规则、涨号流程、检测与提示条件 |

### 进阶（零跑车机功能）

| 页面 | 内容 |
| --- | --- |
| [导航与仪表](导航与仪表.md) | 风挡/仪表导航、主题、通话卡片等 |
| [无线排障](无线排障.md) | 热点加入、Wi-Fi Direct 信道、流畅度排查 |
| [隐私政策](../PRIVACY.md) | 本地数据、诊断、网络访问 |
| [第三方声明](../THIRD_PARTY_NOTICES.md) | 许可证与配件身份说明 |

### 开发者

| 页面 | 内容 |
| --- | --- |
| [从源码构建](从源码构建.md) | `mobile` / `home` / `maphost`、签名与发布资源 |
| [验证与测试](验证与测试.md) | 单元测试、lint、实车验证记录 |
| [贡献指南](贡献指南.md) | Issue、分支、CI 命令 |
| [安全政策](../../SECURITY.md) | 漏洞报告与发布打包边界 |

---

车机：**零跑**；系统 Android 7.0+（7.0–8.1 还测得少）；手机要能开系统 CarPlay。APK 名一般是 `TuPigCarPlay-*.apk`（`mobile` 模块）。

---

# TuPigCarPlay docs (English)

[简体中文](#tupigcarplay-文档中心) | **English**

Same content as above for Leapmotor CarPlay. Built from DiPlay **v0.2.21**. Start at [安装与使用](安装与使用.md) / [常见问题](常见问题.md).
