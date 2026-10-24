# Successor actual-test adequacy — #95 publication drain

Date: 2026-10-21. Independent PE review of the precise added scenario only.
Decision: **ACCEPT source adequacy** under unchanged C-078-R3 and the existing
G4 authorization. The bounded navigation correction may follow this review.
This is not compilation, executed regression evidence or final acceptance.

## Verified lineage

- Previous accepted test, preserved at
  checks/078-execution/RouteSongRetirementTest-before-095.kt.txt:
  `765eb427c28aa5892f211d9909269300dadf898723f93150f18065bbc1ca7a06`.
- Successor shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt:
  `b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42`.
- Unchanged contract, increment 078/contracts/retirement-r3.md:
  `b4ff2beeaad9aca6d8067174a658908c839e863541d8bef9284b03a825c46b83`.

Exact comparison shows one 27-line test addition beginning at line 885:
retiredPublicationDrainCannotStrandReplacementOrTerminal. All previously
accepted fixture code and assertions remain byte-identical. The earlier
review-actual-test-adequacy-20261021-078.md remains historical evidence for its
original identity; this review binds its unchanged coverage plus the new
scenario to the successor identity.

## Scenario assessment

The test first establishes useful A and a drained listener baseline, then
queues a distinct A publication and verifies a posted drain. It retires the
actual controller-issued identity while explicitly observing that A remains
the stored navigation owner. Draining the paused main looper consumes the old
callback while normal token mutation admission is denied. The unchanged
delivery count positively rejects stale A delivery.

Two independent fixture iterations then exercise the useful successor:
replacement B must remain admitted and deliver its distinct route; actual
controller close must deliver the terminal null. Each requires exactly one
new listener delivery and finally checks drainPosted is false and the pending
revision is cleared. Thus merely dropping A and leaving the queue stranded
cannot satisfy the test. Existing lock observations and cleanup remain active.

Targeted inspection of the current navigation postDrain path confirms the
source-backed failure basis: cleanup of the consumed ticket is inside the
admitted mutation closure, so retired A can leave the scheduling flag set.
This inspection is not a runtime reproduction.

The added order elaborates existing stale publication, useful-current-owner
and terminal delivery obligations in AC078-R3-02/07, with AC078-R3-08 fixture
lock constraints. It changes no ownership/null-producer/retirement semantics,
expected callback admission rule or product boundary. Direct retirement of a
real issued identity follows the already accepted stored-retired fixture
pattern; it does not fabricate ownership. No new API or relaxed oracle appears.

No new adequacy finding remains for this delta. #96 and any unrelated product
correction are outside this narrow review. The reviewer ran no Gradle/compiler
or runtime test and modified no product/test file. Required focused/full runtime
verification and post-implementation review remain pending.
