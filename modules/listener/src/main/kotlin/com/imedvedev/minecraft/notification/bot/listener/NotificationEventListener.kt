package com.imedvedev.minecraft.notification.bot.listener

import com.imedvedev.minecraft.notification.bot.messenger.Messenger
import fr.xephi.authme.events.LoginEvent
import fr.xephi.authme.events.LogoutEvent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.PluginDescriptionFile
import java.util.logging.Logger

class NotificationEventListener(private val joinedMessage: String,
                                private val leftMessage: String,
                                private val onlineMessage: String,
                                private val chatColors: List<ChatColor>,
                                private val messenger: Messenger,
                                private val logger: Logger,
                                private val quarantineScheduler: (() -> Unit) -> Unit,
                                private val pluginDescription: PluginDescriptionFile) : Listener {
    private val authenticatedPlayers: AuthenticatedPlayers = AuthenticatedPlayers()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogin(event: LoginEvent) {
        event.player.run {
            if (authenticatedPlayers.put(uniqueId, this) == null) {
                TextComponent("${pluginDescription.name} ${pluginDescription.version}")
                    .apply { clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, pluginDescription.website) }
                    .let { spigot().sendMessage(it) }
                logger.info { "$name joined the game. " +
                    "Server is sending notification" }
                sendNotification(joinedMessage)
            } else {
                logger.info { "$name joined the game when was quarantined. " +
                    "Server is not sending notification" }
                sendMessage(authenticatedPlayerNames())
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogout(event: LogoutEvent) {
        event.player.run {
            authenticatedPlayers.remove(uniqueId)
            logger.info { "$name left the game. " +
                "Server is sending notification" }
            sendNotification(leftMessage)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(event: PlayerQuitEvent) {
        event.player.takeIf { authenticatedPlayers.containsKey(it.uniqueId) }?.run {
            logger.info { "$name has been quarantined" }
            quarantineScheduler.invoke {
                if (authenticatedPlayers.remove(uniqueId, this)) {
                    logger.info { "Quarantine has been finished. $name left the game. " +
                        "Server is sending notification" }
                    sendNotification(leftMessage)
                } else {
                    logger.info { "Quarantine has been finished. $name already joined the game again. " +
                        "Server is not sending notification" }
                }
            }
        }
    }

    private fun Player.sendNotification(message: String) {
        authenticatedPlayers.toString { "<i>$name</i>" }.let {
            messenger.send("<b>$name $message</b>\n$onlineMessage: $it")
        }

        authenticatedPlayerNames().let { authenticatedPlayerNames ->
            authenticatedPlayers.values.forEach { it.sendMessage(authenticatedPlayerNames) }
        }
    }

    private fun authenticatedPlayerNames(): String = authenticatedPlayers.toString {
        "$chatColor${ChatColor.stripColor(name)}${ChatColor.RESET}"
    }.let { "${ChatColor.GREEN}${ChatColor.stripColor(onlineMessage)}: ${ChatColor.RESET}$it" }

    private val Player.chatColor: ChatColor get() = chatColors[uniqueId.leastSignificantBits.toInt() % chatColors.size]
}
