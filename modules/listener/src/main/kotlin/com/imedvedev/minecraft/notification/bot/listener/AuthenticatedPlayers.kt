package com.imedvedev.minecraft.notification.bot.listener

import org.bukkit.entity.Player
import java.util.UUID

typealias AuthenticatedPlayers = MutableMap<UUID, Player>

fun AuthenticatedPlayers.toString(transform: (Player.() -> String)): String {
    return values.joinToString(prefix = "[", postfix = "]", transform = transform)
}

