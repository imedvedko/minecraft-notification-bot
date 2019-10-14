package com.imedvedev.minecraft.notification.bot.injector.module

import com.imedvedev.minecraft.notification.bot.listener.NotificationEventListener
import fr.xephi.authme.api.v3.AuthMeApi
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.dsl.module

val notificationEventListenerModule = module(true) {
    single<Listener> {
        get<Plugin>().let { plugin -> plugin.config.getConfigurationSection("message")!!.let { config ->
            NotificationEventListener(
                config.getString("joined")!!,
                config.getString("left")!!,
                config.getString("online")!!,
                get(),
                plugin.server::getOnlinePlayers,
                AuthMeApi.getInstance(),
                plugin.logger,
                config.getLong("leftMessageQuarantineInTicks").let { delay ->
                    { task: () -> Unit -> plugin.server.scheduler.runTaskLater(plugin, task, delay).let { Unit } }
                }
            )
        } }
    }
}
