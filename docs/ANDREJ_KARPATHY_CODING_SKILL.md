# Andrej Karpathy Coding Guidelines Skill (`BAYAN AL-HUDA`)

Source: `https://github.com/multica-ai/andrej-karpathy-skills`

Behavioral guidelines enforced across the **BAYAN AL-HUDA** (`com.bayanulhuda.com`) Android codebase to prevent over-engineering, preserve existing features, and ensure verifiable, surgical modifications.

---

## 1. Think Before Coding
**Don't assume. Don't hide confusion. Surface tradeoffs.**
- State assumptions explicitly before modifying build scripts, UI layouts, or repositories.
- Present multiple interpretations when requirements are ambiguous rather than guessing silently.
- Prefer the simplest architecture that satisfies Android Material 3 and Play Policy constraints.

## 2. Simplicity First
**Minimum code that solves the problem. Nothing speculative.**
- No speculative abstractions or single-use wrappers.
- No redundant update services (`InAppUpdateService.kt` is the single updater).
- Keep Compose screens symmetrical, scrollable, and responsive without nested-scroll crashes.

## 3. Surgical Changes
**Touch only what you must. Clean up only your own mess.**
- Preserve 100% of sacred Quran, Hadith, Tafsir, and Dua texts, Firebase Authentication flows, Room databases, audio/video modules, and offline libraries.
- Do not refactor unrelated working code or alter `app/build.gradle.kts` `signingConfigs` in ways that break existing signing compatibility.
- Every changed line must trace directly to the user's request.

## 4. Goal-Driven Execution
**Define success criteria. Loop until verified.**
```text
1. [Inspect & Audit] → verify: view_file / grep on exact file:line targets
2. [Surgical Edit]   → verify: YAML / script / Kotlin syntax check
3. [Build & Test]    → verify: gradle :app:signingReport, :app:testDebugUnitTest, compile_applet
```
