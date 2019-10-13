package com.imedvedev.minecraft.notification.bot.injector.module

import com.imedvedev.minecraft.notification.bot.listener.NotificationEventListener
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.dsl.module

val notificationEventListenerModule = module(true) {
    single<Listener> {
        get<Plugin>().let { plugin -> plugin.config.getConfigurationSection("message")!!.let { config ->
            NotificationEventListener(
                config.getString("joined")!!,
                config.getString("left")!!,
                get(),
                plugin.server::getOnlinePlayers,
                plugin.logger
            )
        } }
    }
}
