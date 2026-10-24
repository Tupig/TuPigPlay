# #97 mechanical test compilation correction

Date: 2026-10-21. Authorized by PM after focused attempt 1: product sources compile; test line 212 resolves the unqualified emptyList call to the route-frame helper returning Iap2Frame.

Only change: `controllerLocks: List<Any> = emptyList()` becomes `controllerLocks: List<Any> = kotlin.collections.emptyList()`.

No product code, test assertions, scheduling, contract or semantic oracle changed. No Gradle invocation by this agent; independent exact-delta PE review and designated tester rerun remain required. This is a compilation correction, not behavioral RED or PASS evidence.

Predecessor snapshot: `checks/078-execution/RouteSongRetirementTest-before-097.kt.txt`.
Predecessor SHA-256: `b3681095869941231d66c9ac7619f81b6814279b3f1a27424c7a95c1fdb45b42`.
Successor test SHA-256: `561fc022f890f90223a5b809957ff802541b4469e36e424287d5eb4b06fb385e`.

Original implementation/test evidence is retained unchanged.
