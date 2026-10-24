# Independent post-implementation PE and runtime adequacy — #78/#93

Date: 2026-10-21. Reviewer: independent PE and sole runtime verifier; author
of neither actual test nor product changes.
Decision: **ACCEPT the bounded implementation and local runtime adequacy**
against C-078-R3. No unresolved PE defect was found in the reviewed paths.
This component review does not grant human G5, certify device behavior or
replace formal SecOps, PM static checks or #92 isolated delivery validation.

## Exact evidence

| Reviewed input | SHA-256 |
| --- | --- |
| spec.md revision 2 | 1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585 |
| build-package-20261020-078-r3.md | 96d021ceb04978f997faf71b12803f424f7a2246b9057c7e74fcc153afa9b625 |
| C-078-R3 contract | b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83 |
| CarPlayController.kt | c560785741b6f49d6dfe85d2e229784d16caeb965f694d55488684109f070414 |
| BydNavigationOutputs.kt | 1232b7aae7035e675622006dae7e41728a195fb5724da2ad91c6d1ea9c05b33d |
| BydClusterSong.kt | 00d454c2b861101153b3bc1ff80eec69ecb571e91051234a88117828fa78583c |
| RouteSongRetirementTest.kt | 561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e |
| test-runtime-evidence-20261021-078.md | d4498cd6cbcddf59540b5bf33e531c1d12bace460b7e5a060a4989c39ca1e147 |

Source/test identities match before and after actual execution and at final
review. The four original frozen regions match their manifest hashes; the
token declaration/constructor is also unchanged. All other 46 inputs in the
G4 preparation inventory remain byte-identical. Product scope is the two HUD
objects and the token class body, with one new test. Existing #89 mask behavior,
parser/worker implementation, song decoder/vendor suffix and legacy tests were
not edited.

## Independent mutation and lock audit

**Token ordering.** retire writes the AtomicBoolean only under mutationLock;
withAdmittedMutation checks that same flag and invokes the local closure under
the same private monitor. isAdmitted remains a nonblocking atomic read. The
sequential runtime cases prove the two explicit orders and guard-held
observation. Source inspection establishes mutual exclusion; no claim of
forced simultaneous-contention runtime proof is made.

**Caller graph.** Existing controller activation/end use controller lifecycle
before retire; close additionally holds wirelessResourceLock. Unchanged call
end and worker replacement acquire their own monitors before retire. New
navigation guards use navigation -> token -> overlay OR song. Direct song
paths capture under state, release it, then enter token -> song state. No
guarded closure acquires controller/call/worker/navigation or nests overlay
with song. Navigation worker start/submit/clear execute after token/local-state
release, under nav ownership only. They use unchanged bounded asynchronous
workers; bridge callbacks do not execute inline on the submitting thread.

**Navigation commits.** Start resolves context/availability outside protected
work, commits owner/epoch, song context/ownership and any pending publication
inside the reviewed order, then invokes the start hook unlocked. Final exact
owner/epoch/admission checks precede each worker start, closing #93. Route
ingress performs both initial and final checks, calls the unchanged parser
once under the guard and commits its pending publication in that same local
section. Song ingress propagates the producer identity. Explicit null frames
cannot adopt an owned or stored-retired session.

Listener changes, current-owner refresh, publication selection and terminal
cleanup retain their separate administrative purpose. Pending publications
carry epoch, listener identity/generation and revision. Listener invocation and
handler posting are outside locks. #95's consumed-retired callback releases
only its exact ticket/candidate scheduling slot; it neither delivers A nor
modifies route/shown state, and cannot clear a replacement slot. The regression
executes both useful B and terminal successors after that precise ordering.

**Song commits.** ownerStarted resets old metadata/timer intentions only for a
new navigation owner/epoch, retaining the stamped physical shown record so
same-text B cannot inherit A's output identity. Frame, settings, note, expiry,
enqueue admission, command admission and successful completion all use the
captured owner/epoch through mutate. Intended card/revision is rechecked at
entry and completion. Preference reads precede guarded commits; scheduler,
shell/result boundary and logs follow lock release. Legacy timer signatures
and field types remain available. Unowned shown equality permits legitimate
standalone suppression; every completion still requires its exact lifetime.

ownerEnded creates a local terminal intent and returns an immutable captured
enqueue closure. Navigation invokes that closure after releasing its lock;
there is no scheduler call inside the owner transition. Standalone end retires
outside song state, rechecks its captured epoch and invalidates old timers.
Terminal unowned epochs prevent stale clears/completions from affecting B.

**Entered work and queues.** A captured wrapper can race into an asynchronous
queue after retirement, but entry rejects stale ownership. Already-admitted
external actions may finish; completion cannot restore stale local state.
The tests measure the contract's named schedules, with worker task accounting
held stable by fixture barriers (#94), rather than claiming universal physical
cancellation or historical cumulative queue counts across queue.clear.

**Auxiliary #96 limit.** End captures immutable endedToken with the terminal
owner epoch, then calls the legacy call boundary only with that non-null
identity after unlocked admission. It cannot later adopt mutable lastEndedToken
or null-current-owner semantics. The map token setter remains a source-audited
plain volatile assignment. Call/map auxiliary effects are deliberately faked
in the new fixture; #96 and those unchanged internals therefore have source
review, not new direct auxiliary runtime proof. Already-admitted auxiliary
effects retain the approved limitation.

## Executed acceptance coverage

Fresh focused XML shows 27/0 failures/0 errors/0 skips; fresh full shared XML
repeats all 27 cases. Exact method names and XML hashes are preserved under
checks/078-execution/runtime-20261021-021513. No contract group is skipped.

| Acceptance | Executed proof and source component |
| --- | --- |
| AC078-R3-01 | currentOwnersProduceRouteAndSong, lateProducerCannotChangeReplacement and replacementClearsSongMetadataAndTimerIntent: actual A/B usefulness, stale rejection, disabled cache and reset-on-B. |
| AC078-R3-02 | Six route/song replacement/end/close barrier cases, stale end/refresh and #95 successor publication case; exact payload/hidden-state/queue snapshots. |
| AC078-R3-03 | Both retirementGuardSequentialOrders cases plus independent same-monitor and caller audit above. |
| AC078-R3-04 | nullProducerIsRejectedOnlyWhileOwned: standalone/direct positives, owned and stored-retired rejection, administrative current owner. |
| AC078-R3-05 | Duplicate/partial updates, hidden NoRouteSet/empty-list grace and rejected-frame expiry; wireless retained-route expiry and standalone reset/equality cases. |
| AC078-R3-06 | Captured stale timers, queued write/clear, actual entered write/clear completion and same-text B; old completion cannot alter full B baseline, and B subsequently writes. |
| AC078-R3-07 | Queued stale route, entered route return, stale listener/publication and #95 drain recovery; source confirms no route-to-overlay completion callback or vendor-cache guarantee. |
| AC078-R3-08 | Runtime callback-thread lock observations at real hooks/external boundaries; finite guard closure; source audit of all dispatch/preferences/commit paths and the enqueue exception. |
| AC078-R3-09 | Repeated fixture/reset positive, unchanged legacy/controller regressions and exact full AGENTS success; source boundary checks above. PM static and independent SecOps records remain separate. |
| AC093-01 | delayedStartCannotRetireReplacement: real A/B lifecycle, useful B before/after delayed A, unchanged B admission, no stale start/initialize/clear and idempotent duplicate B. |

The full command exited 0: shared 1573 tests with 0 failure/error and one
existing host-assumption skip; common 1527 with zero failure/error/skip. Home's
6-case report was reused by an UP-TO-DATE task and is not claimed fresh. The
sole shared skip is AirPlayPortSelectorTest's wildcard-overlap assumption,
outside this contract. New tests all executed. Three lint tasks succeeded;
their warning counts and reused versus fresh reports are disclosed. Mobile APK
assembly executed; home/maphost assemblies were UP-TO-DATE with existing outputs.

Required unchanged regression XML is fresh: song 7, note 5, timer 13, vendor
result 4, overlay expiry 7, route state 15, worker 3, plumbing 4 and controller
lifecycle 22, all zero failures/errors/skips. This validates legacy reflected
shown/timer compatibility and lifecycle integration without editing those
tests. Additional redundant runs would add no distinct required evidence.

## Review dimensions and disposition

- Security/privacy: identity-bound local mutation and no producer-null adoption
  are enforced. New diagnostics do not log business payloads or token snapshots;
  actual fixtures use synthetic values. Formal SecOps remains a distinct review,
  and no repository-wide privacy certification is made here.
- Maintainability/best practices: existing parser, handlers, workers and writer
  are retained; ownership/revision state has explicit transitions and bounded
  test support. No dependency, global dispatcher or vendor protocol change was
  introduced. Imports and decoder/tool sections obey frozen boundaries.
- Observability/monitoring: long-running lifecycle behavior retains generic
  success/failure logs; runtime tests distinguish parser application, accepted
  work, external entry and valid completion. No real-device timing or new
  production monitoring service is claimed.
- Documentation/operations: source and runtime limitations, original snapshots,
  #94/#95/#96/#97 lineage, failed first attempt and successful successor are
  preserved. Rollback remains exact-increment-only. #91 historical provenance
  and #89 separate acceptance are not rewritten.

No additional PE finding blocks this bounded local result. #94's observation
repair, #95's scenario/correction and #97's single-line compile correction have
fresh runtime evidence; #96 retains its explicit source-only auxiliary limit.
PM must combine this review with static and SecOps evidence before presenting
human G5. No commit, issue closure, signing, release, device operation or #92
isolated-candidate acceptance follows from this report.
