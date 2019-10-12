package com.imedvedev.minecraft.notification.bot.messenger

import kotlinx.coroutines.Job

interface Messenger {
    fun send(message: String): List<Job>
}
