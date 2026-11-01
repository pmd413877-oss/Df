package com.example

import com.example.data.local.entity.ConversationEntity
import com.example.data.network.GeminiClient
import com.example.data.preferences.AVAILABLE_LANGUAGES
import com.example.data.preferences.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDefaultUserPreferences() {
        val prefs = UserPreferences()
        assertFalse(prefs.hasCompletedWelcome)
        assertFalse(prefs.hasCompletedOnboarding)
        assertEquals("bn", prefs.selectedLanguage)
        assertEquals(1.0f, prefs.speechRate, 0.01f)
        assertEquals(1.0f, prefs.speechPitch, 0.01f)
        assertTrue(prefs.autoSpeak)
    }

    @Test
    fun testSupportedLanguagesContainBengaliAndEnglish() {
        val codes = AVAILABLE_LANGUAGES.map { it.code }
        assertTrue(codes.contains("bn"))
        assertTrue(codes.contains("en"))
        assertTrue(codes.contains("hi"))
    }

    @Test
    fun testConversationEntityCreation() {
        val entity = ConversationEntity(
            userQuestion = "Hello Zara",
            aiResponse = "Hi! How can I help you?",
            language = "en",
            isVoice = true
        )
        assertEquals("Hello Zara", entity.userQuestion)
        assertEquals("Hi! How can I help you?", entity.aiResponse)
        assertTrue(entity.isVoice)
    }

    @Test
    fun testGeminiClientResolveCustomApiKey() {
        val client = GeminiClient()
        val customKey = "test_custom_api_key_12345"
        val resolved = client.resolveApiKey(customKey)
        assertEquals(customKey, resolved)
    }

    @Test
    fun testCallAssistantWorkflow() {
        val callManager = com.example.automation.CallAssistantManager()
        assertEquals(com.example.automation.CallState.IDLE, callManager.callInfo.value.callState)

        callManager.triggerSimulatedCall(callerName = "রফিক আহমেদ")
        assertEquals(com.example.automation.CallState.INCOMING_RINGING, callManager.callInfo.value.callState)
        assertEquals("রফিক আহমেদ", callManager.callInfo.value.callerName)

        val turns = callManager.answerCallWithZara()
        assertEquals(com.example.automation.CallState.ZARA_ANSWERED, callManager.callInfo.value.callState)
        assertTrue(turns.isNotEmpty())
        assertEquals("Zara", turns.first().speaker)

        callManager.endCall()
        assertEquals(com.example.automation.CallState.CALL_ENDED, callManager.callInfo.value.callState)
    }

    @Test
    fun testMessageAssistantWorkflow() {
        val messageManager = com.example.automation.MessageAssistantManager()
        val initialCount = messageManager.messages.value.size
        assertTrue(initialCount >= 3)

        messageManager.addIncomingMessage(
            sender = "অফিস",
            content = "জরুরি মিটিং"
        )
        assertEquals(initialCount + 1, messageManager.messages.value.size)
        assertEquals("অফিস", messageManager.messages.value.first().sender)
    }
}
