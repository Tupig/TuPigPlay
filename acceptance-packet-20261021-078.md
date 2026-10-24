# Manual Acceptance packet — G5-078-R3

Created: 2026-10-21 (Asia/Shanghai). Ready for human decision; G5 not yet approved.
Risk: high/critical. This is bounded local-worktree acceptance evidence, not
isolated delivery, hardware acceptance, commit, push, issue closure or release.

## Delivered outcome

Retired/replaced sessions cannot commit old route/song state after retirement
wins the shared mutation order. Current owners retain useful route/song output.
Replacement clears old song metadata/timer intentions; null producers cannot
adopt an owned session. A delayed old navigation start cannot retire the new
owner. Stale queued callbacks/completions cannot restore old local state.

Exactly three product files were changed: the CarPlayRetirementToken class body,
BydNavigationOutputs.kt, and the BydClusterSong object. One focused test file was
added. Frozen controller surroundings, decoder/vendor suffix, parser/worker,
legacy tests, #71 work and unrelated source changes remain intact.

## Authority and final identity

- Root spec.md revision 2, 2026-10-20: 1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585.
- Approved build-package-20261020-078-r3.md: 96d021ceb04978f997faf71b12803f424f7a2246b9057c7e74fcc153afa9b625.
- Contract C-078-R3: b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83.
- G4 decision: receipt-20261021-078-g4.md; all 49 prior inputs checked before tests and first product edits.

| Final source | SHA-256 |
| --- | --- |
| shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt | c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414 |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt | 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt | 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c |
| shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt | 561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e |

## Verification and independent acceptance

| Evidence | Result |
| --- | --- |
| Forced focused contract | 27 tests, zero failures/errors/skips, exit 0, 12 seconds. |
| Exact full AGENTS command | Exit 0, BUILD SUCCESSFUL in 3m29s; 250 actionable tasks, 50 executed/200 up-to-date. |
| Fresh shared reports | 1573 records, zero failures/errors, one existing host-socket assumption skip. |
| Fresh common reports | 1527 records, zero failures/errors/skips. |
| Home unit task | UP-TO-DATE, six passing historical records reused, not newly executed. |
| Lint mobile/home/maphost | All required tasks successful, no error/fatal severity. Warnings 20/7/1; home/maphost report outputs reused. |
| Debug APK build | mobile rebuilt; home/maphost UP-TO-DATE with outputs present. No APK installed or published. |
| Frozen regressions | Every class required by quickstart freshly executed; song note/timer, parser/result/worker/controller all pass unchanged. |
| PE | ACCEPT source and runtime adequacy; review-post-implementation-pe-20261021-078.md. |
| SecOps | ACCEPT bounded source plus runtime evidence; review-post-implementation-secops-20261021-078-runtime.md and preserved source supplements. |
| PM boundary/static checks | Four frozen byte regions and all unrelated predecessor source identities preserved; public-tree screening includes proposed untracked code/evidence; tracked-tree scanner and git diff --check pass. Index empty, HEAD unchanged, upstream push disabled. |

The one skipped case is AirPlayPortSelectorTest.fallsBackWhenAnotherListenerOwnsTheWildcardPort,
whose existing assumption depends on host wildcard-bind behavior. None of the
27 new checks is skipped. Fresh shared/common reports contain 3100 records in
total (3099 non-skipped); do not add reused home reports as new execution.

## Acceptance mapping

| Contract acceptance | Evidence assessed |
| --- | --- |
| AC078-R3-01 | Executed useful A/B route/song positives, disabled cache and replacement reset. |
| AC078-R3-02 | Executed route/song replacement/end/close barriers; late frames/stale end/refresh and #95 publication successor. |
| AC078-R3-03 | Executed two sequential token orders and held-lock observation, plus independent same-monitor/caller source proof. No forced simultaneous-contention claim. |
| AC078-R3-04 | Executed standalone/admin positives and owned/stored-retired null rejection. |
| AC078-R3-05 | Executed duplicate/partial updates, controlled hidden expiry/grace, retained route, note priority and standalone equality. |
| AC078-R3-06 | Executed stale timers/queued work and actual write/clear completion through fake external results; same-text B must write for itself. |
| AC078-R3-07 | Executed real worker stale queues/entered action and pending publication/listener schedules; no vendor-internal cancellation claim. |
| AC078-R3-08 | Executed callback lock observations and bounded barriers; independent full mutation/dispatch lock audit. |
| AC078-R3-09 | Repeated fixture reset, required unchanged regressions, full build and independent/static reviews. |
| AC093-01 | Executed delayed A start, useful B before/after release and idempotent B restart. |

Exact method mapping and source proof are in the independent PE report; raw
logs/XML and hashes are preserved locally under checks/078-execution/runtime-20261021-021513.

## Corrective findings and remaining limitations

- #94: stable worker-barrier task-count observation; independently reviewed and executed.
- #95: exact consumed retired publication reservation cleanup; new deterministic B/terminal successor regression executed.
- #96: immutable original ended-token delegation; source audit prevents null adoption of B. Auxiliary call/map paths are faked in this focused fixture, so this specific remedy has source proof, not new direct auxiliary/device runtime proof.
- #97: test helper-name collision corrected by one qualified collection initializer; independent mechanical review and complete rerun pass. Failed attempt1 is preserved and never counted as executed tests.

Issues remain open pending applicable acceptance/closeout; this packet does not
silently close them. No unresolved blocker remains for this bounded local G5
proposal. Local results do not establish vehicle/iPhone/OEM compatibility,
physical cancellation of already-entered actions or aggregate queue bounds.
Existing lint warnings remain disclosed. Raw build reports contain standard
local environment metadata and are not authorized for public publication.

#91's historical reconstructed-baseline limitation remains. #89's separate
accepted delta remains intact. #92 isolated candidate validation, predecessor
acceptance and expanded commit approval remain outstanding. The concrete next
source plan is build-package-20261021-092-successor-plan.md with a verified
27-path manifest; it is not itself an isolated candidate or passing build.

## Reviewable evidence identities

| Artifact | SHA-256 |
| --- | --- |
| test-runtime-evidence-20261021-078.md | d4498cd6cbcddf59540b5bf33e531c1d12bace460b7e5a060a4989c39ca1e147 |
| review-post-implementation-pe-20261021-078.md | 6b6c3a5b1ed499f13870f1b4540a4da19757df238cd9a82a381f72469dde1f16 |
| review-post-implementation-secops-20261021-078-runtime.md | 13cb266641fe83fc145f29121389238f02c99e1b4923e2d8633c31914dda981f |
| build-package-20261021-092-successor-plan.md | ce671b3991c01876ddabbed2f8f594f8aa8f0ccdfba7c23a221976f07dffce33 |
| checks/078-execution/delivery-successor-sources-20261021.json | b2e0746b00619f4e8584204b8b4b973f1cd4101175e6077f21cf0e3327efa6d3 |

## Proposed human decision

Manual Acceptance (G5-078-R3): approve the bounded local result and recorded
limitations, then continue already scoped #92 isolated delivery preparation.
No staging/commit/push/merge/issue closure/signing/release follows from G5.
Rollback, if required, restores only verified G4 original three-file snapshots
and this increment's exact new-test delta; never reset unrelated working work.
