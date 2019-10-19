package com.imedvedev.minecraft.notification.bot

import com.imedvedev.minecraft.notification.bot.injector.module.notificationEventListenerModule
import com.imedvedev.minecraft.notification.bot.injector.module.proxyModule
import com.imedvedev.minecraft.notification.bot.injector.module.telegramMessengerModule
import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.startKoin
import org.koin.dsl.module

class NotificationBotPlugin : JavaPlugin() {
    override fun onEnable() {
        val koin = startKoin { modules(listOf(
            module(true) { single<Plugin> { this@NotificationBotPlugin } },
            proxyModule,
            telegramMessengerModule,
            notificationEventListenerModule
        )) }.koin

        config.getConfigurationSection("message")!!.getString("started").let {
            koin.get<Messenger>().send("<b>$it</b>\n${description.name}: ${description.version}")
        }

        server.pluginManager.registerEvents(koin.get(), this)
    }
}
