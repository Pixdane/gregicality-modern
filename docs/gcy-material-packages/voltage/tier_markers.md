# GCY Material Package: voltage/tier_markers

Tier marker materials used by circuit or recipe marker inputs.

This package table excludes materials whose `status` is `exists_in_gtceu`; those remain only in the master dedupe CSV/id map.

Source: `docs/gcy-material-dedupe.csv`, `migration_package=voltage/tier_markers`.

| GCY Field | Old Id | Modern/Canonical Id | Status | Source Category | Source Section | Note |
|---|---|---|---|---|---|---|
| `MAX` | `MAX` | `MAX` | `new_candidate` | `Markers` | `SIMPLE DUSTS` |  |
| `UEV` | `UEV` | `UEV` | `new_candidate` | `Markers` | `SIMPLE DUSTS` |  |
| `UIV` | `UIV` | `UIV` | `new_candidate` | `Markers` | `SIMPLE DUSTS` |  |
| `UMV` | `UMV` | `uxv` | `new_candidate` | `Markers` | `SIMPLE DUSTS` | old-tier marker UMV->GTM UXV canonical id |
| `UXV` | `UXV` | `opv` | `new_candidate` | `Markers` | `SIMPLE DUSTS` | old-tier marker UXV->GTM OpV canonical id |
