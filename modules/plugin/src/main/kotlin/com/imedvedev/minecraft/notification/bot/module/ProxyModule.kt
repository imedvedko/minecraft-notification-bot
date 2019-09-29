package com.imedvedev.minecraft.notification.bot.module

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import org.bukkit.configuration.ConfigurationSection

import java.net.InetSocketAddress
import java.net.Proxy

class ProxyModule(private val config: ConfigurationSection) : AbstractModule() {
    @Provides
    @Singleton
    fun proxy(): Proxy = Proxy(Proxy.Type.SOCKS, InetSocketAddress(
        config.getString("host")!!,
        config.getInt("port")
    ))
}
