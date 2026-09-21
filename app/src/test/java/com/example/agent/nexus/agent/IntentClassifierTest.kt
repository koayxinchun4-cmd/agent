package com.example.agent.nexus.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class IntentClassifierTest {
    private val classifier = IntentClassifier()

    @Test
    fun `classifies app launch intent`() {
        assertEquals(AgentIntent.App, classifier.classify("open app package:com.example.notes"))
    }

    @Test
    fun `classifies github intent`() {
        assertEquals(AgentIntent.GitHub, classifier.classify("check GitHub CI"))
    }

    @Test
    fun `classifies traditional chinese web intent`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("搜尋這個主題的資料"))
    }

    @Test
    fun `classifies simplified chinese web intent`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("搜索這個主題的資料"))
    }

    @Test
    fun `classifies traditional chinese file intent`() {
        assertEquals(AgentIntent.File, classifier.classify("讀取文件 note.txt"))
    }

    @Test
    fun `classifies traditional chinese memory intent`() {
        assertEquals(AgentIntent.Memory, classifier.classify("記住這個偏好"))
    }

    @Test
    fun `classifies general intent when no specialized signal exists`() {
        assertEquals(AgentIntent.General, classifier.classify("hello Nexus"))
    }

    @Test
    fun `classifies blank input as general`() {
        assertEquals(AgentIntent.General, classifier.classify("   "))
    }

    // Bahasa Melayu tests

    @Test
    fun `classifies malay web search intent with cari`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("cari maklumat tentang AI"))
    }

    @Test
    fun `classifies malay web search intent with jelajah`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("jelajah topik ini"))
    }

    @Test
    fun `classifies malay file intent with fail`() {
        assertEquals(AgentIntent.File, classifier.classify("buka fail laporan.pdf"))
    }

    @Test
    fun `classifies malay memory intent with ingat`() {
        assertEquals(AgentIntent.Memory, classifier.classify("ingat perkara ini"))
    }

    @Test
    fun `classifies malay app launch intent with buka app`() {
        assertEquals(AgentIntent.App, classifier.classify("buka app package:com.example.notes"))
    }

    @Test
    fun `classifies malay office intent with hamparan`() {
        assertEquals(AgentIntent.Office, classifier.classify("buat hamparan baru"))
    }

    @Test
    fun `classifies chinese office intent with report keyword`() {
        assertEquals(AgentIntent.Office, classifier.classify("整理報告"))
    }

    @Test
    fun `classifies english office intent with report keyword`() {
        assertEquals(AgentIntent.Office, classifier.classify("write a report"))
    }


    // === 新增：马来语 + 危险意图测试 (2025-09-20) ===

    @Test
    fun `classifies malay app intent with buat`() {
        assertEquals(AgentIntent.App, classifier.classify("buat app baru"))
    }

    @Test
    fun `classifies malay app intent with tengok app`() {
        assertEquals(AgentIntent.App, classifier.classify("tengok app ni"))
    }

    @Test
    fun `classifies malay app intent with guna app`() {
        assertEquals(AgentIntent.App, classifier.classify("guna app whatsapp"))
    }

    @Test
    fun `classifies malay web intent with check`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("check cuaca hari ini"))
    }

    @Test
    fun `classifies malay web intent with harga`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("check harga petrol"))
    }

    @Test
    fun `classifies malay web intent with berita`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("berita terkini pasal AI"))
    }

    @Test
    fun `classifies malay memory intent with simpan`() {
        assertEquals(AgentIntent.Memory, classifier.classify("simpan maklumat ni"))
    }

    @Test
    fun `classifies malay memory intent with save`() {
        assertEquals(AgentIntent.Memory, classifier.classify("save benda ni"))
    }

    @Test
    fun `classifies malay file intent with muat turun`() {
        assertEquals(AgentIntent.File, classifier.classify("muat turun fail ini"))
    }

    @Test
    fun `classifies malay file intent with muat naik`() {
        assertEquals(AgentIntent.File, classifier.classify("muat naik gambar"))
    }

    @Test
    fun `classifies malay office intent with borang`() {
        assertEquals(AgentIntent.Office, classifier.classify("buat borang google form"))
    }

    @Test
    fun `classifies dangerous intent with padam`() {
        assertEquals(AgentIntent.Dangerous, classifier.classify("padam fail penting"))
    }

    @Test
    fun `classifies dangerous intent with buang`() {
        assertEquals(AgentIntent.Dangerous, classifier.classify("buang app ni"))
    }

    @Test
    fun `classifies dangerous intent with format`() {
        assertEquals(AgentIntent.Dangerous, classifier.classify("format phone"))
    }

    @Test
    fun `classifies dangerous intent with hapus`() {
        assertEquals(AgentIntent.Dangerous, classifier.classify("hapus semua data"))
    }

    @Test
    fun `classifies code-switching intent buka app`() {
        assertEquals(AgentIntent.App, classifier.classify("saya nak buka app grab"))
    }

    @Test
    fun `classifies code-switching web intent`() {
        assertEquals(AgentIntent.WebResearch, classifier.classify("tolong cari harga iPhone"))
    }
}
