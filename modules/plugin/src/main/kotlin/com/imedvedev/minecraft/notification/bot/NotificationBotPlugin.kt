package com.imedvedev.minecraft.notification.bot

import com.google.inject.Guice
import com.google.inject.Stage
import com.imedvedev.minecraft.notification.bot.module.MessengerListenerModule
import com.imedvedev.minecraft.notification.bot.module.ProxyModule
import com.imedvedev.minecraft.notification.bot.module.TelegramMessengerModule
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class NotificationBotPlugin : JavaPlugin() {
    override fun onEnable() = saveDefaultConfig().also {
        config.getConfigurationSection("messenger")!!
            .let { messengerConfig -> Guice.createInjector(Stage.PRODUCTION,
                ProxyModule(config.getConfigurationSection("proxy")!!),
                TelegramMessengerModule(messengerConfig.getConfigurationSection("telegram")!!),
                MessengerListenerModule { server.onlinePlayers.map { it.name } }) }
            .getInstance(Listener::class.java).let { server.pluginManager.registerEvents(it, this) }
    }
}
