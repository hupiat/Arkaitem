package com.arkaitem.items;

import com.arkaitem.Program;
import com.arkaitem.utils.ItemsUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public interface ICustomAdds {
    String GIVE_POTION = "GIVE_POTION";
    String CANT_DROP = "CANT_DROP";
    String TREE_FELLER = "TREE_FELLER";
    String NO_DISCARD = "NO_DISCARD";
    String KEEP_ON_DEATH = "KEEP_ON_DEATH";
    String UNBREAKABLE = "UNBREAKABLE";
    String NO_FALL_DAMAGE = "NO_FALL_DAMAGE";
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
    String KILLER_COMMAND = "KILLER_COMMAND";
    String HIDE_PLAYER_NAME = "HIDE_PLAYER_NAME";
    String MULTIPLICATEUR = "MULTIPLICATEUR";
    String LOSS_POWER = "LOSS_POWER";

    String EFFECT_DIVINE_GLOW = "EFFECT_DIVINE_GLOW";
    String EFFECT_CUPIDON = "EFFECT_CUPIDON";
    String EFFECT_GHOST = "EFFECT_GHOST";
    String EFFECT_BLOOD_EXPLOSION = "EFFECT_BLOOD_EXPLOSION";
    String EFFECT_LIGHT_COLUMN = "EFFECT_LIGHT_COLUMN";
    String EFFECT_DAMNED = "EFFECT_DAMNED";
    String EFFECT_SHOOTING_STARS = "EFFECT_SHOOTING_STARS";
    String EFFECT_SMOKE = "EFFECT_SMOKE";

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

    default boolean hasCustomAdd(ItemStack item, String tag, @Nullable Player player) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(item);

        if (!customItem.isPresent()) {
            throw new IllegalArgumentException("Custom item cannot be found");
        }

        for (String line : customItem.get().getCustomAdds()) {
            if (line.toUpperCase().contains(tag)) {
                return true;
            }
        }

        if (player != null) {
            if (hasFullSetRequirements(customItem.get(), player)) {
                for (String line : customItem.get().getFullSetCustomAdds()) {
                    if (line.toUpperCase().contains(tag)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    default String getCustomAddData(ItemStack item, String tag, @Nullable Player player) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        Optional<CustomItem> customItem = Program.INSTANCE.ITEMS_MANAGER.getItemByItemStack(item);

        if (!customItem.isPresent()) {
            throw new IllegalArgumentException("Custom item cannot be found");
        }

        for (String line : customItem.get().getCustomAdds()) {
            if (line.toUpperCase().contains(tag + ";")) {
                return line.toUpperCase().substring(line.toUpperCase().indexOf(tag + ";") + tag.length() + 1).trim();
            }
        }

        if (player != null) {
            if (hasFullSetRequirements(customItem.get(), player)) {
                for (String line : customItem.get().getFullSetCustomAdds()) {
                    if (line.toUpperCase().contains(tag + ";")) {
                        return line.toUpperCase().substring(line.indexOf(tag + ";") + tag.length() + 1).trim();
                    }
                }
            }
        }

        throw new IllegalArgumentException("No such tag: " + tag);
    }

    default boolean hasFullSetRequirements(CustomItem customItem, Player player) {
        for (ItemStack equipment : player.getInventory().getArmorContents()) {
            String id = ItemsUtils.getUniqueID(equipment);
            if (id == null) {
                return false;
            }
            if (!customItem.getFullSetRequirements().contains(id)) {
                return false;
            }
        }
        return true;
    }
}
