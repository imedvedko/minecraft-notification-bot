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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger


class NotificationEventListener(private val joinedMessage: String,
                                private val leftMessage: String,
                                private val onlineMessage: String,
                                private val messenger: Messenger,
                                private val onlinePlayers: () -> Iterable<Player>,
                                private val authMeApi: AuthMeApi,
                                private val logger: Logger,
                                private val quarantineScheduler: (() -> Unit) -> Unit) : Listener {
    private val quarantine: MutableMap<String, UUID> = ConcurrentHashMap()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(event: PlayerJoinEvent) {
        event.player.takeIf(authMeApi::isAuthenticated)?.joinNotification()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        event.player.takeIf(authMeApi::isAuthenticated)?.leftNotification()
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
            notification(joinedMessage, onlinePlayers().filter(authMeApi::isAuthenticated).map(Player::getName))
        }
    }

    private fun Player.leftNotification() {
        val quarantineId = UUID.randomUUID()
        quarantine[name] = quarantineId
        onlinePlayers().filter { this != it }.filter(authMeApi::isAuthenticated).map(Player::getName).let {
            quarantineScheduler.invoke {
                if (quarantine.remove(name, quarantineId)) {
                    notification(leftMessage, it)
                }
            }
        }
    }

    private fun Player.notification(message: String, onlinePlayers: Iterable<String>) = messenger
        .send("<b>$name $message</b>\n$onlineMessage: ${onlinePlayers.map { "<i>$it</i>" }}")
        .forEach { job -> job.exceptionally { e -> logger.log(Level.WARNING, "Can not send the message", e) } }
}
