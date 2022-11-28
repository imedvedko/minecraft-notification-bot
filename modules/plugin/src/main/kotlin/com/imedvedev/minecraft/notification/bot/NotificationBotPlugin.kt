package com.imedvedev.minecraft.notification.bot

import com.imedvedev.minecraft.notification.bot.injector.module.notificationEventListenerModule
import com.imedvedev.minecraft.notification.bot.injector.module.telegramMessengerModule
import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.startKoin
import org.koin.dsl.module

class NotificationBotPlugin : JavaPlugin() {
    override fun onLoad() {
        logger.info { "${description.name} ${description.version} has been loaded" }
    }

    override fun onEnable() {
        saveDefaultConfig();

        val koin = startKoin { modules(
            module(true) { single<Plugin> { this@NotificationBotPlugin } },
            telegramMessengerModule,
            notificationEventListenerModule
        ) }.koin

        config.getConfigurationSection("message")!!.getString("started")?.let {
            koin.get<Messenger>().send("<b>$it</b>" +
                "\n<a href=\"${description.website}\">${description.name} ${description.version}</a>")
        }

        server.pluginManager.registerEvents(koin.get(), this)

        logger.info { "${description.name} ${description.version} has been enabled" }
    }

    override fun onDisable() {
        logger.info { "${description.name} ${description.version} has been disabled" }
    }
}
