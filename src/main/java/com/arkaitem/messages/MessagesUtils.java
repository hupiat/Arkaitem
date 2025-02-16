package com.arkaitem.messages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class MessagesUtils {

    public static void sendToAll(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
