package com.arkaitem.messages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public abstract class MessagesUtils {

    public static void sendToAll(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }
}
