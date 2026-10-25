# 隐私政策 / Privacy Policy

**简体中文** | [English](#privacy-policy)

**最后更新：2026-10-25**

## 概述

TuPigCarPlay 在零跑 Leapmotor 车机上通过 USB / Wi-Fi 与 iPhone 建立 CarPlay 会话。认证在本地完成。

**我们不运营账号体系，也不自动上传诊断或使用数据。**

## 数据收集

无云端产品遥测。应用：

- **不含**强制统计 / 崩溃自动上报账号体系；
- **没有**用户注册或登录；
- 不把诊断报告自动发往本项目服务器（导出由你触发，分享由你选择）。

iPhone 上各 CarPlay 应用的网络与隐私行为由其自身与 Apple 政策约束。

## 本地数据

车机应用存储中可能包含：偏好设置、已选配对设备、配对材料、有界诊断日志等。认证与配对材料尽量排除在 Android 备份之外。卸载会清除应用私有数据；你导出到 Downloads 的报告需自行删除。

## 诊断报告

由你在 **设置 → 诊断** 发起。内容可包括应用 / 设备版本、显示与协商、连接阶段、Wi-Fi 频段信道状态、解码恢复事件等。开启可选方向盘键服务时，可记录最近硬件键的 key/scan code 及应用处理结果（不会记录按键输入的文本内容，也不记录输入设备名）。

导出会尝试过滤协议载荷、含凭据行与常见标识符；**无法保证**识别所有厂商字符串。公开 Issue 前请人工审阅。报告不会自动上传。

存储位置：Android 10+ 通常为 `Downloads/TuPigCarPlay`；更旧系统可能请求存储权限。选择器或公共目录不可用时，回退到应用专属外部或私有目录（确认框会显示路径）；卸载会清除应用目录内回退文件。

## 权限

| 权限 | 用途 |
| --- | --- |
| 麦克风 | Siri / 通话 |
| 蓝牙 / 附近的设备 | 发现与传输 |
| 定位（部分旧系统） | Wi-Fi 相关 |
| 本地 VPN | USB 链路（不是上网 VPN） |
| Usage Access（可选） | 部分零跑仪表主题 / 可见性推断，可关 |

## 网络访问

- 可选：每日检查本仓库 GitHub Releases 是否有更新（User-Agent 含应用版本；GitHub 可见车机 IP）。**不会**未经点击下载安装；可在关于中关闭检查。
- 下载更新时按发布校验和验证；Android 10+ 可能复制到 `Download/TuPigCarPlay`。

## 政策变更

变更发布于本仓库。

## 联系方式

<https://github.com/Tupig/TuPigPlay/issues>

---

# Privacy Policy

[简体中文](#隐私政策--privacy-policy) | **English**

**Last updated: 2026-10-25**

TuPigCarPlay runs on Leapmotor head units and talks to the iPhone over USB/Wi-Fi with **local** authentication. There is **no product account** and **no automatic upload** of diagnostics.

Preferences, pairing material and bounded diagnostic logs stay on the device. You trigger exports under **Settings → Diagnostics**; review them before posting publicly. Optional update checks hit GitHub Releases only. Microphone, Bluetooth/Nearby, location (older Android), local VPN (USB link), and optional Usage Access are used as described above. Contact: <https://github.com/Tupig/TuPigPlay/issues>.
