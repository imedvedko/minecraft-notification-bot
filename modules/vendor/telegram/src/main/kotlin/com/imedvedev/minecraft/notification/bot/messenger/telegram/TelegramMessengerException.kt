package com.imedvedev.minecraft.notification.bot.messenger.telegram

class TelegramMessengerException(code: Int, description: String) : RuntimeException("$description ($code)")