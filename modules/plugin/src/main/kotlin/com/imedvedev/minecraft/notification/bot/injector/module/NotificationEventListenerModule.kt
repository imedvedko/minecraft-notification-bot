package com.imedvedev.minecraft.notification.bot.injector.module

import com.imedvedev.minecraft.notification.bot.listener.NotificationEventListener
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.dsl.module

val notificationEventListenerModule = module(true) {
    single<Listener> {
        val plugin = get<Plugin>()
        val config = plugin.config
        val messageConfig = config.getConfigurationSection("message")!!
        NotificationEventListener(
            joinedMessage = messageConfig.getString("joined")!!,
            leftMessage = messageConfig.getString("left")!!,
            onlineMessage = messageConfig.getString("online")!!,
            chatColors = config.getStringList("name-colors").map(ChatColor::valueOf),
            messenger = get(),
            logger = plugin.logger,
            quarantineScheduler = messageConfig.getLong("left-message-quarantine-in-ticks")
                .let { delay -> { task -> plugin.server.scheduler.runTaskLater(plugin, task, delay) } },
            pluginDescription = plugin.description
        )
    }
}
