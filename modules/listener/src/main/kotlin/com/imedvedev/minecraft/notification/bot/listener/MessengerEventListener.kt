package com.imedvedev.minecraft.notification.bot.listener

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.regex.Pattern

class MessengerEventListener(private val messengers: List<Messenger>,
                             private val onlinePlayers: () -> List<String>) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) = event.joinMessage?.let { sendMessage(it, onlinePlayers.invoke()) }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) = event.quitMessage?.let {
        sendMessage(it, onlinePlayers.invoke().filterNot { event.player.name == it })
    }

    private fun sendMessage(message: String, onlinePlayers: List<String>) {
        SPECIAL_CHARACTER.matcher(message).replaceAll("")
            .let { escapedMessage ->  messengers.forEach { it.send(escapedMessage, onlinePlayers) } }
    }

    companion object {
        private val SPECIAL_CHARACTER = Pattern.compile("§.")
    }
}
