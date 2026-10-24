# #78/#93 runtime verification — attempt 1

Date: 2026-10-21. Role: independent tester and sole Gradle owner.
Result: **FAILED at test-source compilation; no focused test executed.**
This is an actual failure, not expected RED or acceptance evidence.

The approved focused command used the locally configured JAVA_HOME and ANDROID_HOME values.

```sh
./gradlew :shared:testDebugUnitTest --tests 'com.shilapi.xcertplay.hud.RouteSongRetirementTest' --rerun-tasks
```

Start: 2026-10-21T02:13:21.594170 local. End: 02:13:49.214552.
Exit code: 1. Gradle: BUILD FAILED in 27s, 15 actionable tasks executed.
Product Kotlin compilation completed; :shared:compileDebugUnitTestKotlin failed.

Source inputs were checked before and after the run with no drift:

- Controller: c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414.
- Navigation: 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d.
- Song: 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c.
- RouteSongRetirementTest: b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42.

## Precise failure and correction route

Log lines 58–59 report test line 212:50, initializer expected List<Any> but
received Iap2Frame. The controllerLocks initializer calls emptyList(), which
resolves to this test class's route-frame helper rather than the Kotlin
collection function. Qualifying kotlin.collections.emptyList() is a bounded
mechanical correction preserving every contract assertion. PM was notified;
the tester made no source change. A corrected identity and review are required
before another focused attempt.

## Preserved evidence

Directory: checks/078-execution/runtime-20261021-021303.

| Artifact | SHA-256 |
| --- | --- |
| focused.log | 5213cf82152651b3a10c484358816cc94295735a1c5334a3c24c837a59f9b5c9 |
| focused-result.json | ca5aaf1cb6f0d2f80ca882bdde3e32e6ca2008bc5d66014837e5df2698d79f0a |
| source-identities.json | d603e78806cf9407c858b7bcb3d9989e255ca58c0116f9ce68062e1cdcda1896 |

The preservation step copied 239 pre-existing XML files. **None was modified
since this attempt began.** They are stale reports and provide no executed
case totals for this attempt. No focused failure/error/skip count is inferred
from them. The full AGENTS command, lint and APK assembly were not run because
focused compilation failed. No Git/GitHub or device operation was performed.
