package com.arkaitem.items;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public interface IItemPlaceholders {
    String kills = "kills";
    String damage_done = "damage_done";
    String last_kill = "last_kill";
    String arrows_shot = "arrows_shot";
    String last_enemy_hit = "last_enemy_hit";
    String blocks_travelled = "blocks_travelled";
    String blocks_mined = "blocks_mined";
    String hits_taken = "hits_taken";
    String item_owner = "item_owner";
    String mobs_killed = "mobs_killed";
    String trees_chopped = "trees_chopped";
    String electricado = "electricado";
    String power_retire = "power_retiré";
    String immortalite_cd = "imortalité_cd";
    String uses = "uses";
    String maxuses = "maxuses";
    String shop_multiplicateur = "shop_multiplicateur";

    default Set<String> getAllItemPlaceholders() {
        Set<String> placeholders = new HashSet<>();
        Field[] fields = IItemPlaceholders.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                try {
                    placeholders.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Unable to access field " + field.getName(), e);
                }
            }
        }

        return placeholders;
    }
}
