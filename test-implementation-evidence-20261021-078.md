# #78/#93 actual-test source delivery — T006

Date: 2026-10-21. Role: bounded test author. Authority: `receipt-20261021-078-g4.md` and `execution-brief-20261021-078-r3.md`.
Contract: C-078-R3, `specs/001-lifecycle-display-privacy/increments/078/contracts/retirement-r3.md`, SHA-256 `b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83`.
Root spec revision 2: `1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585`.

Created only the approved actual test file `shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt` (905 lines, 26 @Test methods covering all 16 planned groups and their variants).
Test SHA-256: `dd28704cc8eb47d9cfcbbc245543ee8297211acbeef0b4511849d7c63b9f075f`.

## Source coverage, not execution evidence

| Acceptance | Actual method(s) / observation |
| --- | --- |
| AC078-R3-01 | `currentOwnersProduceRouteAndSong`, `lateProducerCannotChangeReplacement`, `replacementClearsSongMetadataAndTimerIntent`: real A/B tokens, separate route/song positives, disabled-output cache, standalone/hud/cluster/FCE branch observation. |
| AC078-R3-02 | Six `routeMutationBarrierRejects*` / `songMutationBarrierRejects*` methods for replacement/end/close; late ingress after explicit end; exact complete snapshots, hidden route fields, counters and actual task observations. |
| AC078-R3-03 | `retirementGuardSequentialOrdersRetirementFirst` and `...MutationFirst`: finite protected closure, held-monitor observation, actual end/close retirement. Independent same-monitor source audit remains required. |
| AC078-R3-04 | `nullProducerIsRejectedOnlyWhileOwned`: positive unowned navigation/direct song ingress, owned null rejection, actual issued token retired while still stored, separate admin start/refresh/settings/note behavior. |
| AC078-R3-05 | `acceptedDuplicatesAndHiddenRouteTimersKeepPolicy`, `acceptedHiddenNoRouteAndEmptyListGraceAndRetiredFramesKeepPolicy`, replacement clearing, timer/admin cases, `wirelessEndRetainsRouteUntilOriginalExpiry`, repeated fixture reset and standalone equality. |
| AC078-R3-06 | `staleSongTimersCannotRestoreOrClearB`; separate queued write/clear; entered write, same-text write and clear completion methods; exact owner/cache/wanted/shown/timer/firstLogged state before and after stale return, then useful B completion. |
| AC078-R3-07 | `queuedStaleRouteWorkDoesNotEnterVendor`, `enteredRouteActionMayFinishWithoutLocalSuccessor`, `staleEndRefreshAndPublicationCannotEraseB`; real bounded workers and valid latest main-handler output. |
| AC078-R3-08 | Shared fixture records callback failures outside production runCatching; verifies current-thread nav/overlay/song/token/controller locks at hooks/listeners/external actions; bounded waits outside guards, real single writer, no concurrent-guard-overlap claim. |
| AC078-R3-09 | `fixtureResetRestoresLegacyStandaloneBehavior` plus explicit cleanup. Complete Gradle/static/frozen-regression/runtime/PII review remains T012/T013, not satisfied by source authoring. |
| AC093-01 | `delayedStartCannotRetireReplacement`: outside-lock A start hook, actual controller replacement, useful B output before and after A release, no stale start/clear/initialize, duplicate B start preserves state/output. |

Fixture installs both fake external environments before controller construction. Tokens come from the approved start hook, never constructors/private fields. Read-only reflection is restricted to controller lock observations and route decoder state. Direct `token.retire()` is used only for the explicit retired-but-still-stored state on a genuine controller-issued token; the replacement/end/close integration cases use actual controller entry points.

Navigation task observations use standard ThreadPoolExecutor taskCount less counted fixture drain/block tasks, together with real initialize/frame/clear entry records. Equality is checked at the named stable negative schedules after B setup; taskCount is not presented as a historical cumulative counter across queue.clear. Song immediate/timer enqueue counters are recorded by its controlled asynchronous scheduler; one real writer thread executes actual callback bodies, including captured stale timer bodies even if their futures were cancelled. Callbacks capture exceptions for assertion on the test thread; no swallowed callback assertion is treated as success.

Cleanup releases barriers, joins bounded fixture work, closes controllers/sessions, drains while fakes remain installed, cancels timers, terminates only test executors, then resets singleton environments and restores complete preference key/value sets and stream control. Constructor failure also invokes cleanup. Failed quiescence refuses restoration of real ports and fails the test.

## Dependencies and limitations

This is an actual source artifact only. No Gradle, compiler, JUnit, Robolectric, device, shell/vendor command or runtime test was executed. Future support APIs/types from the exact contract (token guard/lock probe, navigation environment/hooks/clock/snapshot/lock observations, song environment/snapshot/lock observations and token-aware onFrame overload) are currently absent. Independent PE must accept actual-source adequacy before paired product work; missing APIs are not RED, a skip or passing evidence.

No claim is made about physical cancellation, vehicle compatibility, frozen vendor caches, forced simultaneous token contention or aggregate production queue bounds. Route completion remains a local no-successor observation plus required source audit; song completion executes its actual local write/clear path through a synthetic result. No private material, APK or network connection is used. #91's historical reconstructed-baseline limitation remains unchanged; this run preserves the three original source snapshots.

Recomputed product hashes after authoring match approved originals exactly:

- BydNavigationOutputs.kt: `2d7a6a88b30457ca64cd435ef39d48cdba8b4db0c166051299140bcc195d87d5`.
- BydClusterSong.kt: `b8e2fdf64a31fe93baabee1bdadaab4ae8a4bbf3da31113a6c8b48d5ae7a68e7`.
- CarPlayController.kt: `439b9349cf2ca312fcf7a4264c35c49a85498d89bfcdeded58f82c17ba164d1f`.

No frozen test/planning/source file, Git index/HEAD, remote or issue was changed by this task. Next dependency: independent T007 actual-test source adequacy.
