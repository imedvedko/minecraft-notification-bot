package com.imedvedev.minecraft.notification.bot

import com.imedvedev.minecraft.notification.bot.injector.get
import org.bukkit.plugin.java.JavaPlugin

class NotificationBotPlugin : JavaPlugin() {
    override fun onEnable() = server.pluginManager.registerEvents(get(this), this)
}
