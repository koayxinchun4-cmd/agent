package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ChatSession::class, ChatMessage::class, AgentSkill::class, AgentMemory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun skillDao(): SkillDao
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexus_agent_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultSkills(database.skillDao())
                        populateDefaultMemories(database.memoryDao())
                    }
                }
            }
        }

        suspend fun populateDefaultSkills(skillDao: SkillDao) {
            val defaultSkills = listOf(
                AgentSkill(
                    id = "general",
                    name = "核心智能体",
                    description = "全能型高速移动 AI 助手，具备深度逻辑推理、日常答疑与情境感知能力。",
                    iconName = "smart_toy",
                    category = "productivity",
                    systemPrompt = "你是一个运行在 Android 设备上的全能移动端 AI 智能体助手（Nexus Agent）。请使用中文（华文）进行回答，提供清晰、直接、富有建设性的见解。善用 Markdown 格式、粗体、列表和代码块来呈现结构化内容。",
                    samplePromptsJson = "[\"如何优化手机电池续航与后台管理？\", \"总结 2026 年移动端端侧 AI 的关键趋势\", \"为我制定一份高效的每日工作与学习计划\"]",
                    isBuiltIn = true
                ),
                AgentSkill(
                    id = "office_pro",
                    name = "办公文档与周报",
                    description = "Marvis 级办公助手：格式化专业 Markdown 表格、撰写商务邮件、项目规划与会议纪要。",
                    iconName = "description",
                    category = "office",
                    systemPrompt = "你是 Nexus 办公文档专家，专精于商务报告、Markdown 格式化表格、正式邮件起草、项目需求规划（PRD）和会议备忘录。请始终使用清晰的中文排版、Markdown 表格和序号清单输出。",
                    samplePromptsJson = "[\"生成一份 Q3 产品路线图与里程碑表格\", \"起草一封向管理层申请项目预算的正式商务邮件\", \"将这几项工作成果整理为专业的工作周报总结\"]",
                    isBuiltIn = true
                ),
                AgentSkill(
                    id = "auto_tools",
                    name = "自动化与设备工具",
                    description = "协助规划 Android 日历日程、系统闹钟提醒、Shell 脚本命令、正则表达式及手机工作流。",
                    iconName = "settings_suggest",
                    category = "automation",
                    systemPrompt = "你是 Nexus 自动化引擎，协助用户进行日常自动化规划、Android Intent 调度指引、Shell 脚本、正则表达式生成以及免 Root 任务流编排。请提供易于复制和执行的步骤。",
                    samplePromptsJson = "[\"写一个提取文本中所有手机号和邮箱的正则表达式\", \"提供一个批量将图片压缩并转为 WebP 的 Bash 脚本\", \"帮我规划今天下午 3 点的项目复盘会议日历日程\"]",
                    isBuiltIn = true
                ),
                AgentSkill(
                    id = "codex_dev",
                    name = "Codex 编程开发",
                    description = "Codex 级编程助理：精通 Jetpack Compose、Kotlin 协程、Python、SQL 与算法解析。",
                    iconName = "code",
                    category = "coding",
                    systemPrompt = "你是 Nexus Codex 编程导师，精通现代 Android 开发（Jetpack Compose、Kotlin Coroutines/Flow、Room 数据库、NDK/JNI）、Python 与后端开发。请输出模块化、可直接运行的高质量代码，并附带中文核心注释。",
                    samplePromptsJson = "[\"编写一个 Jetpack Compose 带呼吸光效的动态科技感按钮\", \"实现一个 Kotlin Flow 防抖（debounce）搜索框查询逻辑\", \"编写一个 Room 数据库多表关联与事务处理示例\"]",
                    isBuiltIn = true
                )
            )
            skillDao.insertSkills(defaultSkills)
        }

        suspend fun populateDefaultMemories(memoryDao: MemoryDao) {
            val initialMemories = listOf(
                AgentMemory(
                    key = "设备规格",
                    content = "当前设备：高性能 Android 智能手机（如 Poco F5 Pro / 骁龙平台），注重响应速度与能效平衡。",
                    category = "device"
                ),
                AgentMemory(
                    key = "语言与风格偏好",
                    content = "偏好使用规范流畅的华文（中文）进行交流，回答要求条理分明、直击要点，必要时附带代码或表格。",
                    category = "preference"
                )
            )
            initialMemories.forEach { memoryDao.insertMemory(it) }
        }
    }
}
