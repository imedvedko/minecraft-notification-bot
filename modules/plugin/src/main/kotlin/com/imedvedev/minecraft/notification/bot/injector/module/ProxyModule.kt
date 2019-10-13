package com.imedvedev.minecraft.notification.bot.injector.module

import org.bukkit.plugin.Plugin
import org.koin.dsl.module
import java.net.InetSocketAddress
import java.net.Proxy

val proxyModule = module(true) {
    single {
        get<Plugin>().config.getConfigurationSection("proxy")!!.let { config ->
            Proxy(Proxy.Type.SOCKS, InetSocketAddress(
                config.getString("host")!!,
                config.getInt("port")
            ))
        }
    }
}
