package com.imedvedev.minecraft.notification.bot.messenger.telegram

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.suspendCoroutine

class TelegramMessenger(
    private val bot: TelegramBot,
    private val chats: Iterable<String>,
    private val logger: Logger
) : Messenger {
    override fun send(message: String) {
        try {
            runBlocking {
                chats.forEach { chat ->
                    val telegramMessage = SendMessage(chat, message)
                        .parseMode(ParseMode.HTML)
                        .disableWebPagePreview(true)

                    launch {
                        suspendCoroutine { continuation ->
                            bot.execute(telegramMessage, TelegramCallback(continuation))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, e) { "Server can not send the notification: \"$message\"" }
        }
    }
}
