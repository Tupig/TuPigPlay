# TuPigCarPlay

[![CI](https://img.shields.io/github/actions/workflow/status/Tupig/TuPigPlay/diplay.yml?branch=main&label=CI)](https://github.com/Tupig/TuPigPlay/actions)
[![Release](https://img.shields.io/github/v/release/Tupig/TuPigPlay)](https://github.com/Tupig/TuPigPlay/releases)
[![License](https://img.shields.io/github/license/Tupig/TuPigPlay)](LICENSE)

**简体中文** | [English](#tupigcarplay-english)

给 **零跑 Leapmotor** 安卓车机用的 CarPlay：有线、无线都行。仓库是 [`Tupig/TuPigPlay`](https://github.com/Tupig/TuPigPlay)，对外就叫 **TuPigCarPlay**。

详细说明在 [文档中心](docs/wiki/Home.md)。常用：[安装](docs/wiki/安装与使用.md) · [怎么连](docs/wiki/连接方式.md) · [常见问题](docs/wiki/常见问题.md) · [版本](docs/wiki/版本说明.md) · [版本与更新](docs/wiki/版本与更新.md)

## 目录

- [简介](#简介)
- [从哪一版改的](#从哪一版改的)
- [怎么装、怎么连](#怎么装怎么连)
- [支持什么](#支持什么)
- [文档](#文档)
- [自己编译](#自己编译)
- [隐私、安全和许可](#隐私安全和许可)

## 简介

装在车机上（别装到 iPhone），可以：

- USB 有线，或车机热点 / Wi-Fi Direct / 同一局域网无线
- 核心投屏不要求越狱、转接盒、账号；也不要求 ADB
- 零跑仪表、导航一类能力看固件，有的要已授权的网络 ADB
- 出问题可在设置里导出诊断报告，要不要发出去由你决定

**只做零跑。** 别的牌子或许能开，我们不承诺、也不按那些车修。  
**不是 Apple 认证产品。** 预览包里可能带有从公开 Carlinkit 固件里取出的实验性配件身份，私钥能被抠出来，以后 iOS 认不认都不好说——见 [第三方声明](docs/THIRD_PARTY_NOTICES.md)。

## 从哪一版改的

代码是在 [DiPlay](https://github.com/shihabal3amri/DiPlay) 公开预览版 **[v0.2.21](https://github.com/shihabal3amri/DiPlay/releases/tag/v0.2.21)** 上继续改的（当时 DiPlay 最新带版本号的发布就是这一版；本仓分叉时还带上了该标签之后、大约到 2026-10-24 的一小段提交）。

协议接收端最早来自 [xcertplay](https://github.com/shilapi/xcertplay)（GPL-3.0）。界面样子参考了 DiAuto（AGPL-3.0）。许可与致谢写在 [第三方声明](docs/THIRD_PARTY_NOTICES.md)。

## 怎么装、怎么连

1. 到 [Releases](https://github.com/Tupig/TuPigPlay/releases) 下 `TuPigCarPlay-*.apk`，装到**车机**。
2. 按提示给权限；先把别的投屏软件关掉。
3. **设置 → 连接 → 连接方式**：优先填好**车机自带热点**（能开 5 GHz 更好），或改用 USB / Wi-Fi Direct / 同一局域网。
4. 点 **连接手机**。

更细的步骤：[安装与使用](docs/wiki/安装与使用.md)、[连接方式](docs/wiki/连接方式.md)。

报 bug 请先在本机复现，再 **设置 → 诊断 → 保存诊断报告**，看过、去掉热点密码之类以后，再贴到 [Issues](https://github.com/Tupig/TuPigPlay/issues)。

## 支持什么

| | |
| --- | --- |
| 车机 | 零跑 Leapmotor，能装 APK |
| 系统 | Android 7.0+（API 24+）；7.0–8.1 实车还测得少 |
| 手机 | 能开系统 CarPlay 的 iPhone |
| ADB | 核心投屏不用；部分车辆功能才要 |

四种连法的注意点见 [连接方式](docs/wiki/连接方式.md)，边界见 [兼容性](docs/wiki/兼容性.md)。

## 文档

- 给人看的：[文档中心](docs/wiki/Home.md)
- [隐私](docs/PRIVACY.md) · [安全](SECURITY.md) · [版本摘要](docs/wiki/版本说明.md) · [CHANGELOG](CHANGELOG.md)

## 自己编译

主包是 `mobile` 模块：

```sh
./gradlew :mobile:assembleDebug
```

提交前最好跑一轮和 CI 一样的测试 / lint（见 [从源码构建](docs/wiki/从源码构建.md)、[AGENTS.md](AGENTS.md)）。签名密钥和配件私钥不要进 Git。

## 隐私、安全和许可

没有账号，诊断不会自动上传。[隐私政策](docs/PRIVACY.md)。安全问题请走 GitHub 私密报告：[安全政策](SECURITY.md)。

CarPlay 名字和图标是 Apple 的；本项目和 Apple、零跑都没有官方关系。

有用的话欢迎 Star。

---

# TuPigCarPlay (English)

[简体中文](#tupigcarplay) | **English**

CarPlay on **Leapmotor** Android head units. Repo: [`Tupig/TuPigPlay`](https://github.com/Tupig/TuPigPlay).

Built from [DiPlay](https://github.com/shihabal3amri/DiPlay) public preview **[v0.2.21](https://github.com/shihabal3amri/DiPlay/releases/tag/v0.2.21)** (plus a short stretch of commits after that tag through about 2026-10-24). Receiver work traces to [xcertplay](https://github.com/shilapi/xcertplay).

Leapmotor only. Not Apple-certified; see [THIRD_PARTY_NOTICES](docs/THIRD_PARTY_NOTICES.md). Docs: [wiki](docs/wiki/Home.md). Install APK from [Releases](https://github.com/Tupig/TuPigPlay/releases) on the **car**, then Connection setup → prefer the car hotspot, or USB / Wi-Fi Direct / Same LAN.
