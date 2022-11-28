package com.imedvedev.minecraft.notification.bot.messenger.telegram

import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TelegramCallback(private val continuation: Continuation<Unit>) : Callback<SendMessage, SendResponse> {
    override fun onResponse(request: SendMessage, response: SendResponse) {
        if (response.isOk) {
            continuation.resume(Unit)
        } else {
            continuation.resumeWithException(TelegramMessengerException(response.errorCode(), response.description()))
        }
    }

    override fun onFailure(request: SendMessage, exception: IOException) {
        continuation.resumeWithException(exception)
    }
}
