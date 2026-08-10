# Versioned Tool Catalog v1

[简体中文](versioned-tool-catalog.zh-CN.md) · [Architecture](architecture.md) · [Task Plan](rules-first-task-plan.md) · [Status](../STATUS.md)

Status date: 2026-08-10. `TOOL-001` defines a reviewed, immutable metadata boundary between a proposed task plan and later tool gateways. Looking up or validating a descriptor never invokes Android, a model, OpenCode, root, or a network service. A catalog entry is not permission to execute it.

## Catalog

| Tool identity | Risk | Input → output | Rollback declaration | Audience | Timeout |
|---|---|---|---|---|---|
| `android.open_settings@1` | `low` | `empty.v1` → `activity.v1` | `none` | Android gateway | 5 s |
| `android.open_gallery@1` | `low` | `empty.v1` → `activity.v1` | `none` | Android gateway | 5 s |
| `android.open_dialer@1` | `low` | `empty.v1` → `activity.v1` | `none` | Android gateway | 5 s |
| `android.adjust_volume@1` | `reversible` | `volume.v1` → `volume.v1` | restore a captured snapshot through the same versioned tool | Android gateway | 3 s |
| `android.observe@1` | `observe` | `observe.v1` → `observation.v1` | `none` | Android gateway | 3 s |

The six referenced schemas are real files under `contracts/`. Empty-input launch tools accept no extra fields. Volume is a bounded non-zero relative change. Observation is restricted to foreground-package metadata and explicitly excludes screen text, accessibility trees, screenshots, and raw media. All outputs have `public_log_safe=false`; callers must not copy them into public logs or fixtures.

## Rollback semantics

Rollback is metadata for future gateway policy, not proof that recovery is implemented. `none` explicitly means that no automatic rollback is declared. `restore_snapshot` requires a before-state captured by a later executor and a matching reviewed tool identity in this same versioned catalog. `TOOL-001` performs neither capture nor restore.

The catalog rejects duplicate name/version identities, any version other than v1, missing risk/schema/audience fields, invalid timeouts, and rollback targets absent from the same catalog. The built-in list and lookup map are immutable after class initialization.

## Authority boundary

The rules-first planner may use catalog risk metadata to reject unknown or mismatched steps. It still produces only a dry-run proposal. `TOOL-002` now supplies the separate loopback/same-UID, one-use, short-lived authorization core, but it remains unwired and executes no adapter; catalog lookup itself still grants nothing.

OpenCode and root tools are intentionally absent. They require independent audiences and policy tiers rather than inheriting Android authority by name. Adding a tool requires a new reviewed descriptor, concrete input/output schemas, public synthetic positive and rejection fixtures, semantic validation, and a version change for incompatible behavior.

## Reproduce the evidence

```sh
bash apps/android/xiaohei-android/test.sh
python3 scripts/verify-tool-catalog-contract.py
python3 scripts/verify-tool-catalog-boundary.py
bash scripts/verify.sh
```

The public fixture set contains one exact five-descriptor catalog and four rejection cases for duplicate identity, unknown version, missing schema, and unresolved rollback. These are static synthetic records; they do not claim a device action or rollback run.
