# Independent actual-test source adequacy — T007, #78/#93

Date: 2026-10-21. Reviewer: independent PE; no authorship of the test or paired
product changes. Decision: **ACCEPT the corrected actual test source** against
unchanged C-078-R3. This satisfies the source-adequacy dependency before paired
product work under the existing G4 authorization. It is not compilation,
runtime adequacy, a passing test result, G5 acceptance or delivery approval.

## Reviewed identities and source boundary

| Artifact | SHA-256 |
| --- | --- |
| spec.md revision 2 | 1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585 |
| build-package-20261020-078-r3.md | 96d021ceb04978f997faf71b12803f424f7a2246b9057c7e74fcc153afa9b625 |
| execution-brief-20261021-078-r3.md | 9dde0269992c273c32d5adaba667a168bcccb0ed8b83741dc0e87b4644c7349e |
| Increment 078/contracts/retirement-r3.md | b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83 |
| shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt | 765eb427c28aa5892f211d9909269300dadf898723f93150f18065bbc1ca7a06 |
| test-implementation-evidence-20261021-078.md, original delivery | 8a0fc572f2c78642a4312154fc8c8ff0ec1035a677e2112bb0030b2d23dcde7e |
| test-mechanical-correction-20261021-078-r1.md | b05c8df5df3c66a95af35d3b1f91f51367fdd11d20786e1e2ed3cdfe1d8a5284 |

Authority is receipt-20261021-078-g4.md. The corrected test has 972 lines and
26 @Test methods expanding all 16 contract groups. Three product input hashes
were recomputed and still equal the approved original snapshot manifest:
navigation 2d7a6a88b30457ca64cd435ef39d48cdba8b4db0c166051299140bcc195d87d5;
song b8e2fdf64a31fe93baabee1bdadaab4ae8a4bbf3da31113a6c8b48d5ae7a68e7;
controller 439b9349cf2ca312fcf7a4264c35c49a85498d89bfcdeded58f82c17ba164d1f.
No product edit was needed to conduct this source review.

## Finding and corrected lineage

**T007-01 / issue #94 — resolved in source.** The original test identity was
dd28704cc8eb47d9cfcbbc245543ee8297211acbeef0b4511849d7c63b9f075f
(905 lines). Its freely sampled ThreadPoolExecutor.taskCount observation could
read a dequeue/active/completion bookkeeping transition. Future completion
alone does not establish a stable worker-count observation. This affected the
exact negative enqueue evidence for AC078-R3-02/06/07/08; the original source
was not accepted for product entry.

The corrected fixture records worker threads using the standard ThreadFactory
and registers each fixture barrier before signalling entry. counts() at line
344 reuses only an entered, unreleased existing barrier; otherwise it submits
a helper snapshot barrier. All four workers must enter their barriers before
any count is sampled, with registrations/unreleased state checked before and
after. Helper counts include only successfully accepted submissions. Finally
releases only newly created barriers and checks their completion/errors with
bounds. A deliberately blocked old action is never drained by observation.

This makes the relevant task lifecycle stable while preserving exact standard
executor/queue/rejection classes and the original complete state/count oracles.
It is a same-contract mechanical fixture correction, not a weakened assertion,
new product seam or changed concurrency requirement. The original delivery
record remains historical; the correction record binds the accepted successor.
No unresolved source-adequacy finding remains.

## Acceptance coverage in actual source

All line references below address the corrected test file.

| Acceptance | Actual source and adequacy assessment |
| --- | --- |
| AC078-R3-01 | Lines 508, 525, 639: actual controller-issued A/B, useful route/song positives, selected standalone/HUD/cluster/FCE entry, disabled-output cache and replacement metadata clearing. |
| AC078-R3-02 | Lines 539–546: six separate route/song replacement/end/close methods share a real unlocked barrier schedule; full state, hidden parser fields and exact rejection deltas are checked before and after draining. Late producer after end is also covered. |
| AC078-R3-03 | Lines 573, 582: actual end/close retirement and both finite token orders; guard-held observation occurs without a wait or external operation inside the closure. Independent same-monitor source proof remains a later mandatory component. |
| AC078-R3-04 | Line 604: unowned navigation/direct song positives, owned and retired-but-stored null rejection, administrative current-owner behavior. Direct retire uses a real issued token only for the stored-retired case; integration retirement cases use actual lifecycle entry points. |
| AC078-R3-05 | Lines 659, 689, 722, 885, 924: duplicate and partial updates, accepted hidden timer changes, controlled exact expiry boundaries, note priority, wireless retention and standalone equality. Rejected old frames cannot change B expiry. |
| AC078-R3-06 | Lines 722, 764–766, 806–809: captured stale timer bodies, queued write/clear and entered successful write/clear completion. Same-text B must execute its own command; no fabricated shown=B or second concurrent writer. |
| AC078-R3-07 | Lines 789, 843, 862: real queued route workers, entered action completion, listener replacement and pending A rejection, explicit stale end/refresh and valid terminal delivery. Vendor-internal cancellation is not asserted. |
| AC078-R3-08 | Shared fixture observes current-thread nav/overlay/song/token/controller locks at hooks and external boundaries. Standard worker execution and bounded outside-lock barriers preserve the approved exception; source lock/caller audit remains required after implementation. |
| AC078-R3-09 | Lines 924, 954 and fixture close: repeated lifecycle/reset, canonical exposed state, no-shell standalone positive, preference restoration and bounded quiescence. Frozen regression suites, full AGENTS/static checks and final independent review remain T012/T013 work. |
| AC093-01 | Line 894: A pauses after real initial admission, real controller admits useful B, late A cannot add accepted start/initialize/clear or retire B, duplicate B remains idempotent, subsequent B frame reaches a real output worker. |

The full negative comparison at line 485 checks named navigation/song payload,
owner/timer identity and completion fields, hidden route fields, stable task
counts and listener deliveries. It excludes only the explicitly asserted
rejection-counter deltas. Counters alone do not replace state/output positives.
Navigation observations are used at the named controlled schedules, not as a
historical cumulative count across queue.clear or universal cancellation proof.

## Fixture and API assessment

Existing controller activation/end/close helpers, construction signatures,
Iap2Messages builders and preference setters were checked against current
source. Fake output boundaries are installed before controller construction;
tokens are captured from the approved hook, never fabricated. Reflection is
limited to controller lock observations and route state/lock inspection, not
private token identity or ownership mutation.

The controlled song scheduler records actual immediate/timer acceptance, keeps
ready-task FIFO and uses one asynchronous writer thread. Captured timer bodies
can run even after cancellation, exercising actual stale rejection. The fake
command boundary returns synthetic success through actual write/clear logic;
it does not claim to verify vendor response parsing. Existing parser regression
remains mandatory. Callback errors are collected and raised on the test thread,
so production runCatching cannot silently turn a failed observation into green.

Cleanup releases barriers, joins bounded helper work, closes real controllers/
sessions, drains while fake ports remain installed, cancels timers, terminates
only test executors, then resets singleton environments and restores complete
preference values and stream control. It refuses real-port restoration when
test-owned executors cannot quiesce. Constructor failure also invokes cleanup.
Canonical reset and useful standalone behavior are checked explicitly; frozen
legacy note/timer tests remain unchanged and still need their required run.

## Compile dependency and next action

The planned token guard/lock observation and navigation/song environment,
snapshot, hook and clock APIs are absent from current product source. This is
the approved paired-implementation dependency. No compiler, Gradle, JUnit or
Robolectric execution was performed; there is no baseline RED or runtime PASS.
Mechanical compilation issues, if discovered later, require correction and
renewed adequacy where relevant rather than changing the contract to fit code.

PM may use this exact accepted test identity for T008 and then delegate bounded
T009–011 under the existing G4 receipt. After implementation, all new checks
must actually compile/run without skips, followed by the unchanged regressions,
exact full AGENTS checks, same-monitor/mutation-path audit and independent
runtime/SecOps review. This source acceptance cannot substitute for that
evidence. Only this review file was authored by the reviewer; no product/test,
Git/GitHub, device or credential operation was performed.
