# T007-01 / #94: stable navigation task observation

Date: 2026-10-21. Scope: mechanical actual-test correction only, unchanged C-078-R3. Issue: https://github.com/Tupig/TuPigPlay/issues/94.
Predecessor delivery: `test-implementation-evidence-20261021-078.md` remains unchanged; original test SHA-256 `dd28704cc8eb47d9cfcbbc245543ee8297211acbeef0b4511849d7c63b9f075f`.
Corrected test: `shared/src/test/java/com/shilapi/xcertplay/hud/RouteSongRetirementTest.kt`, 972 lines, still 26 @Test methods.
Corrected SHA-256: `765eb427c28aa5892f211d9909269300dadf898723f93150f18065bbc1ca7a06`.

Independent PE identified that taskCount can be sampled during executor dequeue/completed-task bookkeeping even after a Future completes. The original freely sampled observation was therefore not sufficient for the exact negative enqueue oracle.

The fixture now registers Thread-to-worker identity through the unchanged standard ThreadFactory. Every fixture Barrier registers itself before signalling entered, and unregisters on exit. counts() reuses a worker's existing barrier only when entered and not released; this includes workerBlock and an already-entered fake external action. It never drains that blocked old action. For other workers, counts() submits and records a short snapshot barrier, then waits for all workers to be observed inside their barriers before sampling any taskCount. It checks the same registrations/unreleased state before and after sampling. Worker dequeue/completion bookkeeping cannot progress while those task bodies are held.

Helper submission counts advance only after successful executor submission. In finally, only newly introduced snapshot barriers are released; every snapshot future receives a bounded completion/error check and callback errors are surfaced on the test thread. Existing scenario barriers remain controlled by their tests. The exact standard ThreadPoolExecutor / ArrayBlockingQueue / AbortPolicy implementations, real worker logic, source API dependencies and full state/enqueue/publication assertions are unchanged. There is no custom executor subclass or weakened expectation.

No Gradle/compiler/runtime test was run; this remains corrected test-source evidence pending independent PE recheck. No product or frozen test/planning file was edited. Original three-product hashes remain `2d7a6a88b30457ca64cd435ef39d48cdba8b4db0c166051299140bcc195d87d5`, `b8e2fdf64a31fe93baabee1bdadaab4ae8a4bbf3da31113a6c8b48d5ae7a68e7`, and `439b9349cf2ca312fcf7a4264c35c49a85498d89bfcdeded58f82c17ba164d1f` respectively.
