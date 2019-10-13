package com.imedvedev.minecraft.notification.bot.listener

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import fr.xephi.authme.api.v3.AuthMeApi
import fr.xephi.authme.events.LoginEvent
import fr.xephi.authme.events.LogoutEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger

class NotificationEventListener(private val joinedMessage: String,
                                private val leftMessage: String,
                                private val messenger: Messenger,
                                private val onlinePlayers: () -> Iterable<Player>,
                                private val logger: Logger,
                                private val quarantineDelayMillis: Long) : Listener {
    private val quarantine: MutableMap<String, UUID> = ConcurrentHashMap()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        event.player.takeIf(AuthMeApi.getInstance()::isAuthenticated)?.joinNotification()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        event.player.takeIf(AuthMeApi.getInstance()::isAuthenticated)?.leftNotification()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogin(event: LoginEvent) {
        event.player.takeIf(Player::isOnline)?.joinNotification()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogout(event: LogoutEvent) {
        event.player.takeIf(Player::isOnline)?.leftNotification()
    }

    private fun Player.joinNotification() {
        if (quarantine.remove(name) == null) {
            notification(joinedMessage, onlinePlayers())
        }
    }

    private fun Player.leftNotification() {
        val quarantineId = UUID.randomUUID()
        quarantine[name] = quarantineId
        GlobalScope.launch {
            delay(quarantineDelayMillis)
            if (quarantine.remove(name, quarantineId)) {
                notification(leftMessage, onlinePlayers())
            }
        }
    }

    private fun Player.notification(message: String, onlinePlayers: Iterable<Player>) = onlinePlayers
        .filter(AuthMeApi.getInstance()::isAuthenticated).map { "<i>${it.name}</i>" }
        .let { messenger.send("<b>$name $message</b>\nOnline players: $it") }.forEach { job ->
            job.invokeOnCompletion { e -> e?.let { logger.log(Level.WARNING, "Can not send message", it) } }
        }
}
