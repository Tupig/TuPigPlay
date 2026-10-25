# 第三方声明 / Third-party notices

**简体中文** | [English](#credits-and-license-notices-english)

仓库 [`Tupig/TuPigPlay`](https://github.com/Tupig/TuPigPlay)，产品名 **TuPigCarPlay**。

本应用在 [DiPlay](https://github.com/shihabal3amri/DiPlay) 公开预览版 **[v0.2.21](https://github.com/shihabal3amri/DiPlay/releases/tag/v0.2.21)** 上继续修改（分叉时还带了该标签之后大约到 2026-10-24 的提交）。

## 接收端

协议接收端最早来自 [xcertplay（shilapi）](https://github.com/shilapi/xcertplay)（GNU GPL v3），全文见 `LICENSE`；相关说明归档在 `docs/archive/UPSTREAM-README.md`。

原作者还感谢 [LIVI](https://github.com/f-io/LIVI)、[Showcase](https://github.com/amineross/showcase) 等协议研究；源码里的署名我们保留。

## 主页与设置 UI

`common/.../TuPigCarPlayActivity.kt` 等适配了 [DiAuto](https://github.com/shihabal3amri/DiAuto) 的配色、布局与文案风格。DiAuto 为 AGPL-3.0；许可文本见 `docs/licenses/DiAuto-AGPL-3.0.txt`。

## CarPlay 图标

未改动的图标来自 Apple 开发者站点资源。CarPlay 及相关标识属于 Apple Inc.；本项目开源许可证不覆盖该资源，亦不代表 Apple 认证或背书。

## 运行时依赖

AndroidX / Jetpack Compose、Kotlin 标准库、Bouncy Castle、JmDNS、SLF4J 等，许可证见 `docs/licenses/dependencies/` 与版本目录。

## 配件身份（重要）

公开 APK 可能包含从公开 Carlinkit 固件提取的实验性配件证书与密钥，**可被提取**。这不是为本项目新签发的 MFi 身份；未来 iOS 是否继续接受未知。详见 [SECURITY.md](../SECURITY.md)。

仪表导航等 PNG 资源另见 `shared/TUPIG_HUD_ICONS_NOTICE.md`。

---

# Credits and license notices (English)

[简体中文](#第三方声明--third-party-notices) | **English**

## Based on DiPlay

TuPigCarPlay continues from [DiPlay](https://github.com/shihabal3amri/DiPlay) public preview **[v0.2.21](https://github.com/shihabal3amri/DiPlay/releases/tag/v0.2.21)**, including a short stretch of commits after that tag through about 2026-10-24.

## Receiver

The protocol receiver comes from [xcertplay by shilapi](https://github.com/shilapi/xcertplay) (GNU GPL version 3); see `LICENSE` and `docs/archive/UPSTREAM-README.md`.

Credits to [LIVI](https://github.com/f-io/LIVI) and [Showcase](https://github.com/amineross/showcase) for protocol research are kept in the tree.

## Home and settings UI

`common/src/main/java/com/shilapi/xcertplay/TuPigCarPlayActivity.kt` adapts the palette, visual arrangement and interface copy of the [DiAuto project](https://github.com/shihabal3amri/DiAuto). DiAuto's source is licensed under AGPL version 3. The UI file is marked AGPL-3.0-only; its license text is included in `docs/licenses/DiAuto-AGPL-3.0.txt`.

## CarPlay icon

The unmodified icon was obtained from Apple's developer site at:

https://developer.apple.com/assets/elements/icons/carplay/carplay-96x96_2x.png

CarPlay and the CarPlay icon are Apple Inc. marks/assets. This asset is not covered by the project's open-source code license. Its use here does not imply Apple approval or certification.

## Runtime dependencies

- AndroidX and Jetpack Compose — Android Open Source Project; Apache License 2.0.
- Kotlin standard library — JetBrains; Apache License 2.0.
- Bouncy Castle 1.79 — The Legion of the Bouncy Castle Inc.; Bouncy Castle license (MIT-style).
- JmDNS 3.6.3 — JmDNS contributors; Apache License 2.0.
- SLF4J — QOS.ch; MIT license.

Gradle dependency declarations and version catalog accompany the source. License files available in the resolved artifacts are included under `docs/licenses/dependencies/`.

## Native call echo cancellation

TuPigCarPlay vendors a subset of [SpeexDSP 1.2.1](https://github.com/xiph/speexdsp/tree/SpeexDSP-1.2.1) from Xiph.org Foundation and its contributors, including the acoustic echo canceller, preprocessor and FFT/filterbank support. It is compiled into `libspeex_echo.so` for the optional experimental **Call echo cancellation** feature.

SpeexDSP uses a BSD-style three-clause license. The full copyright notices, redistribution conditions and disclaimer are retained in [the vendored COPYING file](../shared/src/main/jni/speexdsp/COPYING). Component source headers retain their additional notices, including KISS FFT's Mark Borgerding attribution. The [vendoring notes](../shared/src/main/jni/speexdsp/README.md) identify the source version and build configuration.

## Software Opus microphone encoder

TuPigCarPlay vendors [Concentus](https://github.com/lostromb/concentus), Logan Stromberg's pure-Java port of the Opus reference library from Xiph.Org Foundation, Skype Limited, Microsoft Corporation and other contributors. It encodes the CarPlay microphone on head units whose Android has no MediaCodec Opus encoder.

Concentus uses the Opus BSD-style license. The full copyright notices, redistribution conditions and disclaimer are retained in [the vendored LICENSE file](../shared/src/main/java/org/concentus/LICENSE). The [vendoring notes](../shared/src/main/java/org/concentus/README.md) identify the source commit.

## Experimental authentication data

The public preview APK includes an accessory certificate/key pair recovered from public Carlinkit C2Air Allwinner V821 firmware during the owner's local investigation. These data are not newly generated Apple-issued credentials for TuPigCarPlay and are not relicensed as project source code. They are bundled in the preview APK to reproduce the offline experiment; continued acceptance and suitability for general distribution are unresolved. The source archive does not contain the private key, and the separate Android APK-signing key is never distributed.

## Download website

The static site layout, CSS and generator adapt DiAuto (AGPL-3.0). The AGPL license text is included with the source.

## TuPig HUD maneuver icons

Required Notice: Copyright AndyShaman (https://github.com/AndyShaman/BYDMate)

The maneuver PNGs under `shared/src/main/assets/tupig-hud-icons` were imported from BYDMate. Its PolyForm Noncommercial 1.0.0 terms and required notice are included alongside the assets. These files are separate from the project code license; upstream describes them as donor assets and their original provenance is not independently established. The validated DiLink5.1 windshield path uses factory turn codes rather than these images.
