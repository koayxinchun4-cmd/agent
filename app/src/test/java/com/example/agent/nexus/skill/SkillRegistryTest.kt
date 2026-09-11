package com.example.agent.nexus.skill

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillRegistryTest {
    @Test
    fun installedSkillDefaultsToEnabledForBackwardCompatibility() {
        val root = Files.createTempDirectory("skills").toFile()
        val registry = SkillRegistry(root)
        registry.install("demo", "# Demo\n\nUse for tests.")

        assertTrue(registry.isEnabled("demo"))
    }

    @Test
    fun disablingAndEnablingSkillPersistsState() {
        val root = Files.createTempDirectory("skills").toFile()
        val registry = SkillRegistry(root)
        registry.install("demo", "# Demo\n\nUse for tests.")

        registry.disable("demo")
        assertFalse(registry.isEnabled("demo"))

        val reloaded = SkillRegistry(root)
        assertFalse(reloaded.isEnabled("demo"))

        reloaded.enable("demo")
        assertTrue(SkillRegistry(root).isEnabled("demo"))
    }

    @Test
    fun provenanceAwareInstallPersistsSourceMetadata() {
        val root = Files.createTempDirectory("skills").toFile()
        val provenance = SkillProvenance(
            skillId = "demo",
            skillName = "Demo Skill",
            sourceRepository = "owner/repo",
            sourcePath = "skills/demo/SKILL.md",
            sourceCommitOrVersion = "abc123",
            license = "MIT",
            originalAuthor = "owner",
            importDate = "2026-09-11",
            modificationStatus = "NEXUS_NATIVE",
            nexusChanges = "Adapted validation rules",
            removalStatus = "RETAINED"
        )

        SkillRegistry(root).install("demo", "# Demo", provenance)

        assertEquals(provenance, SkillRegistry(root).getProvenance("demo"))
    }

    @Test
    fun provenanceAwareInstallAcceptsMatchingContentHash() {
        val root = Files.createTempDirectory("skills").toFile()
        val content = "# Demo\n\nUse for tests."
        val provenance = SkillProvenance(
            skillId = "demo",
            skillName = "Demo Skill",
            sourceRepository = "owner/repo",
            sourcePath = "skills/demo/SKILL.md",
            sourceCommitOrVersion = "abc123",
            license = "MIT",
            originalAuthor = "owner",
            importDate = "2026-09-11",
            modificationStatus = "UNMODIFIED",
            nexusChanges = "",
            removalStatus = "RETAINED",
            contentSha256 = SkillIntegrity.sha256(content)
        )

        SkillRegistry(root).install("demo", content, provenance)

        assertEquals(provenance, SkillRegistry(root).getProvenance("demo"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun provenanceAwareInstallRejectsMismatchedContentHash() {
        val root = Files.createTempDirectory("skills").toFile()
        val provenance = SkillProvenance(
            skillId = "demo",
            skillName = "Demo Skill",
            sourceRepository = "owner/repo",
            sourcePath = "skills/demo/SKILL.md",
            sourceCommitOrVersion = "abc123",
            license = "MIT",
            originalAuthor = "owner",
            importDate = "2026-09-11",
            modificationStatus = "UNMODIFIED",
            nexusChanges = "",
            removalStatus = "RETAINED",
            contentSha256 = "0000000000000000000000000000000000000000000000000000000000000000"
        )

        SkillRegistry(root).install("demo", "# Different", provenance)
    }

    @Test(expected = IllegalArgumentException::class)
    fun provenanceAwareInstallRejectsUnknownLicense() {
        val root = Files.createTempDirectory("skills").toFile()
        val provenance = provenance(license = "Unknown License")

        SkillRegistry(root).install("demo", "# Demo", provenance)
    }

    @Test(expected = IllegalArgumentException::class)
    fun provenanceAwareInstallRejectsMissingLicense() {
        val root = Files.createTempDirectory("skills").toFile()
        val provenance = provenance(license = "")

        SkillRegistry(root).install("demo", "# Demo", provenance)
    }

    @Test
    fun registryCanUseCustomLicensePolicy() {
        val root = Files.createTempDirectory("skills").toFile()
        val registry = SkillRegistry(root, licenseGate = SkillLicenseGate(setOf("Custom-License")))

        registry.install("demo", "# Demo", provenance(license = "Custom-License"))

        assertTrue(registry.get("demo") != null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun provenanceSkillIdMustMatchInstalledSkill() {
        val root = Files.createTempDirectory("skills").toFile()
        val provenance = SkillProvenance(
            skillId = "other",
            skillName = "Other",
            sourceRepository = "owner/repo",
            sourcePath = "SKILL.md",
            sourceCommitOrVersion = "abc123",
            license = "MIT",
            originalAuthor = "owner",
            importDate = "2026-09-11",
            modificationStatus = "UNMODIFIED",
            nexusChanges = "",
            removalStatus = "RETAINED"
        )

        SkillRegistry(root).install("demo", "# Demo", provenance)
    }

    @Test(expected = IllegalArgumentException::class)
    fun enablingMissingSkillFails() {
        val root = Files.createTempDirectory("skills").toFile()
        SkillRegistry(root).enable("missing")
    }

    private fun provenance(license: String) = SkillProvenance(
        skillId = "demo",
        skillName = "Demo Skill",
        sourceRepository = "owner/repo",
        sourcePath = "skills/demo/SKILL.md",
        sourceCommitOrVersion = "abc123",
        license = license,
        originalAuthor = "owner",
        importDate = "2026-09-11",
        modificationStatus = "UNMODIFIED",
        nexusChanges = "",
        removalStatus = "RETAINED"
    )
}
