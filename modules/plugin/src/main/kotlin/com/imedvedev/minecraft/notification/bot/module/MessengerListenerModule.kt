package com.imedvedev.minecraft.notification.bot.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.imedvedev.minecraft.notification.bot.listener.MessengerEventListener
import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import org.bukkit.event.Listener

class MessengerListenerModule(private val onlinePlayers: () -> List<String>) : AbstractModule() {
    @Provides
    @Singleton
    fun messengerEventListener(messenger: Messenger): Listener = MessengerEventListener(
        listOf(messenger), onlinePlayers
    )
}
