# Execution brief — #78/#93 R3

Created: 2026-10-21. Risk: high/critical. Human G1/G2/G3 approved; requested
next decision is Execution Authorization (G4-078-R3). No G4 authority exists at
this brief's creation. This is T005 under the approved immutable package.

## Objective and authority

Deliver the bounded local repair for old-session route/song mutations and
#93 delayed navigation start, with useful current-session behavior and complete
local evidence. Root spec.md revision 2 is the only product authority. This
brief applies approved C-078-R3 and T006–013 without changing requirements,
assertions, APIs or source boundaries. Historical records keep their original
creation-time status; mutable progress remains in status.md.

| Bound input | SHA-256 |
| --- | --- |
| spec.md | 1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585 |
| build-package-20261020-078-r3.md | 96d021ceb04978f997faf71b12803f424f7a2246b9057c7e74fcc153afa9b625 |
| receipt-20261021-078-g3.md | aaa5e7a25e7b440a1b1e1ae6116d263316f73614adad659416817ee0234bbdc2 |
| inventory-20261020-078-g3.json | faccfcf9b3effb5cbe4bea1c10e4b73d52bd9eed183802ec5fe35775fa698365 |
| review-pre-code-pe-20261020-078-r3.md | d1cff53082b27acbc2a48cfc92d2e21a597f52af19bcf85153e739b936fa841d |
| review-pre-code-secops-20261020-078-r3.md | 68ef918697381cca4f0c8d8e72be7f0b91ff691df1282f4e75e0677286cc1af1 |
| review-pre-code-secops-20261020-078-r3-erratum.md | 4ffad1ac29d66bdd8fa5258eb74597fea04a6c9ae2c2cfbdb30cb4c4538b0e48 |
| review-pre-code-reconciliation-20261020-078-r3.md | fd6e7d5fac5bc3bfb2fc14cbc8c5ca7f2c0df7a6e57012303dcad4f1c741f3f7 |
| specs/001-lifecycle-display-privacy/increments/078/contracts/retirement-r3.md | b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83 |
| specs/001-lifecycle-display-privacy/increments/078/tasks-r3.md | 770c3d9696bdbc0ded79e4780b63fe229ab7ae447c77864a0eff9ae45237c900 |
| specs/001-lifecycle-display-privacy/increments/078/quickstart-r3.md | 7f97a1b8e1d732c33f1d7d7cbe58dcd031777d4f8f3f90849ce8bab70246bbd8 |
| checks/078-execution/source-baseline-20261021.json | 03c276c931230dad55ee37b684226bb741a7aeff1750ae9afbf24c1bb504a553 |

## Editable files and frozen regions

1. shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt:
   the only new test file, currently absent. Test author creates it only after
   explicit G4 and before any product change.
2. shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt:
   only CarPlayRetirementToken class body. Preserve every byte outside that
   class, including imports, controller lifecycle and #89 mask behavior.
3. shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt:
   approved owner/state/publication/start/terminal ordering and exact test support.
4. shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt:
   approved song ownership/cache/timer/completion logic and test support inside
   its object. Preserve the entire prefix before BydClusterSong (including
   imports/decoder) and suffix beginning ClusterSongWriteResult (vendor/tool).
   Use fully qualified new type names inside the allowed region as necessary.

All other product/tests are frozen: worker, route parser, legacy fixtures,
#71 privacy changes, build config and settings included. Preserve unrelated
working-tree changes and .deveco. Governance/verification records are written
separately; they do not grant a new product-file boundary. Existing original
three-file byte snapshots and four frozen-region digests are in the bound
manifest, with SHA equality to the reviewed source. Recheck immediately before
actual test creation and again before first product edit; stop on drift.

## Ordered work, role, entry and exit

Roles use the already HITL-approved current-session equivalent models. PM is
non-implementing and verifies evidence. Tester authors actual checks; a bounded
coder performs product edits; independent PE/SecOps review their evidence.
No concurrent editing of product files and one tester owns Gradle outputs.

| Stage / Spec Kit tasks | Role | Entry | Required action and exit |
| --- | --- | --- | --- |
| Record execution / T005 | PM | Explicit human G4 | Write dated G4 receipt with this brief, spec/package/inventory identities; verify original snapshots and new-test absence. |
| Actual checks / T006 | Tester | G4 receipt and identity checks | Author complete C-078-R3 in the one permitted file, using exact planned APIs. Cover all 10 AC and 16 method groups, including variants. Preserve three product inputs unchanged. |
| Check sufficiency / T007–008 | Independent PE, PM | Complete actual test source | Inspect actual assertions/positive controls/fixture/cleanup against contract; accept source adequacy before paired edits. Explicitly record planned-API compile dependency. Source adequacy is not runtime proof. |
| Token primitive / T009 | Coder | Actual-check PE ACCEPT and unchanged product baseline | Shared monitor for retire and withAdmittedMutation, nonblocking isAdmitted, bounded held-lock observation; controller-outside-class bytes unchanged. |
| Navigation and #93 / T010 | Coder | T009 | Guard state/intent commits; propagate producer token; reject owned null ingress; exact owner/epoch start/end/refresh/publication ordering and reviewed auxiliary treatment. Use real worker implementation unchanged. |
| Song lifetime / T011 | Coder | T010 | Owner/epoch/revision through cache, settings/notes/timers/write/clear completion, replacement clearing and exact named support. Preserve frozen bytes and legacy field/wrapper signatures. |
| Actual verification / T012 | Tester | Paired implementation complete | Compile/run entire focused contract, meaningful unchanged regressions and full AGENTS task list. Inspect XML/logs/lint/assembly outputs. No contract skips or fabricated RED results. |
| Independent result review / T013 | Independent PE/SecOps, PM | Complete executed evidence and final hashes | Runtime adequacy, full mutation/caller audit, final bounded PII review, frozen boundaries and AC->evidence reconciliation. Prepare local G5 packet; no automatic acceptance or delivery. |

Once G4 is approved, proceed through these internal stages without repeatedly
asking about ordinary in-scope implementation choices. Independent adequacy is
still a hard dependency. A scope/contract conflict or unresolved privacy finding
requires the specified change control; routine implementation corrections that
preserve the approved contract receive appropriate independent review and rerun.

## Non-negotiable implementation and check details

- Use real controller-issued A/B tokens captured by the legitimate unlocked
  navigation hook. No token construction/private-token reflection/owner writes
  to manufacture new test scenarios. Existing frozen low-level tests retain
  their historical fixtures and are not substituted for this new proof.
- All pause hooks, listeners, handler posts, preferences reads, external calls
  and waits stay outside protected token/state commits. Sequential token orders
  and held-lock observation combine with independent same-monitor source proof;
  do not invent forced simultaneous-contention evidence.
- Keep the approved lock DAG and no overlay/song nesting. Navigation -> worker
  enqueue is allowed only after token/state release and with standard bounded
  asynchronous workers. Non-inline execution means the callback thread itself
  holds no relevant monitor; it does not imply the submitting thread has
  released every monitor before another thread runs (SecOps erratum applies).
- A paused old producer must lose final local mutation admission after actual
  replacement/end/close; useful B output is a prerequisite to negative success.
  Accepted duplicate/NONE route applications can refresh hidden time state;
  only rejected ingress has zero application/state/intent effects.
- Strict owned-session null-producer rejection is separate from administrative
  owner capture and valid standalone input. Replacement clears A song metadata
  and timer/note intentions; playback-only B cannot inherit A title/artist.
- #93 hook is after initial nav admission and outside locks before final worker
  dispatch recheck. B must start, remain admitted and produce useful frames
  before/after late A; repeated B start preserves behavior. No mock-only worker.
- Run real song write/clear logic through fake external command results. A
  blocked command cannot coexist with fabricated shown=B on a single writer.
  B cache/wanted may advance; valid B completion is proved after A returns.
- Zero-new-enqueue assertions apply to the contract's rejected-ingress/stale
  completion schedules. A previously captured wrapper racing after unlocked
  dispatch may queue but must reject on execution. No new aggregate production
  queue-memory guarantee or physical vendor cancellation claim.
- Install fake external boundaries before constructing controllers. Preserve
  real target local lifecycle/worker/song logic. Drain callbacks with fakes in
  place, terminate only test-owned executors, then restore singleton fields,
  handler/clock/hooks/context/preferences. No shell/OEM/device action escapes.
  Legacy standalone shown equality and exact reflection-visible signatures
  remain; equal-text owned B still requires its own valid command completion.

Missing future support APIs may prevent the new test compiling before the
paired implementation. Do not introduce unsafe stubs or a separate seam-only
patch to manufacture a baseline. Label this dependency explicitly at source
adequacy. No new-contract pre-fix runtime RED is promised. After implementation,
all checks must actually compile and run; fixture/compile/timeouts are failures.

## Required commands and result evidence

Use the planned local JBR and Android SDK, a unique run-log location and one
Gradle owner. Before execution, verify binaries/SDK exist without changing
project configuration. Preserve exit status and XML for each run. Focused run:

```sh
JAVA_HOME="${JAVA_HOME:?Set JAVA_HOME}" \
ANDROID_HOME="${ANDROID_HOME:?Set ANDROID_HOME}" \
./gradlew :shared:testDebugUnitTest \
  --tests 'com.shilapi.xcertplay.hud.RouteSongRetirementTest' --rerun-tasks
```

Then execute unchanged relevant classes listed exactly in quickstart-r3.md;
use their current full-suite execution evidence when that already proves the
required regression, avoiding redundant runs without a reason. Mandatory full
AGENTS command:

```sh
JAVA_HOME="${JAVA_HOME:?Set JAVA_HOME}" \
ANDROID_HOME="${ANDROID_HOME:?Set ANDROID_HOME}" \
./gradlew :shared:testDebugUnitTest :common:testDebugUnitTest :home:testDebugUnitTest \
  :mobile:lintDebug :home:lintDebug :maphost:lintDebug \
  :mobile:assembleDebug :home:assembleDebug :maphost:assembleDebug
```

Require the focused contract's actual XML with zero failure/error/skip, useful
positive controls and full 10-AC trace. Report actual method counts rather than
assuming 16 prose groups equal 16 executed cases. Record full command exit
code/BUILD SUCCESSFUL and individual unit/lint/assemble outcomes, distinguishing
UP-TO-DATE/no-source from actual execution. Explain unrelated existing skips;
the new contract allows none. Run public-tree screening (including proposed
new files), git diff --check, frozen-region/boundary checks and disabled-upstream
push verification. Final PE validates same-monitor/caller source proof and every
local mutation path, not just test counts. Final SecOps checks actual sources,
fixtures and evidence for identifying log leakage.

Deliver a concise AC->case/source-audit->execution-evidence matrix and exact
source/test hashes. This is local acceptance evidence only. Worktree green
does not replace #92's later refreshed isolated candidate verification or prove
vehicle/iPhone/OEM compatibility.

## Stop, correction, rollback and authorization limits

A fourth product file, changed frozen test/import/decoder/vendor region,
semantic assertion/API/contract change, real external action, identity drift or
suspected privacy leak stops affected work. Report exact evidence and track each
new requirement/defect independently; comment on existing issues for scoped
findings. Do not weaken expectations to fit code. Mechanical same-contract
corrections require reviewer confirmation and fresh complete contract evidence;
semantic changes invoke Aegis downstream rebuild control and affected gates.

Rollback restores only this increment's captured original three-file bytes and
its new test changes after verifying the precise diff; never reset to HEAD,
erase predecessor changes or alter .deveco. Preserve all old evidence, including
#91's historical reconstruction limit and #89's separate local acceptance.

Requested G4 covers complete bounded local actual-test/fix/verification and
post-review work through a reviewable G5 packet. G5 acceptance, refreshed #92
isolated delivery, staging/commit, push/PR/merge, issue closure, signing/release,
runtime promotion and real vehicle/device operations remain later decisions.
Never access third-party MFi/private-key/APK payload or push to upstream.
