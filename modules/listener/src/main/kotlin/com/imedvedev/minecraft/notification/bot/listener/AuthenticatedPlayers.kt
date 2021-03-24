package com.imedvedev.minecraft.notification.bot.listener

import org.bukkit.entity.Player
import java.util.UUID

class AuthenticatedPlayers : MutableMap<UUID, Player> by LinkedHashMap() {
    fun toString(transform: (Player.() -> String)): String {
        return values.joinToString(prefix = "[", postfix = "]", transform = transform)
    }
}
