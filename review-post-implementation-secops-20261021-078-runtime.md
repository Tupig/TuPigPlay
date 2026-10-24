# Independent SecOps runtime-evidence supplement — #78/#93

Date: 2026-10-21. Reviewer: `secops_078`, independent of product/test authorship and Gradle execution. Scope: T013 review of the preserved local execution evidence and unchanged reviewed source. No tests were rerun by this reviewer.

**Decision: ACCEPT the bounded SecOps source and runtime-evidence review for the identities below.** The prior source-only runtime reservation is resolved by the inspected focused/full evidence, subject to the explicit reuse and hardware limits below. No unresolved security/privacy finding was identified. This does not grant G5, issue closure, staging/commit/push, public evidence publication, release or #92 isolated-candidate acceptance.

## Source and review lineage

| Current source | Independently reverified SHA-256 |
| --- | --- |
| shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt | c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414 |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt | 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt | 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c |
| shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt | 561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e |

Preserved source review: `review-post-implementation-secops-20261021-078.md`, SHA-256 `6e112d463b491602409e95f2870c47f07b6d9b52ef3bde8309f045c67ac07d5c`. Mechanical #97 supplement: `review-post-implementation-secops-20261021-078-097.md`, SHA-256 `cb7d93edb49c604dbf37aa4bb81b75a65d0c51c9dad1a23d972e7767de6fd4c2`. Their source/lock/frozen-region/fixture analysis remains applicable; no new source change was found.

Final tester evidence inspected: `test-runtime-evidence-20261021-078.md`, SHA-256 `d4498cd6cbcddf59540b5bf33e531c1d12bace460b7e5a060a4989c39ca1e147`. The reviewer also inspected underlying result JSON, logs and XML rather than relying only on the tester's conclusion.

## Bound execution evidence

Successful evidence root: `checks/078-execution/runtime-20261021-021513`.

| Artifact relative to that root | Verified SHA-256 |
| --- | --- |
| source-identities.json | 89080afcd679e89d69688a581d3ac62427a8f4edbb668cd6a44cb0154cfb51fd |
| focused.log | a4a11d3d2f23060a9c86481bf1b103c43c32a2154c757204cf7fb41eabeea18c |
| focused-result.json | 389d1a50802cb66ddf8315299989b5b9bfe78b151bebd3888bc4be1d69027595 |
| focused-artifacts/test-results/testDebugUnitTest/TEST-com.shilapi.xcertplay.hud.RouteSongRetirementTest.xml | d1f33adf3c5a491d282d582e3be6966c58bb9135b514cff13dda5b5a282813ac |
| full.log | 967366d4591dfd3911eac72bca102f71eaae203aea38bbc764f28f36b1403d2e |
| full-result.json | ef556dd9cc4d178857d36d8a149b79447737fcb6f5e53d6e94a3a214929fcfe5 |
| full-xml-manifest.json | 033c4069c3c9d5fc99c55c44b00247ec2e8e6953d6b6f7e5c3fd8d0c887872db |

All 441 manifest XML paths were independently checked for existence, containment, non-symlink files and matching hashes. Current four source hashes match source-identities.json. Focused/full result records show exit 0 and no source drift.

The earlier failed attempt remains separate: `checks/078-execution/runtime-20261021-021303/focused.log`, SHA-256 `5213cf82152651b3a10c484358816cc94295735a1c5334a3c24c837a59f9b5c9`. It records test compilation failure, not an executed contract failure or expected RED. Its old XML was not used for execution totals. The accepted #97 qualification correction is the sole source delta between that attempt's test and the successful successor.

## Independently inspected outcomes

| Evidence | Result and interpretation |
| --- | --- |
| Forced focused contract | Log says BUILD SUCCESSFUL in 12s; actual XML contains 27 distinct cases, zero failures/errors/skips and empty system-out/system-err. Includes both token orders, all route/song lifecycle barriers, same-text completion, #93, #95 and fixture reset. |
| Exact AGENTS command | Result JSON task list matches the required command; log says BUILD SUCCESSFUL in 3m 29s. Task outcomes distinguish actual execution from UP-TO-DATE. |
| Shared full run | 240 fresh XML suites, 1573 records, zero failures/errors, one existing host-socket assumption skip. New contract again reports 27/0/0/0. |
| Common full run | 200 fresh XML suites, 1527 records, zero failures/errors/skips. |
| Home full task | UP-TO-DATE; six passing records from one reused XML suite. These are not newly executed records. |
| Fresh-record total | Shared plus common total 3100 records, including the one assumption skip. Do not describe the combined 3106 stored records as all fresh execution. |
| Lint | Mobile generated 20 warnings; reused home/maphost reports contain seven/one warnings. XML severity inspection found warnings only, no error/fatal issue. BUILD SUCCESSFUL is not a warning-free assertion. |
| Assemble | Mobile packaging/assemble actually executed; home/maphost outputs were UP-TO-DATE. This reviewer relied on task/output metadata and did not read APK payloads or install a package. |

The skipped `AirPlayPortSelectorTest.fallsBackWhenAnotherListenerOwnsTheWildcardPort` is tied to its existing host wildcard/specific-address socket assumption in source. It does not waive any retirement-contract case. Frozen song note/timer suites freshly report five and thirteen passing cases respectively, and controller lifecycle reports 22 passing cases with no skip. Existing diagnostic privacy canary suites report two shared and four common passing cases; synthetic fixture/canary inputs are not treated as actual personal data.

Runtime results corroborate the approved local guard/ownership/completion/cleanup tests. They do not turn the sequential token checks into a forced simultaneous-contention experiment; the exclusion argument still combines those executed outcomes with the prior independent same-monitor and caller lock audit. #96 remains specifically supported by immutable ended-token source proof and frozen exact-identity call admission, not an invented new call/OEM runtime test.

## Runtime privacy and security review

**Scoped check completed; no suspected product PII leak, credential disclosure or newly introduced security finding was identified.** Scope was the successful focused/full logs, focused test XML, all preserved full-run test XML, three lint XML reports, the prior failed compilation log and the tester's final report. This comprised 448 log/XML files, with derived summaries used for cross-checking. Binary test-result stores, duplicate HTML/SARIF presentations, APK contents, device data and unrelated repository files were not audited.

Methods: parsed all test XML outcomes and stdout/stderr; classified nonempty streams and exception/diagnostic types; inspected the focused suite's empty streams and failure formatting against its already reviewed synthetic sources; performed bounded pattern triage for private-key blocks, common credential prefixes, email/MAC-like literals and network identifiers; checked source/evidence identities and manifest integrity. Location-only triage did not copy suspected values into durable records.

The reports contain standard local test-runner hostname metadata and local tooling paths. These are local build-environment metadata, not route/song payload telemetry or proof of a safe-to-publish report. Complete raw XML/logs remain local acceptance evidence outside the proposed source delivery; this review authorizes no publication or inclusion of them in #92. No hostname/address value is reproduced here. A future proposal to publish raw evidence needs its own bounded disclosure review.

Nonempty legacy-suite streams are framework/resource diagnostics, fixed style/intent warnings, JVM instrumentation notices and socket-closure traces. The new focused suite emits no stdout/stderr. No private-key/token/email/MAC literal was found in the bounded log/XML scan, and no new route/song snapshot dump or business payload log was identified. Synthetic assertions/canaries remain identified by their test provenance; an actual suspected leak would still require the Aegis P0/HITL process rather than being excused as metadata or a canary.

## Final scope of this acceptance

Security and observability conclusions are now supported by both the unchanged source review and inspected local execution evidence. The real workers/timers, controlled single writer, callback failure reporting and repeated fixture resets executed successfully in the approved local tests. Existing lint/framework warnings and reused tasks remain visible. No measured vehicle latency, real iPhone/OEM compatibility, external physical cancellation or aggregate queue-memory guarantee is made.

Documentation, rollback, source boundary and monitoring assessments from the source review remain unchanged. PM-owned public-tree/whitespace/upstream checks, PE's final runtime adequacy, human G5 and later #92 isolated exact-candidate validation remain distinct evidence/decisions. No issue is closed by this supplement, and #91's historical provenance limitation is unaffected. This reviewer performed only read-only evidence/source checks and wrote this supplement.
