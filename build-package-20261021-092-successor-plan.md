# #92 successor delivery plan after bounded #78/#93 repair

Created: 2026-10-21 (Asia/Shanghai). Risk: high/critical. Planning evidence only.
Master: repository-root spec.md revision 2 dated 2026-10-20, SHA-256
1c4c5f3ca8e6896147b1728461e8d64fbe6b33b18bf80a10da161f5322b77585.
Requirements: R-059-03/04/05. No product requirement is added by this plan.

Predecessor package: build-package-20261020-092-delivery.md, SHA-256
2ea85e2448fac0fec8fab8a6a780ebb80de224058e92a85f982c002169a20d3f.
Repair package: build-package-20261020-078-r3.md, SHA-256
96d021ceb04978f997faf71b12803f424f7a2246b9057c7e74fcc153afa9b625.
Current work is authorized by receipt-20261021-078-g4.md through local verification
and G5 packet preparation. This record prepares later delivery; it authorizes no
staging, commit, push, release or issue closure.

## Exact source plan

Keep HEAD 448bc1d63fadc496450c7c01e33c3feb4b66f7ee -> P1 predecessor ->
P2 #89 corner-mask -> P3 #78/#93 retirement repair.

P1/P2 original patches and manifests remain immutable in checks/delivery-092.
P1 keeps #67/#68 together and the existing #71 boundary. P2 keeps the exact
accepted #89 three-path logical delta and historical #91 provenance limitation.
P3 is the exact new delta from the three G4 original source snapshots to the
reviewed repair sources, plus the one new RouteSongRetirementTest.kt. The
four-path repair intersects two existing delivery paths and adds song/test,
so the final selected source/test union is 27 paths.

The exact intended final path/hash list is
checks/078-execution/delivery-successor-sources-20261021.json, SHA-256
b2e0746b00619f4e8584204b8b4b973f1cd4101175e6077f21cf0e3327efa6d3.
It binds the 25 predecessor P2 paths plus BydClusterSong.kt and the new focused
test. Existing unrelated changes, .deveco, credentials, generated outputs,
APKs, tooling and governance artifacts are not silently added to these source
commits. Packaging governance evidence requires its own explicit selection if
later delivery includes it.

This is a final-source identity plan, not a materialized candidate tree or an
isolated build result. Current working-tree validation cannot substitute for
candidate verification. The P3 patch/tree and isolated work directory will be
created during the already scoped delivery preparation after local #78 G5 and
its prerequisite evidence; no new product changes are implied.

## Dependency-ordered remaining tasks

| Task | Role | Entry | Exit / verification |
| --- | --- | --- | --- |
| D092-R2-01 | PM | #78 source/runtime/post-review complete | Present G5-078-R3 with exact local evidence and limitations. |
| D092-R2-02 | PM + independent PE | Local #78 G5, unchanged final source identity | Reconstruct P1/P2/P3 in isolated source materialization; exact manifests and patch replay match all 27 intended hashes, frozen logical #89 delta preserved. No real index or HEAD mutation. |
| D092-R2-03 | Independent tester | Candidate identity verified; approved safe build inputs available | Run exact full AGENTS command on final isolated candidate; preserve logs/XML/lint/build results and before/after source manifest. Do not count intermediate P1's known pre-#89 failure as a green candidate or weaken tests. |
| D092-R2-04 | Independent PE/SecOps + PM | Complete isolated candidate evidence | Same-identity reviews, predecessor r6/r7.2 acceptance packet, explicit #91 and hardware limits. Existing #89 local acceptance does not accept predecessor work. |
| D092-R2-05 | HITL + PM | Concrete reviewed acceptance/commit plan | Obtain applicable predecessor G5 and expanded exact G6, then only authorized dependency-ordered commits. G9 push/PR/closeout remains separate. |

No tests or new behavior need to be invented for delivery. Reuse the accepted
contracts and actual regression sources. A source mismatch, unexpected copied
file, omitted dependency or failing candidate check stops promotion of the
candidate; report the exact problem with a separately owned issue. Do not read
credential or APK payloads to materialize the source tree. Upstream push stays
disabled. Preserve all prior working-tree changes and original evidence.

## Rollback and limits

This plan changes no Git state. Discarding a future isolated candidate does not
reset the workspace. Product rollback remains the precise G4 original snapshots
and bounded test delta, preserving predecessor work. #91 cannot be repaired
retroactively by these new manifests. No local test verifies actual vehicle,
iPhone or OEM compatibility; no release or runtime promotion is proposed here.
