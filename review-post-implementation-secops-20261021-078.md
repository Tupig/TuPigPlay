# Independent post-implementation SecOps source review — #78/#93

Date: 2026-10-21. Reviewer: `secops_078`, independent of product and test authorship. Authority: G4-078-R3, C-078-R3 and ADR r3.2; root spec revision 2 remains authoritative.

**Decision: ACCEPT the bounded source/security review at the identities below. Runtime acceptance remains pending.** No unresolved source/security finding or suspected PII leak was identified. This report does not assert successful compilation, executed contract checks, full Gradle checks, G5 acceptance, issue closure, hardware behavior or delivery readiness. A changed reviewed source identity requires successor review evidence.

## Reviewed identities

| Repository-relative artifact | SHA-256 independently verified |
| --- | --- |
| implementation-evidence-20261021-078.md | 3d79e7e4f0ddafe66fc74e5aef62577b2edb7d839cd95d34c03457736c4e50ee |
| shared/src/main/java/com/shilapi/xcertplay/orchestration/CarPlayController.kt | c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414 |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydNavigationOutputs.kt | 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d |
| shared/src/main/java/com/shilapi/xcertplay/hud/BydClusterSong.kt | 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c |
| shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt | b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42 |

The approved package remains `build-package-20261020-078-r3.md`, digest `96d021ceb04978f997faf71b12803f424f7a2246b9057c7e74fcc153afa9b625`; contract remains `specs/001-lifecycle-display-privacy/increments/078/contracts/retirement-r3.md`, digest `b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83`.

Independent byte-boundary verification matched all four frozen-region hashes: controller prefix `66d64253b89fd863beac924dadce9f1fc08107df76258849ca60b78b60ece93b`, controller suffix `3b5ac25d15149de4c5c839406186a6a169eeea82128e2a4da88200d45acd38b7`, song decoder prefix `e4006fd1cab98fafde467f81c246180d3b00c67c6335bbd90e06978ef13aec4e`, and song vendor-result/tool suffix `a966291fa94da85e9ead3965b878edbbf60ee2aff30b74ad277eef153071934e`. Thus this reviewed implementation preserves controller behavior outside the token class and the frozen song decoder/vendor region. This is not a whole-repository diff audit.

## Source findings and resolved early observations

No new unresolved finding remains in this source review. Early implementation findings are retained under their separately tracked issue identities:

| Issue / acceptance | Inspected remedy | Current evidence and limit |
| --- | --- | --- |
| #95; AC078-R3-02/07 | Navigation lines 414–424 release only the exact consumed drain ticket and pending object when token admission fails. They neither deliver retired payload nor update route/last-delivered state. | Exact-ticket cleanup prevents a dead posted flag from stranding B or terminal delivery. The added `retiredPublicationDrainCannotStrandReplacementOrTerminal` source covers both successors and unchanged stale delivery count. Its source adequacy receipt is `review-actual-test-adequacy-20261021-078-095.md`; execution remains pending here. |
| #96; AC078-R3-02/08 | Navigation lines 486–510 capture the original ended token inside the transition and delegate only that nonnull identity after terminal owner revalidation. No later lastEndedToken read or null fallback remains. | Frozen `BydCarPlayCall.end` checks explicit identity before retirement. A later B owner therefore cannot be adopted through null by the old navigation cleanup. Genuinely unowned cleanup is not delegated into this owned call path. This is source proof of the exact remedy, not executed call/OEM behavior. |

## Retirement, ownership and lock audit

The token class uses the same private `mutationLock` for `retire` and the admitted check-plus-closure; `isAdmitted` remains an atomic nonblocking read. Retirement invokes no callbacks. The guard and its lock-observation API expose neither the monitor nor credential material.

Existing controller replacement/end/close callers acquire retirement under controller lifecycle, with close additionally under wireless-resource ownership. The frozen navigation worker can retire under its worker monitor; the frozen call subsystem can retire under its lifecycle monitor. The paired HUD paths introduce no reverse edge back into those monitors while the token guard is held.

Navigation owns start/end and route-publication transitions under lifecycle ownership. Start computes application context/availability before locks, commits the already resolved song context by plain assignment, then calls the local song transition. Song and overlay state locks are taken in separate sections. The outside-lock start hook precedes the final owner/epoch/admission check and real worker enqueue, so late A cannot reach worker replacement after B (#93). Map token assignment is a leaf setter under navigation ownership after token/state release; map initialization and call attachment run outside protected locks after admission checks.

Route input captures the explicit producer identity and rechecks it after the unlocked hook. The real decoder runs once inside navigation → token → overlay for admitted ownership. Rejected producer paths do not mutate decoder/intent state. A retired stored owner cannot admit null producers. Valid NONE/duplicate frames retain the unchanged decoder's hidden freshness/grace behavior. Route listeners and Handler.post execute outside protected locks; pending publications carry local epoch, listener generation, revision and token identity. Terminal publication uses its unowned terminal epoch rather than normal admission of a retired token.

Navigation → worker enqueue occurs only after token/state release. The worker and fixture executor remain asynchronous with no CallerRunsPolicy. A callback thread does not itself hold navigation/token/state/worker admission locks during bridge action. This is not a guarantee that every submitting-thread lock has already been released before another thread starts a callback, as clarified in the preserved pre-code erratum.

Direct song operations capture owner/epoch under state, release state, then acquire token → state and recheck. The shared `mutate` helper prevents a token-retirement winner from performing a later owned local commit. Local owner start/end and standalone terminal transitions are state-only operations ordered by navigation or exact captured owner. No song → navigation/token acquisition occurs while holding song state.

Song producer input receives the explicit token, including rejection of null while a retired owner is still stored. B transition clears A decoder metadata, wanted value, notes/change intentions and announcement state. Retaining the last completed physical shown record does not populate B metadata: owned deduplication requires the same owner and epoch. The unowned equality rule remains restricted to null shownOwner, preserving the frozen legacy timer/note fixtures.

Each song action carries immutable owner, epoch, revision, card and context. Queue admission, physical entry and successful completion revalidate those values; timers additionally require their exact stored object identity. Preferences, executor submission, timer scheduling, command execution and fixed logs are outside token/state locks. A previously admitted command may finish; denied completion cannot change shown/stamps/firstLogged or enqueue follow-up. A captured dispatch may race retirement before actual enqueue, but its wrapper rejects on execution; the source and contract do not claim physical cancellation or universal zero stale queue entries.

## Test boundary and cleanup review

The sole new test uses real controller activation/end/close, real navigation workers and actual song timer/write/completion logic with synthetic external results. Controller identities are captured through the unlocked start hook; controller reflection reads only lock objects for current-thread observations, not private token ownership. Route reflection observes the unchanged parser state. Frame/business values are synthetic.

Navigation and song fake environments are installed before controller construction, covering tokenless bootstrap. Actual worker executors use standard bounded single-thread ThreadPoolExecutor with AbortPolicy. The controlled song scheduler never executes inline, has a bounded accepted-task count, and uses one actual thread; the entered A tests do not manufacture B shown state while that thread is blocked. Callback assertions are retained and surfaced outside production runCatching.

Teardown releases barriers, waits for owned futures, closes controllers and sessions, awaits controller teardown, cancels future timers and drains work/main-handler callbacks while fake ports remain installed. It then terminates only test-owned executors. Real ports are restored only after all owned executors terminate; otherwise teardown fails. Reset clears legacy and new route/song/ownership/timer/publication/stamp/counter fields, restores saved context/handler/clock and stream-control/preference state, and does not shut down production executors.

Frozen BydClusterSongNoteTest and BydClusterSongTimerTest retain the required field names and `endNote(Context, Any)` / `endChange(Context, Any)` wrappers. Their legitimate unowned reflected shown values still suppress output through the null shownOwner rule; owned same-text B requires its own command. Successful unchanged-suite execution and final quiescence remain runtime obligations, not conclusions of this source pass.

## Scoped PII/security check

**Completed for the stated source/evidence scope; no suspected PII leak identified.** Manual inspection covered the changed token class, full navigation file, song object through its command boundary, complete new focused test and implementation evidence. The #95 actual-test adequacy receipt and frozen caller/legacy-fixture context were also read. Frozen controller surroundings and vendor regions were identity/context checks, not an expanded audit or credential inspection.

Methods included data-flow and log-sink inspection, test assertion/failure-format review and bounded literal triage for private-key blocks, common credential prefixes and email-like literals. Triage output exposed only category/path/line. Six source/evidence regions were searched. The two song log sinks at lines 393 and 401 are fixed messages without route/song payload or token interpolation. Test field comparisons emit field labels rather than complete payload/token snapshots; positive values are explicitly synthetic. No identifying telemetry, external logger, credential load or APK payload read was added.

This does not certify the rest of the repository, actual device storage, frozen external shell implementation or future runtime logs. Runtime XML/logs and any subsequent source corrections are not included in this cutoff and require follow-up review. Any suspected future PII leak must be recorded as a stable P0 path/line-only finding and receive HITL disposition; this clean source result does not waive that requirement.

## Engineering dimensions and residual limits

| Dimension | Assessment |
| --- | --- |
| Security | Exact lifetime admission and stamped completion address stale local authority. No credential/APK access, upstream write, new permission, dependency or OEM protocol change. No unresolved bounded security finding. |
| Best practices | Acyclic lock edges, unlocked external work, real positive controls, separate parser/publication/enqueue observations, deterministic bounded waits and explicit source-versus-runtime claims are present. |
| Maintainability | One guard helper, small immutable owner/intent records and internal fixture seams stay within approved files. Frozen legacy compatibility is preserved. Future source changes must retain exact API/oracle and source-boundary requirements. |
| Observability | Fixed production outcome messages remain; test observations avoid payload/token dumps. Actual XML, callback failures and per-task build outcomes still need inspection. Source instrumentation alone is not behavioral proof. |
| Long-running monitoring | Applicable: real workers and song timers span sessions. Ownership, late completion and repeated reset scenarios are authored, with bounded test cleanup and no new production polling. No aggregate queue-memory, measured latency or hardware reliability guarantee is asserted. |
| Documentation/recovery | Implementation evidence maps changes and #95/#96 remedies to approved requirements, records frozen hashes and separates pending execution. Rollback remains exact G4 originals, preserving unrelated dirty work and #91 provenance limits. #89/#92 delivery identity refresh remains separate. |

This reviewer ran no compiler, Gradle, Git/GitHub mutation, device operation, credential access or runtime test. The sole written artifact in this assignment is this report. Before G5 preparation can claim verified completion, the designated tester/PE must provide final focused and full command results, runtime adequacy, final stable hashes and required static checks; SecOps must inspect the resulting evidence and any changed source. No issue is closed by this review.
