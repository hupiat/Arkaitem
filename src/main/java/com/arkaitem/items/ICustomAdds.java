package com.arkaitem.items;

import com.arkaitem.Program;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public interface ICustomAdds {
    String CANT_DROP = "CANT_DROP";
    String TREE_FELLER = "TREE_FELLER";
    String NO_DISCARD = "NO_DISCARD";
    String KEEP_ON_DEATH = "KEEP_ON_DEATH";
    String UNBREAKABLE = "UNBREAKABLE";
    String NO_FALL_DAMAGE = "NO_FALL_DAMAGE";
    String GIVE_POTION = "GIVE_POTION";
    String KILL_GIVE_POTION = "KILL_GIVE_POTION";
    String DEATH_CHANCE_TP = "DEATH_CHANCE_TP";
    String TELEPORT_ON_ATTACK = "TELEPORT_ON_ATTACK";
    String HIT_EFFECT = "HIT_EFFECT";
    String SELF_EFFECT = "SELF_EFFECT";
    String SPAWN_LIGHTNING = "SPAWN_LIGHTNING";
    String SPAWN_HEAD_ON_KILL = "SPAWN_HEAD_ON_KILL";
    String STEAL_LIFE = "STEAL_LIFE";
    String STEAL_MONEY = "STEAL_MONEY";
    String VIEW_ON_CHEST = "VIEW_ON_CHEST";
    String MINE_AREA = "MINE_AREA";
    String BLOCK_COLUMN = "BLOCK_COLUMN";
    String SELL_CHEST_CONTENTS = "SELL_CHEST_CONTENTS";
    String CONSUMABLE = "CONSUMABLE";
    String CONSUMABLE_GIVE_POTION = "CONSUMABLE_GIVE_POTION";
    String CONSUMABLE_USE_COMMAND = "CONSUMABLE_USE_COMMAND";
    String EXECUTE_COMMAND_ON_KILL = "EXECUTE_COMMAND_ON_KILL";
    String HIDE_PLAYER_NAME = "HIDE_PLAYER_NAME";
    String MULTIPLICATEUR = "MULTIPLICATEUR";

    default Set<String> getAllCustomAdds() {
        Set<String> customAdds = new HashSet<>();
        Field[] fields = ICustomAdds.class.getDeclaredFields();

        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                try {
                    customAdds.add((String) field.get(null));
                } catch (IllegalAccessException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "Unable to access field " + field.getName(), e);
                }
            }
        }

        return customAdds;
    }

    default boolean hasCustomAdd(ItemStack item, String tag) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(item);

        if (!customItem.isPresent()) {
            throw new IllegalArgumentException("Custom cannot be found");
        }

        for (String line : customItem.get().getItem().getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).contains(tag)) {
                return true;
            }
        }
        return false;
    }

    default String getCustomAddData(ItemStack item, String tag) {
        if (item == null) {
            throw new IllegalArgumentException("item is null");
        }

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(item);

        if (!customItem.isPresent()) {
            throw new IllegalArgumentException("custom item not found");
        }

        ItemMeta meta = customItem.get().getItem().getItemMeta();
        List<String> lore = meta.getLore();
        for (String line : lore) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.contains(tag + ";")) {
                return strippedLine.substring(strippedLine.indexOf(tag + ";") + tag.length() + 1).trim();
            }
        }
        throw new IllegalArgumentException("No such tag: " + tag);
    }
}
