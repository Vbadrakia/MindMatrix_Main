# PRD Gap Analysis & Refactor Log

This document tracks the repository alignment work against the Employee Performance Tracker PRD.

## 1) Repo vs PRD mismatch summary

### Architecture
- **Mismatch (before):** Core repositories depended on Firebase for CRUD/sync.
- **PRD requirement:** Room-first architecture in MVVM flow; Firebase optional/future.
- **Action:** Refactored Employee/Task/Performance repositories to Room-first operation.

### Database schema
- **Mismatch (before):** Column names diverged from PRD schema (`joinDate`, `phone`, `assignedTo`, `dueDate`, performance metric naming).
- **PRD requirement:** Canonical schema naming (`joining_date`, `contact`, `employee_id`, `deadline`, `timeliness_score`, etc.).
- **Action:** Added Room `@ColumnInfo` mappings and migration `4 -> 5` to preserve existing data while aligning schema.

### Reliability and migrations
- **Mismatch (before):** Only partial migration path from v3 to v4 existed.
- **PRD requirement:** Reliable persistence and maintainability.
- **Action:** Added explicit migration from schema v4 to v5 with data copy and index recreation.

### Reports
- **Mismatch (before):** Reports screen had a compile-time issue (`isLead` unresolved).
- **PRD requirement:** Reports screen must exist and be functional.
- **Action:** Fixed role guard variable used by export FAB visibility.

### Testing quality
- **Mismatch (before):** Added test had formatting/maintainability issues.
- **PRD requirement:** Maintainable MVVM codebase with testability.
- **Action:** Cleaned formatting in `EmployeeViewModelTest`.

## 2) Step-by-step execution log (PRD order)

1. **Database schema fixed** (column alignment + migration path).
2. **Repository layer fixed** (Room as primary source of truth).
3. **ViewModel compatibility preserved** through repository contract stability.
4. **Screen fix** applied to Reports role-check path.
5. **Analytics logic kept in repository layer** and moved to Room-backed computations.
6. **Navigation flow** unchanged in this patch.
7. **Reports/export** retained and compile issue fixed.
8. **Cleanup** via test formatting and data-layer consistency.

## 3) Remaining follow-ups for full PRD closure

- Complete UI-level audit for all PRD screens and exact flow sequence.
- Enforce color palette values and dark/light parity globally.
- Add additional report filter test coverage (department/time/performance) around ViewModel state transitions.
- Add targeted instrumentation/performance checks for dashboard <2s objective.
- Add CSV export unit tests and repository tests for analytics aggregations.
