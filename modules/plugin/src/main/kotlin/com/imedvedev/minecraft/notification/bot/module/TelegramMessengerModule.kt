package com.imedvedev.minecraft.notification.bot.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import com.imedvedev.minecraft.notification.bot.messenger.telegram.TelegramMessenger
import com.pengrad.telegrambot.TelegramBot
import okhttp3.OkHttpClient
import org.bukkit.configuration.ConfigurationSection

import java.net.Proxy

class TelegramMessengerModule(private val config: ConfigurationSection) : AbstractModule() {

    @Provides
    @Singleton
    fun telegramBot(proxy: Proxy): TelegramBot = TelegramBot.Builder(config.getString("token"))
        .okHttpClient(OkHttpClient.Builder()
            .proxy(proxy)
            .build())
        .build()

    @Provides
    @Singleton
    fun telegramMessenger(telegramBot: TelegramBot): Messenger = TelegramMessenger(
        telegramBot, config.getStringList("chats")
    )
}
