package com.example.agent.nexus.io

import java.io.File

/**
 * Android-safe workspace abstraction. Implementations should map this to
 * app-private storage or a user-granted Storage Access Framework tree.
 */
interface MobileWorkspace {
    fun read(relativePath: String): String
    fun write(relativePath: String, content: String)
    fun list(relativeDirectory: String = ""): List<String>
}

/** Safe app-private implementation with traversal protection. */
class AppPrivateWorkspace(
    private val root: File
) : MobileWorkspace {
    override fun read(relativePath: String): String = resolve(relativePath).readText()

    override fun write(relativePath: String, content: String) {
        val target = resolve(relativePath)
        target.parentFile?.mkdirs()
        target.writeText(content)
    }

    override fun list(relativeDirectory: String): List<String> =
        resolve(relativeDirectory).listFiles()
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

    private fun resolve(relativePath: String): File {
        require(!relativePath.startsWith('/')) { "Absolute paths are not allowed" }
        val base = root.canonicalFile
        val candidate = File(base, relativePath).canonicalFile
        require(candidate == base || candidate.toPath().startsWith(base.toPath())) {
            "Path escapes the workspace"
        }
        return candidate
    }
}
