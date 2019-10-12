package com.imedvedev.minecraft.notification.bot.messenger.telegram

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import kotlinx.coroutines.Job
import java.io.IOException

class TelegramMessenger(private val bot: TelegramBot, private val chats: Iterable<String>) : Messenger {

    override fun send(message: String): List<Job> = chats.map { chat ->
        Job().apply {
            object : Callback<SendMessage, SendResponse> {
                override fun onResponse(request: SendMessage, response: SendResponse) {
                    if (response.isOk) {
                        complete()
                    } else {
                        completeExceptionally(TelegramMessengerException(response.errorCode(), response.description()))
                    }
                }

                override fun onFailure(request: SendMessage, exception: IOException) {
                    completeExceptionally(exception)
                }
            }.let { bot.execute(SendMessage(chat, message).apply { parseMode(ParseMode.HTML) }, it) }
        }
    }
}
