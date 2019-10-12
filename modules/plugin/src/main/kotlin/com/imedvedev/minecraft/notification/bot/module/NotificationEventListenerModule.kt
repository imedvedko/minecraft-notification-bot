package com.imedvedev.minecraft.notification.bot.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.imedvedev.minecraft.notification.bot.listener.NotificationEventListener
import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import fr.xephi.authme.api.v3.AuthMeApi
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.logging.Logger

class NotificationEventListenerModule(private val config: ConfigurationSection,
                                      private val onlinePlayers: () -> Iterable<Player>,
                                      private val logger: Logger) : AbstractModule() {
    @Provides
    @Singleton
    fun notificationEventListener(messenger: Messenger): Listener = NotificationEventListener(
        AuthMeApi.getInstance(),
        config.getString("joined")!!,
        config.getString("left")!!,
        messenger,
        onlinePlayers,
        logger
    )
}
