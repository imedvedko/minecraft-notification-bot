package com.imedvedev.minecraft.notification.bot.injector.module

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import com.imedvedev.minecraft.notification.bot.messenger.telegram.TelegramMessenger
import com.pengrad.telegrambot.TelegramBot
import okhttp3.OkHttpClient
import org.bukkit.plugin.Plugin
import org.koin.dsl.module

val telegramMessengerModule = module(true) {
    single<Messenger> {
        get<Plugin>().config.getConfigurationSection("messenger")!!.getConfigurationSection("telegram")!!
            .let { config ->
                TelegramMessenger(
                    TelegramBot.Builder(config.getString("token"))
                        .okHttpClient(OkHttpClient.Builder()
                            .proxy(get())
                            .build())
                        .build(),
                    config.getStringList("chats")
                )
            }
    }
}
