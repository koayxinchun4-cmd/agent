# Nexus GitHub Skill Crawler

## Status

`PLANNED -> FOUNDATION`

The first foundation Skill is now present at `skills/github-skill-crawler/SKILL.md`.

## Purpose

Nexus should be able to learn engineering patterns from public GitHub Agent Skills without treating every public file as reusable code.

The intended pipeline is:

```text
GitHub search
    ↓
Candidate SKILL.md
    ↓
Pinned source/version
    ↓
README + LICENSE + provenance
    ↓
License gate
    ↓
Security review
    ↓
Concept extraction
    ↓
Nexus-native synthesis
    ↓
Human approval
    ↓
Skill Registry
```

## First target categories

1. Android CI / GitHub Actions
2. Gradle / Kotlin Android
3. Android testing
4. GitHub PR / Issue automation
5. Release automation
6. Code review / coding agent workflows

## Important boundary

This is a learning/import system, not an unrestricted web crawler.

- Prefer GitHub APIs/search and public repository content.
- Respect rate limits.
- Do not crawl private repositories without explicit authorization and available account permissions.
- Do not collect cookies, tokens, credentials, or unrelated personal data.
- Do not automatically execute discovered Skill instructions.
- Public availability does not imply permission to copy or redistribute.

## License decision states

| State | Meaning |
|---|---|
| `ADOPTABLE` | License/permission is clear for the intended reuse. |
| `REFERENCE_ONLY` | Useful for learning, but reuse rights are unclear or unsuitable. |
| `REJECT` | Unsafe, irrelevant, prohibited, or otherwise unsuitable. |

A Skill should not move to `INSTALLED` solely because it was found by search.

## Provenance record

Store at least:

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
nexus_changes
removal_status
```

## Current known learning sources

The Android CI foundation was synthesized after studying public examples including:

- `prasad-vennam/Awesome-Android-AI-Agent-Skills`
- `po4yka/RIPDPI`

Their licenses and source commits are recorded in the Nexus-native CI Skill. fileciteturn260file0

Nexus should continue to record each future source individually rather than assuming that all discovered Skills have the same license.

## Android implementation plan

### A. Network/source layer

Future classes:

- `GitHubSkillSearchClient`
- `GitHubSkillSourceReader`

Responsibilities:

- search candidate repositories/files;
- fetch a pinned `SKILL.md`;
- fetch README and license metadata;
- normalize GitHub URLs and commit references.

### B. Review layer

Future classes:

- `SkillLicenseGate`
- `SkillSecurityAnalyzer`
- `SkillAnalyzer`

Responsibilities:

- classify license permission;
- detect risky instructions;
- extract trigger, capabilities, tools, workflow stages, and validation rules;
- produce a reviewable structured record.

### C. Registry layer

Future classes:

- `SkillProvenanceStore`
- `SkillRegistry`

Suggested lifecycle:

```text
DISCOVERED
  ↓
LICENSE_REVIEW
  ↓
SECURITY_REVIEW
  ↓
REFERENCE_ONLY / APPROVED
  ↓
INSTALLED
  ↓
ENABLED
```

Removal flow:

```text
REMOVAL_REVIEW → DISABLED → REMOVED
                         ↘ RETAINED_WITH_BASIS
```

## UI plan

Do not make the crawler the first large frontend feature.

After the Android frontend is stable, add a Skill Review screen showing:

- Skill name
- source repository
- source path
- pinned commit/version
- license
- author/project owner
- risk summary
- extracted capabilities
- `Reference only` / `Install` decision

The user should explicitly approve activation of Skills with sensitive capabilities.

## Definition of done

The complete feature eventually needs to support:

`discover → fetch → provenance → license gate → security review → analyze → synthesize/reference → approval → registry`

It must also be possible to explain why a Skill was adopted, kept reference-only, rejected, or removed.
