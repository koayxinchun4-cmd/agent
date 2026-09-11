package com.example.agent.nexus.skill

import org.junit.Assert.assertEquals
import org.junit.Test

class SkillLicenseGateTest {
    private val gate = SkillLicenseGate()

    @Test
    fun knownPermissiveLicenseIsAllowed() {
        assertEquals(SkillLicenseGate.Decision.ALLOWED, gate.evaluate("MIT"))
        assertEquals(SkillLicenseGate.Decision.ALLOWED, gate.evaluate(" Apache-2.0 "))
    }

    @Test
    fun missingOrUnknownLicenseRequiresReview() {
        assertEquals(SkillLicenseGate.Decision.REVIEW_REQUIRED, gate.evaluate(""))
        assertEquals(SkillLicenseGate.Decision.REVIEW_REQUIRED, gate.evaluate("Unknown License"))
    }

    @Test
    fun policyCanBeExtendedWithoutChangingRegistryContract() {
        val customGate = SkillLicenseGate(setOf("Custom-License"))

        assertEquals(SkillLicenseGate.Decision.ALLOWED, customGate.evaluate("Custom-License"))
        assertEquals(SkillLicenseGate.Decision.REVIEW_REQUIRED, customGate.evaluate("MIT"))
    }
}
