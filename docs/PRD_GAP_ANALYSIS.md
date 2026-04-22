# PRD Gap Analysis & Refactor Log

## Current alignment focus
This pass shifts the project back to Firebase-first persistence (Firestore + Storage) while keeping MVVM boundaries.

## Completed in this pass
1. **Repository backend alignment to Firebase**
   - Employee, Task, and Performance repositories now use Firestore/Storage APIs as primary data source.
   - CRUD and streaming operations are repository-driven using `callbackFlow` and Firestore listeners.

2. **Model field compatibility**
   - Canonical PRD field names are used for query/write paths (`employee_id`, `deadline`, `overall_rating`, etc.) while preserving compatibility reads.

3. **UI logic separation (reports)**
   - Reports filter state and CSV creation remain outside composables (`ReportsViewModel`, `CsvReportBuilder`).
4. **Screen hardcoded string cleanup (in progress)**
   - Department screen moved to string resources for title/subtitle/dialog/actions to improve localization readiness.

## Remaining PRD closure items
- Expand lifecycle-collection enforcement from screens to any non-screen composables still observing flow state directly.
- Remove/retire Room module and entities fully once Firebase path is validated in all ViewModels.
- Finalize strict PRD screen-by-screen audit and user flow verification.
- Add/verify Firestore composite indexes for production query patterns.
