# Nexus Skills Source & License Policy

## Purpose

Nexus may support importing or installing Agent Skills from public GitHub repositories. This document defines the provenance and licensing rules for those Skills.

## Allowed sources

A third-party Skill may be accepted only when:

1. The source is a public GitHub repository or a repository explicitly permitted by its author.
2. The Skill has a clearly identifiable author or project owner.
3. The repository provides a license or other explicit permission that allows the intended use, modification, or redistribution.
4. The imported Skill records its original source URL and the license information available at the time of import.

"Public on GitHub" does **not** by itself mean "free to copy". A public repository without an applicable license must not be treated as an automatically reusable Skill source.

## Required attribution record

Every imported Skill should have a provenance record containing at least:

```text
skill_id
skill_name
source_repository
source_path
source_commit_or_version
license
original_author
import_date
modification_status
```

Example:

```text
Skill: example-skill
Source: https://github.com/example/project
Path: skills/example/SKILL.md
Version: <commit-sha-or-tag>
License: MIT
Author: <author>
Imported: 2026-09-08
Modified: no
```

## Modification and redistribution

If a Skill is modified, Nexus should preserve the original attribution and license notices required by the source license and clearly record that the Skill was modified.

Nexus must not remove upstream copyright notices or present third-party code as original Nexus code.

## GPLv3 compatibility

Nexus core is intended to use GPLv3-only. Third-party Skills are **not automatically relicensed as GPLv3 merely because they are used by Nexus**. Each Skill must be evaluated according to its own license and the way it is integrated or distributed.

Skills with licenses that impose incompatible distribution conditions must not be copied into the Nexus source tree without an explicit legal basis.

## Security review

A valid software license does not make a Skill trustworthy. Before activation, imported Skills should be checked for:

- arbitrary network requests
- secrets or credential collection
- destructive commands or irreversible actions
- attempts to bypass Android security boundaries
- unexpected file or system access
- prompt-injection or instruction-hijacking content

A Skill must not receive sensitive Android permissions merely because it was imported from GitHub. Nexus should request only the permissions necessary for the specific operation and require user confirmation for sensitive actions.

## Current repository status

The current repository contains the historical architecture direction for importing GitHub `SKILL.md` files, but this repository search did not identify a current set of external Skill source files that can be verified as already imported. Therefore, Nexus should treat this policy as a gate for future Skill imports rather than claiming that every existing Skill is already sourced from a verified open-source repository.

## Goal

Nexus remains a community-friendly project: people may discover, review, contribute, fork, and share Skills, while the project preserves attribution, respects upstream licenses, and keeps user data separate from Skill source code.
