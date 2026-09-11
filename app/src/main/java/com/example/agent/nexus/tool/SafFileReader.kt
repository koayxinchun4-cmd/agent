package com.example.agent.nexus.tool

import java.io.InputStream

/** Reads one user-selected SAF document without requiring broad filesystem access. */
class SafFileReader(
    private val openStream: (String) -> InputStream?
) {
    fun read(uriString: String, maxBytes: Int = MAX_READ_BYTES): ToolResult {
        val uri = uriString.trim()
        if (uri.isEmpty()) return ToolResult.Failure("未提供选取文件的 URI")
        if (maxBytes < 1) return ToolResult.Failure("文件读取上限无效")

        return runCatching {
            val bytes = openStream(uri)?.use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                val output = java.io.ByteArrayOutputStream()
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > maxBytes) {
                        return ToolResult.Failure("选取文件过大，暂不读取（上限 ${maxBytes / 1024} KB）")
                    }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            } ?: return ToolResult.Failure("无法读取选取的文件")

            ToolResult.Success("选取文件读取完成\n\n${bytes.toString(Charsets.UTF_8)}")
        }.getOrElse { error ->
            ToolResult.Failure("选取文件读取失败：${error.message ?: "未知错误"}", error)
        }
    }

    companion object {
        const val MAX_READ_BYTES = 256 * 1024
    }
}
