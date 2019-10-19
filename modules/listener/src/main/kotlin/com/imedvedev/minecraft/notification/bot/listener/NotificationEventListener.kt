package com.imedvedev.minecraft.notification.bot.listener

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import fr.xephi.authme.events.LoginEvent
import fr.xephi.authme.events.LogoutEvent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.absoluteValue

class NotificationEventListener(private val joinedMessage: String,
                                private val leftMessage: String,
                                private val onlineMessage: String,
                                private val chatColors: List<ChatColor>,
                                private val messenger: Messenger,
                                private val onlinePlayers: () -> Iterable<Player>,
                                private val isAuthenticated: (Player) -> Boolean,
                                private val logger: Logger,
                                private val quarantineScheduler: (() -> Unit) -> Unit,
                                private val pluginName: String,
                                private val pluginVersion: String) : Listener {
    private val authenticatedPlayers: MutableMap<String, Int> = LinkedHashMap()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogin(event: LoginEvent) {
        event.player.takeIf(Player::isOnline)?.run {
            if (authenticatedPlayers.put(name, entityId) == null) {
                sendMessage("$pluginName $pluginVersion")
                logger.log(Level.INFO) { "$name joined the game. " +
                    "Server is sending notification" }
                sendNotification(joinedMessage)
            } else {
                logger.log(Level.INFO) { "$name joined the game when was quarantined. " +
                    "Server is not sending notification" }
                sendMessage(authenticatedPlayerNames())
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogout(event: LogoutEvent) {
        event.player.takeIf(Player::isOnline)?.run {
            authenticatedPlayers.remove(name)
            logger.log(Level.INFO) { "$name left the game. " +
                "Server is sending notification" }
            sendNotification(leftMessage)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        event.player.takeIf(isAuthenticated)?.run {
            logger.log(Level.INFO) { "$name has been quarantined" }
            quarantineScheduler.invoke {
                if (authenticatedPlayers.remove(name, entityId)) {
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

    private fun Player.sendNotification(message: String) {
        messenger.send("<b>$name $message</b>\n$onlineMessage: ${authenticatedPlayers.keys.map { "<i>$it</i>" }}")
            .forEach { job -> job.exceptionally { exception -> logger.log(Level.WARNING, exception) {
                "Server can not send the notification: \"$message\""
            } } }

        authenticatedPlayerNames().let { authenticatedPlayerNames ->
            onlinePlayers().filter(isAuthenticated)
                .forEach { it.sendMessage(authenticatedPlayerNames) }
        }
    }

    private fun authenticatedPlayerNames(): String = authenticatedPlayers.keys.map { name ->
        name.toByteArray().reduce { first: Byte, second: Byte -> (first + second).toByte() }.toInt().absoluteValue
            .let { "${chatColors[it % chatColors.size]}${ChatColor.stripColor(name)}${ChatColor.RESET}" }
    }.let { "${ChatColor.GREEN}${ChatColor.stripColor(onlineMessage)}: ${ChatColor.RESET}$it" }
}
