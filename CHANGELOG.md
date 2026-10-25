# 更新日志 / Changelog

**简体中文** | [English](#changelog-english)

面向用户的版本摘要见 [docs/wiki/版本说明.md](docs/wiki/版本说明.md)。下列为按版本的要点列表（公开预览，不宣称全车型验收）。

## TuPigCarPlay 0.0.1 — 2026-10-25

- 本仓产品版本从 `0.0.1`（`versionCode` 1）起计；代码仍基于 DiPlay 公开预览 v0.2.21 一带继续改。
- 品牌与文档面向零跑；图标与更新检测指向本仓 Releases；`version.properties` + Release 工作流涨号发版。

## 继承自 DiPlay 的发布摘要（非本仓号）

下列 `0.2.x` 为分叉前的上游预览要点，便于对照；**不是**本仓 tag。

## TuPigCarPlay 0.2.21 — 2026-10-17

- 修复 0.2.20 音乐滞后与变速；中断后恢复播放。
- 有线自恢复与断开等待逻辑；导航提示音处理。
- 软件 Opus 检查失败不再拖垮进程；无线失败原因展示；热点 / VLAN / 蓝牙等待等修复。
- 零跑：结束会话交还仪表、DiYou 卡片、热点关闭提示、来电弹窗（Android 10+）等。
- Android 7.0 安装、意大利语、多项默认关闭的新选项；诊断字段增强。

## TuPigCarPlay 0.2.20 — 2026-10-15

- 音频时钟补偿、线程优先级、无线/USB 分缓冲。
- 通话外放与麦克风 / Siri / 车机语音源相关修复。
- 有线启动时序、热点开启路径、Wi-Fi Direct 扫描与信道边界。
- 仪表控制器探测、HEVC 回退、设置信息架构分组；诊断增强。

## TuPigCarPlay 0.2.19 — 2026-10-12

- 有线加速与 USB 权限、通话时钟与麦克风、仪表地图与设置菜单、日语 / 繁中、诊断扫码等。

## 更早版本（0.2.9–0.2.18）

见 [版本说明](docs/wiki/版本说明.md) 合并摘要；英文长文在 [docs/archive/](docs/archive/)。

---

# Changelog (English)

[简体中文](#更新日志--changelog) | **English**

User-facing summaries: [docs/wiki/版本说明.md](docs/wiki/版本说明.md). This file keeps short per-release bullets for 0.2.19+. Older detail is archived under `docs/archive/`.
