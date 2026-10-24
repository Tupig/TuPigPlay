# #78/#93 implementation evidence — 2026-10-21

Status: T009–T011 authored; source/boundary checks only. No compilation, runtime check, behavioral RED/pass, device, credential, Git mutation, or release claim. PM designates `commit_pe_audit` as sole Gradle owner after this handoff.

Authority: `receipt-20261021-078-g4.md`, `execution-brief-20261021-078-r3.md`, C-078-R3, ADR r3.2, root spec revision 2. Original actual-test PE acceptance is `review-actual-test-adequacy-20261021-078.md`; #95 addition accepted in `review-actual-test-adequacy-20261021-078-095.md` before the corresponding product repair.

## Bound source identities

- `shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt`: `c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414`
- `shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt`: `1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d`
- `shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt`: `00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c`
- `shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt`: `b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42`

## Changes and caller/lock audit

- Token: retire and withAdmittedMutation share the same private monitor; Boolean admission, finite local closure, no new external operations. All other controller bytes, including #89 mask handling, remain frozen.
- Navigation start: precompute application context and availability outside locks; navigation → token → song, then navigation → token → overlay in separate sections. Changed ownership advances navigation epoch; same owner preserves it. Worker start/submit/clear use navigation → worker only after token/state release. Start hook runs unlocked between initial owner registration and final worker-owner recheck (#93).
- Route ingress: strict explicit null ownership, admission capture then unlocked mutation hook, navigation → token → overlay commit applies the unchanged parser once. Duplicate/NONE accepted frames retain parser freshness behavior. Handler posts are outside locks.
- Overlay refresh/listener: admin refresh captures current owner; explicit stale refresh rejects. Publication carries owner/epoch and listener/revision stamps. Handler bookkeeping validates under navigation → token → overlay; listener callback is outside all locks. Terminal delivery uses exact unowned terminal epoch, not retired normal admission.
- #95: after a posted runnable fails token admission, navigation → overlay clears only its exact consumed ticket/pending identity. It never delivers or updates route/last-delivered state. This prevents replacement/terminal publication from inheriting a nonexistent posted runnable. Same-contract test covers replacement and close successors.
- Navigation end: retire under navigation → token, then local song terminal transition and overlay transition separately. No song/overlay nesting. Worker clear enqueues only after token/state release. Song terminal intent dispatch and call-end wrapper are unlocked.
- #96: initial transition returns immutable original ended token; call end receives it only when nonnull and terminal ownership recheck passes. No mutable lastEndedToken reread; no null fallback into frozen call subsystem. Production caller search finds sole session-start call in CarPlayController passing a real issued token; genuinely unowned navigation cleanup has no owned call session to retire. Frozen BydCarPlayCall is not edited and its internals are not newly certified.
- Direct song paths: capture owner under state, release, then token → state and exact owner/epoch recheck. Frame admission hook, preferences, execute/schedule, command boundary and logs remain unlocked. Normal frame/null acceptance uses the stored owner, including a retired-but-still-stored owner.
- Song B transition clears decoder metadata/wanted/note/change/announced intentions, advancing local epoch/revision; same-owner start is idempotent. Last successfully shown card, owner stamp and firstLogged remain physical-completion bookkeeping; they are not inherited B decoder metadata. Owned same-text output requires matching owner/epoch. B with no metadata or disabled output does not promise a physical card clear. Valid later clear can still see the prior physical card.
- Song intents: owner/epoch/revision/card/context checked before unlocked scheduler dispatch, before external entry and after accepted result. Timers additionally require exact stored timer identity; current preference snapshot precedes commit. Narrow retirement-after-dispatch-check race may enqueue an ineffective task; execution/completion checks reject it. Terminal intents use unowned exact epoch. No stale successful completion changes shown/owner stamps/firstLogged.
- Song attach is plain assignment of already resolved application context. ownerStarted/ownerEnded contain only local state operations; returned terminal dispatch executes later, outside navigation/state locks.
- Fixture APIs preserve real navigation workers and real song writer/timer/completion logic. Exact standard asynchronous bounded executor shape enforced for test navigation ports. Auxiliary map/call initialization/start/end and OEM bridge actions are replaced/suppressed only while fake environment is installed. Production scheduler is never shut down; quiescent fixture reset clears legacy/new state and restores context/handler/clock/ports/default delegation.

Allowed edges used: controller resource/lifecycle → token (frozen callers); call lifecycle → token (frozen caller); worker monitor → token (frozen caller); navigation → token → overlay or song; direct song token → song; navigation → worker after token/state release. There is no state → token/navigation, token → controller/call/worker/navigation, or nested song+overlay edge introduced.

## Frozen boundary checks

Baseline: `checks/078-execution/source-baseline-20261021.json`. Exact SHA equality verified for:
- all bytes before token class declaration: `66d64253b89fd863beac924dadce9f1fc08107df76258849ca60b78b60ece93b`.
- all bytes starting newline before internal fun isWirelessHandoffInProgress: `3b5ac25d15149de4c5c839406186a6a169eeea82128e2a4da88200d45acd38b7`.
- all bytes before internal object BydClusterSong declaration (pure decoder): `e4006fd1cab98fafde467f81c246180d3b00c67c6335bbd90e06978ef13aec4e`.
- all bytes starting internal object ClusterSongWriteResult declaration (vendor result/tool): `a966291fa94da85e9ead3965b878edbbf60ee2aff30b74ad277eef153071934e`.

`git diff --check` passes. Decoder/result parser/tool suffix and all controller bytes outside token class match baseline. Original 26-test file retained at `checks/078-execution/RouteSongRetirementTest-before-095.kt.txt`, SHA `765eb427c28aa5892f211d9909269300dadf898723f93150f18065bbc1ca7a06`. Current file adds only the accepted #95 regression (27 @Test methods). Original test-authoring evidence and mechanical-correction evidence are unchanged. No other product/test source was edited by this implementation agent.

## Remaining verification

Designated tester must compile/run all 27 actual contract checks, unchanged song note/timer and related regressions, and exact AGENTS Gradle command. Independent PE/SecOps must bind actual final source/result identity. Local evidence does not establish phone/vehicle/OEM behavior or physical cancellation of an already-entered external command.
