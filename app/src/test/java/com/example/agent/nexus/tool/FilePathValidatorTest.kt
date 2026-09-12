package com.example.agent.nexus.tool

import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class FilePathValidatorTest {
    @Test
    fun accepts_safe_relative_path() {
        assertNull(FilePathValidator.validate("notes/today.txt"))
    }

    @Test
    fun rejects_absolute_and_windows_paths() {
        assertNotNull(FilePathValidator.validate("/etc/hosts"))
        assertNotNull(FilePathValidator.validate("C:\\secret.txt"))
    }

    @Test
    fun rejects_invalid_segments_and_null_character() {
        assertNotNull(FilePathValidator.validate("notes//today.txt"))
        assertNotNull(FilePathValidator.validate("notes/./today.txt"))
        assertNotNull(FilePathValidator.validate("notes/\u0000.txt"))
    }
}
