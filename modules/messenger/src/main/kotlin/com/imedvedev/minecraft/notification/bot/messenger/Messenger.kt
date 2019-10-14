package com.imedvedev.minecraft.notification.bot.messenger

import java.util.concurrent.CompletableFuture

interface Messenger {
    fun send(message: String): List<CompletableFuture<Unit>>
}
