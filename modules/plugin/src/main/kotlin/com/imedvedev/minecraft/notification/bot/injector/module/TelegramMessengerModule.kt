package com.imedvedev.minecraft.notification.bot.injector.module

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import com.imedvedev.minecraft.notification.bot.messenger.telegram.TelegramMessenger
import com.pengrad.telegrambot.TelegramBot
import okhttp3.OkHttpClient
import org.bukkit.plugin.Plugin
import org.koin.dsl.module
import java.net.InetSocketAddress
import java.net.Proxy

val telegramMessengerModule = module(true) {
    single<Messenger> {
        val plugin = get<Plugin>()
        plugin.config.getConfigurationSection("messenger.telegram")?.let { config ->
            val token = config.getString("token") ?: return@let null
            TelegramMessenger(
                bot = config.getConfigurationSection("proxy")?.takeIf { proxyConfig ->
                    "enabled".takeIf(proxyConfig::isBoolean)?.let(proxyConfig::getBoolean) != false
                }?.let { proxyConfig ->
                    val proxy = Proxy(
                        Proxy.Type.SOCKS, InetSocketAddress(
                            proxyConfig.getString("host") ?: throw IllegalStateException(
                                "Please specify telegram proxy host in the configuration file"
                            ),
                            proxyConfig.getInt("port")
                        )
                    )
                    TelegramBot.Builder(token)
                        .okHttpClient(OkHttpClient.Builder()
                            .proxy(proxy)
                            .build())
                        .build()
                } ?: TelegramBot(token),
                chats = config.getStringList("chats"),
                logger = plugin.logger
            )
        } ?: throw IllegalStateException("Please specify your telegram bot token in the configuration file")
    }
}
