package com.imedvedev.minecraft.notification.bot.injector

import com.imedvedev.minecraft.notification.bot.injector.module.notificationEventListenerModule
import com.imedvedev.minecraft.notification.bot.injector.module.proxyModule
import com.imedvedev.minecraft.notification.bot.injector.module.telegramMessengerModule
import org.bukkit.plugin.Plugin
import org.koin.core.context.startKoin
import org.koin.dsl.module

inline fun <reified T> get(plugin: Plugin): T {
    val app = startKoin { modules(listOf(
        module(true) { single { plugin } },
        proxyModule,
        telegramMessengerModule,
        notificationEventListenerModule
    )) }

    val result = app.koin.get<T>()
    app.close()
    return result
}
