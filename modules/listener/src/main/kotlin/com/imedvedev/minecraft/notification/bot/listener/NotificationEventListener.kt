package com.imedvedev.minecraft.notification.bot.listener

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import fr.xephi.authme.api.v3.AuthMeApi
import fr.xephi.authme.events.LoginEvent
import fr.xephi.authme.events.LogoutEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.logging.Level
import java.util.logging.Logger

class NotificationEventListener(private val joinedMessage: String,
                                private val leftMessage: String,
                                private val messenger: Messenger,
                                private val onlinePlayers: () -> Iterable<Player>,
                                private val logger: Logger) : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        event.player.takeIf(AuthMeApi.getInstance()::isAuthenticated)
            ?.run { joinNotification() }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        event.player.takeIf(AuthMeApi.getInstance()::isAuthenticated)
            ?.run { leftNotification(onlinePlayers().filter { this != it }) }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogin(event: LoginEvent) = event.player.joinNotification()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogout(event: LogoutEvent) = event.player.leftNotification(onlinePlayers())

    private fun Player.joinNotification() {
        notification(joinedMessage, onlinePlayers())
    }

    private fun Player.leftNotification(onlinePlayers: Iterable<Player>) {
        notification(leftMessage, onlinePlayers)
    }

    private fun Player.notification(message: String, onlinePlayers: Iterable<Player>) = onlinePlayers
        .filter(AuthMeApi.getInstance()::isAuthenticated).map { "<i>${it.name}</i>" }
        .let { messenger.send("<b>$name $message</b>\nOnline players: $it").forEach { result ->
            result.invokeOnCompletion { e -> e?.let { logger.log(Level.WARNING, "Can not send message", it) } }
        } }
}
