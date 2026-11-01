package com.example.automation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CallState {
    IDLE,
    INCOMING_RINGING,
    ZARA_ANSWERED,
    USER_TALKING,
    CALL_ENDED
}

data class CallConversationTurn(
    val speaker: String, // "Zara", "Caller", "User"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class IncomingCallInfo(
    val callerName: String = "অফিস ম্যানেজার (রাহুল)",
    val callerNumber: String = "+880 1712-345678",
    val callerReason: String = "আজকের প্রজেক্ট মিটিংয়ের আপডেট জানা দরকার",
    val callState: CallState = CallState.IDLE,
    val conversation: List<CallConversationTurn> = emptyList(),
    val callSummary: String? = null
)

class CallAssistantManager {

    private val _callInfo = MutableStateFlow(IncomingCallInfo())
    val callInfo: StateFlow<IncomingCallInfo> = _callInfo.asStateFlow()

    fun triggerSimulatedCall(
        callerName: String = "অফিস ম্যানেজার (রাহুল)",
        callerNumber: String = "+880 1712-345678",
        reason: String = "আজকের বিকেল ৫টার প্রজেক্ট রিপোর্ট প্রস্তুত কি না জানতে চেয়েছেন।"
    ) {
        _callInfo.value = IncomingCallInfo(
            callerName = callerName,
            callerNumber = callerNumber,
            callerReason = reason,
            callState = CallState.INCOMING_RINGING,
            conversation = emptyList(),
            callSummary = null
        )
    }

    fun answerCallWithZara(): List<CallConversationTurn> {
        val initialTurns = listOf(
            CallConversationTurn(
                speaker = "Zara",
                text = "নমস্কার! আমি ইউজারের ব্যক্তিগত এআই সহকারী জারা বলছি। ইউজার বর্তমানে গুরুত্বপূর্ণ কাজে ব্যস্ত আছেন। আপনি কি কোনো জরুরি বার্তা দিতে চান?"
            ),
            CallConversationTurn(
                speaker = "Caller",
                text = _callInfo.value.callerReason
            ),
            CallConversationTurn(
                speaker = "Zara",
                text = "ধন্যবাদ। আমি বার্তাটি নোট করে নিয়েছি এবং ইউজারকে এখনই অবহিত করছি। ভালো থাকবেন।"
            )
        )

        val summary = "কলকারীর বার্তা: '${_callInfo.value.callerReason}'. জারা কলটি রিসিভ করে বার্তা সংরক্ষণ করেছে।"

        _callInfo.value = _callInfo.value.copy(
            callState = CallState.ZARA_ANSWERED,
            conversation = initialTurns,
            callSummary = summary
        )

        return initialTurns
    }

    fun endCall() {
        _callInfo.value = _callInfo.value.copy(callState = CallState.CALL_ENDED)
    }

    fun reset() {
        _callInfo.value = IncomingCallInfo(callState = CallState.IDLE)
    }
}
