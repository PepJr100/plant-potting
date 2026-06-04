# PLANTPOTTING-0006 Draft Critique

## Codex Draft (`PLANTPOTTING-0006-CODEX.md`)

*   **Stronger than mine:** The "Phase 5 — Verification" section is much stronger. Codex explicitly lists critical CI scripts (e.g., `scripts/check-stub-isolation.sh`, `verifyNoNetworking`, and `scripts/integration-flow.ps1`), which my draft omitted entirely. Its recommended UX direction (hiding the suffix when it rounds to 0%) is also a safer and more pragmatic approach than my `<1%` suggestion.
*   **Weaker than mine:** The "Sequencing and Dependencies" section is just a flat list of rules rather than a structural guide. It also lacks strict constraints on the fixture crop (e.g., exactly 480x480, JPEG q80) which are critical for reproducible ML tests, whereas I was more specific about matching the Monstera fixture pattern.
*   **Missing tasks:** Updating the sprint ledger to mark the sprint complete. Furthermore, while handling the conditional cleanup for `FakeFixedIdentifier` is mentioned in its scope boundaries, it is missing a concrete task in the "Work Plan" checkboxes.
*   **Underweighted risks:** Codex mentions the risk of a non-representative photo ("Risk: the selected crassula photo is not representative enough"), but underweights the risk that the `<1%` UI fix might require new data flow plumbing (which Claude correctly identifies as a mandate violation).
*   **Wrong sequencing:** In "Phase 3 — Accuracy Assertion and Threshold Decision", it specifies adding the test assertion ("If the probe clears... add an assertion"), *then* conditionally seeding the threshold ("If the probe maps correctly... seed crassula-ovata"), *then* updating mapper tests. If the threshold needs seeding, the assertion will fail unless the threshold is seeded and its contract is updated *first*. The threshold decision and seeding must precede the final assertion.

## Claude Draft (`PLANTPOTTING-0006-CLAUDE.md`)

*   **Stronger than mine:** The technical depth in "Phase 6 — UX fix B" analysis is brilliant. It completely invalidates my draft's `<1%` recommendation by pointing out that `probabilityPct` is already a floored `Int` from the ViewModel (`coerceIn(0,100)`), meaning `<1%` would require net-new data plumbing, violating the scope boundaries. The "Sequencing & dependencies" DAG is also excellent, clearly showing that "Phase 4 — UX fix A" and "Phase 5 — UX fix B" are parallelizable tracks.
*   **Weaker than mine:** It is overly prescriptive to the point of fragility. Pinning exact line numbers like `ModelScoreMapper.kt:55-66` and `LowConfidencePickerScreen.kt:81` in the task list guarantees the plan will drift and become inaccurate if any unrelated commits land before this sprint.
*   **Missing tasks:** While it mentions splitting `FakeFixedIdentifier` in the text (Section 3), it completely omits it from the actionable checkbox list in Section 4, unlike the threshold semantics revisit which got its own checkbox in "Phase 3 — `perSpeciesThresholds` decision".
*   **Underweighted risks:** It underweights the risk that the conditional cleanups (like revisiting the threshold semantics in Phase 3) might blow up the sprint scope if triggered. It suggests a logged note if deferred, but if forced, it could derail the timeline and violate the "deliberately narrow" goal.
*   **Wrong sequencing:** In "Phase 3 — `perSpeciesThresholds` decision", it conditionally includes revisiting the top-1-only override semantics. If this condition fires, it essentially blocks the entire branch. Yet the dependency diagram claims Phase 4 and Phase 5 are independent. If an architectural cleanup is triggered on the same branch, it inherently blocks the "independent" UI string updates from being merged concurrently.

## Conclusion

If I were merging, I'd keep the **explicit verification scripts list (Phase 5)** from the Codex draft, and the **dependency DAG and UX fix B implementation analysis (dropping the suffix instead of `<1%`)** from the Claude draft.
