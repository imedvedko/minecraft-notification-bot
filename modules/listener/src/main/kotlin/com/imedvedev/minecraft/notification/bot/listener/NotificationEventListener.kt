package com.imedvedev.minecraft.notification.bot.listener

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import fr.xephi.authme.api.v3.AuthMeApi
import fr.xephi.authme.events.LoginEvent
import fr.xephi.authme.events.LogoutEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.LinkedHashMap

class NotificationEventListener(private val joinedMessage: String,
                                private val leftMessage: String,
                                private val onlineMessage: String,
                                private val messenger: Messenger,
                                private val authMeApi: AuthMeApi,
                                private val logger: Logger,
                                private val quarantineScheduler: (() -> Unit) -> Unit) : Listener {
    private val onlinePlayers: MutableMap<String, Int> = LinkedHashMap()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogin(event: LoginEvent) {
        event.player.takeIf(Player::isOnline)?.run {
            if (onlinePlayers.put(name, entityId) == null) {
                logger.log(Level.INFO) { "$name joined the game. " +
                    "Server is sending notification" }
                sendNotification(joinedMessage)
            } else {
                logger.log(Level.INFO) { "$name joined the game when was quarantined. " +
                    "Server is not sending notification" }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogout(event: LogoutEvent) {
        event.player.takeIf(Player::isOnline)?.run {
            onlinePlayers.remove(name)
            logger.log(Level.INFO) { "$name left the game. " +
                "Server is sending notification" }
            sendNotification(leftMessage)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        event.player.takeIf(authMeApi::isAuthenticated)?.run {
            logger.log(Level.INFO) { "$name has been quarantined" }
            quarantineScheduler.invoke {
                if (onlinePlayers.remove(name, entityId)) {
                    logger.log(Level.INFO) { "Quarantine has been finished. $name left the game. " +
                            "Server is sending notification" }
                    sendNotification(leftMessage)
                } else {
                    logger.log(Level.INFO) { "Quarantine has been finished. $name already joined the game again. " +
                            "Server is not sending notification" }
                }
            }
        }
    }

    private fun Player.sendNotification(message: String) = messenger
        .send("<b>$name $message</b>\n$onlineMessage: ${onlinePlayers.keys.map { "<i>${it}</i>" }}")
        .forEach { job -> job.exceptionally { exception -> logger.log(Level.WARNING, exception) {
            "Server can not send the notification: \"$message\""
        } } }
}
