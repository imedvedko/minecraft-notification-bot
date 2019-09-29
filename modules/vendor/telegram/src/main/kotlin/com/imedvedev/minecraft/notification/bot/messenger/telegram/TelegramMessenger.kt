package com.imedvedev.minecraft.notification.bot.messenger.telegram

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage

class TelegramMessenger(private val bot: TelegramBot, private val chats: List<String>) : Messenger {

    override fun send(message: String, onlinePlayer: List<String>) = chats.forEach { send(it, message, onlinePlayer) }

    private fun send(chat: String, message: String, onlinePlayer: List<String>) = bot.execute(SendMessage(
        chat, "$message\n\nOnline players: $onlinePlayer"
    ))
}
