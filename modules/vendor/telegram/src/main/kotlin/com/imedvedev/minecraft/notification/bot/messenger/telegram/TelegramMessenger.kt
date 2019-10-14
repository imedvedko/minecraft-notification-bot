package com.imedvedev.minecraft.notification.bot.messenger.telegram

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import com.pengrad.telegrambot.Callback
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import java.io.IOException
import java.util.concurrent.CompletableFuture

class TelegramMessenger(private val bot: TelegramBot, private val chats: Iterable<String>) : Messenger {

    override fun send(message: String): List<CompletableFuture<Unit>> = chats.map { chat ->
        CompletableFuture<Unit>().apply {
            object : Callback<SendMessage, SendResponse> {
                override fun onResponse(request: SendMessage, response: SendResponse) {
                    if (response.isOk) {
                        complete(Unit)
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
