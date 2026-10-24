# #78/#93 runtime verification — successful successor

Date: 2026-10-21. Independent tester; sole Gradle owner.
Result: **focused contract PASS and exact full AGENTS command PASS** for the
identities below. This is local worktree verification, not G5, hardware
acceptance, release or #92 isolated candidate validation.

## Source and mechanical correction lineage

| Input | SHA-256 |
| --- | --- |
| CarPlayController.kt | c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414 |
| BydNavigationOutputs.kt | 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d |
| BydClusterSong.kt | 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c |
| RouteSongRetirementTest.kt | 561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e |

All four identities were checked before/after focused and full execution; no
drift occurred. Test/product paths remain those in the approved execution brief.
Attempt 1 remains in test-runtime-evidence-20261021-078-attempt1.md: it failed
test compilation and executed no new contract cases. Its stale XML was not used.

For #97, the tester independently compared the preserved pre-correction test
snapshot to the current file: the only delta qualifies controllerLocks'
initializer as kotlin.collections.emptyList(), avoiding the route-frame helper
with the same name. **Mechanical source adequacy ACCEPT:** no assertion,
contract, fixture schedule or product source changed. Correction evidence is
test-mechanical-correction-20261021-078-097.md, SHA-256
45e49c8e4d655e37311d2d3d04af8b1675b76122ac508e3300a63737b93bb343.
Earlier #94/#95 source-adequacy lineage remains intact.

## Executed commands

Both commands used the locally configured JAVA_HOME and ANDROID_HOME values. The SDK binaries were checked before execution. No source or build configuration was changed.

```sh
./gradlew :shared:testDebugUnitTest --tests 'com.shilapi.xcertplay.hud.RouteSongRetirementTest' --rerun-tasks
```

Focused attempt 2: 02:15:25.596932–02:15:38.161609 local, exit 0,
BUILD SUCCESSFUL in 12s, 17/17 actionable tasks executed. The fresh XML contains
**27 tests, 0 failures, 0 errors, 0 skipped**, including #95. XML/HTML/log were
copied to focused-artifacts before the full run could overwrite shared reports.

```sh
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :home:testDebugUnitTest \
  :mobile:lintDebug :home:lintDebug :maphost:lintDebug \
  :mobile:assembleDebug :home:assembleDebug :maphost:assembleDebug
```

One full invocation: 02:16:19.630362–02:19:49.087324 local, exit 0,
BUILD SUCCESSFUL in 3m 29s. 250 actionable tasks: 50 executed, 200 up-to-date.

| Unit task/report | Tests | Failures / errors / skipped | Evidence freshness |
| --- | ---: | --- | --- |
| shared | 1573 | 0 / 0 / 1 | 240 XML suites newly written by this full run |
| common | 1527 | 0 / 0 / 0 | 200 XML suites newly written by this full run |
| home | 6 | 0 / 0 / 0 | Task UP-TO-DATE; existing single XML suite reused, not fresh execution |

Module mapping was verified in settings.gradle.kts: home and maphost reside
under samples/. Their reports and output metadata were collected from those
actual directories. The full shared report also contains the new contract's
27/0/0/0 result, independently of its preserved focused execution.

The sole skip is the existing
AirPlayPortSelectorTest.fallsBackWhenAnotherListenerOwnsTheWildcardPort.
Its source at lines 22–35 probes wildcard/specific-address listener overlap
and uses assumeTrue when the host permits it. This host-specific assumption
does not waive any new retirement-contract case. No failure/error is hidden
behind the skip. No additional test run was needed.

| Lint / assemble | Observed result |
| --- | --- |
| mobile lintDebug | Completed; newly generated XML: 20 warnings, no error/fatal issue |
| home lintDebug | Completed; lintReportDebug UP-TO-DATE: existing XML has 7 warnings, no error/fatal issue |
| maphost lintDebug | Completed; lintReportDebug UP-TO-DATE: existing XML has 1 warning, no error/fatal issue |
| mobile assembleDebug | Executed with packageDebug; mobile-debug.apk newly written, 24,881,628 bytes |
| home assembleDebug | UP-TO-DATE; existing samples/home/build/outputs/apk/debug/home-debug.apk, 5,478,841 bytes |
| maphost assembleDebug | UP-TO-DATE; existing samples/maphost/build/outputs/apk/debug/maphost-debug.apk, 2,537,766 bytes |

APKs were checked through output metadata and filesystem presence/size; none
was installed or released. Reused tasks/reports are explicitly distinguished
from fresh execution. Compiler/JVM/deprecation and lint warnings remain visible
in the logs; BUILD SUCCESSFUL is not a warning-free claim.

## Frozen regression execution

Fresh full-run XML proves every regression class named in quickstart, with
zero failures/errors/skips in these classes:

| Shared class | Tests |
| --- | ---: |
| BydClusterSongTest | 7 |
| BydClusterSongNoteTest | 5 |
| BydClusterSongTimerTest | 13 |
| ClusterSongWriteResultTest | 4 |
| ClusterTurnOverlayExpiryTest | 7 |
| BydHudRouteStateTest | 15 |
| NavigationOutputWorkerTest | 3 |
| RetirementTokenPlumbingTest | 4 |
| CarPlayControllerLifecycleRetirementTest | 22 |

These run unchanged; the full run therefore supplies the required legacy and
controller regression evidence without redundant class-specific reruns.

## Preserved evidence and remaining boundaries

All successful-run evidence is under
checks/078-execution/runtime-20261021-021513. Full XML paths/hashes and all
executed test names are preserved in the JSON summaries/manifests.

| Artifact in that directory | SHA-256 |
| --- | --- |
| source-identities.json | 89080afcd679e89d69688a581d3ac62427a8f4edbb668cd6a44cb0154cfb51fd |
| focused.log | a4a11d3d2f23060a9c86481bf1b103c43c32a2154c757204cf7fb41eabeea18c |
| focused-result.json | 389d1a50802cb66ddf8315299989b5b9bfe78b151bebd3888bc4be1d69027595 |
| focused-xml-summary.json | 004a8c510a64f97225730231bb206bc5c74207049e9ebcb78b2b9c8e870c98b8 |
| focused contract XML | d1f33adf3c5a491d282d582e3be6966c58bb9135b514cff13dda5b5a282813ac |
| full.log | 967366d4591dfd3911eac72bca102f71eaae203aea38bbc764f28f36b1403d2e |
| full-result.json | ef556dd9cc4d178857d36d8a149b79447737fcb6f5e53d6e94a3a214929fcfe5 |
| full-xml-summary.json | 06364f201ad763847ac0bfc977a800b0c3afc127e33aa1db98aaef128fb8325e |
| full-xml-manifest.json | 033c4069c3c9d5fc99c55c44b00247ec2e8e6953d6b6f7e5c3fd8d0c887872db |
| requested-task-outcomes.json | 501b90a378a518c3e1745a4f463edf221509bfa14fc1c02846416438364da967 |
| lint-apk-summary.json | 30732fa2e65c801f51e593bee7d2bcba7676d7dbaf23cd8ce5b35a23cd78522d |

Independent source/runtime adequacy follows in
review-post-implementation-pe-20261021-078.md. The tester also verified the
four frozen regions, unchanged token declaration/constructor and all other 46
G4 inventory inputs. PM-owned public-tree/whitespace/upstream checks and formal
SecOps evidence remain separate records. No Git/GitHub, credential, third-party
APK, real-device or release action was performed by this verifier. The result
does not resolve #91's historical provenance limitation or replace #92's later
isolated exact-candidate validation.
