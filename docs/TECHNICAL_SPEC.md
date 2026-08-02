# Norvexa OneStep — Technical Specification

## Product objective

Help a user move from a broad goal to a small, actionable next step while preserving an editable long-term plan.

## Core entities

- **Goal**: title, description, category, status, deadline, daily time budget.
- **Stage**: ordered section of a goal plan.
- **Step**: executable action with status, estimate, actual time, schedule, and optional note.
- **Settings**: theme, language, reminders.
- **AppData**: versioned local snapshot used for persistence and backup.

## Status rules

### Goal

- `DRAFT`: created but not selected as the current goal;
- `ACTIVE`: the single current goal;
- `PAUSED`: temporarily inactive;
- `COMPLETED`: all steps are completed or skipped;
- `ARCHIVED`: reserved for the archive workflow.

### Step

- `TODO`;
- `IN_PROGRESS`;
- `COMPLETED`;
- `SKIPPED`.

## Main flows

### Goal creation

1. User enters title, optional result description, category, daily minutes, and deadline.
2. A localized offline template creates three starter stages.
3. The first goal becomes active; later goals begin as drafts.
4. The user can fully edit the generated plan.

### Next step

The next step is the first in-progress step or the first available TODO step whose scheduled time has arrived. Postponed steps do not block a later available action.

### Completion

Completing or skipping a step records the date. When every step is resolved, the goal becomes completed. Completed steps contribute to completion statistics; skipped steps contribute to plan progress but not the completed-step metric.

### Backup

The entire local state is serialized to a schema-versioned JSON document. Import replaces the local state only after basic validation.

## Persistence

The MVP stores one versioned JSON snapshot in Preferences DataStore. This keeps the model simple for the initial release and supports atomic state updates and portable backup. A later Room migration is planned when notes, attachments, and event history become larger.

## Architecture

- Compose UI observes a `StateFlow<AppData>`.
- `MainViewModel` exposes intent-oriented operations.
- `GoalRepository` is the single writer for local state.
- WorkManager reads a consistent repository snapshot before showing reminders.
- UI does not directly modify persisted models.

## Security and privacy

- no API keys in the application;
- no cleartext network traffic;
- no broad storage permission;
- no contacts, SMS, location, call log, accessibility service, or device-admin permission;
- notifications are requested only when the user enables reminders;
- backup uses the system document picker;
- release builds enable code and resource shrinking.

## Accessibility

- semantic labels for important actions;
- Material controls provide minimum touch target sizing;
- text follows system font scaling;
- content is not communicated only through color;
- all major flows are keyboard/TalkBack navigable;
- animations are intentionally minimal in the MVP.

## Definition of done for 0.1.0

- project syncs with the documented toolchain;
- unit tests, lint, and debug assembly pass in GitHub Actions;
- a user can create, edit, execute, postpone, split, skip, and complete a plan offline;
- state survives process death and device restart;
- reminder scheduling can be enabled and disabled;
- JSON export/import works through the system picker;
- Russian, Ukrainian, English, and Polish resources are present.
