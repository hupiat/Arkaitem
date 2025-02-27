package com.arkaitem.utils;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

public abstract class PotionUtils {

    @Nullable
    public static PotionEffect getByPotionEffectType(PotionEffectType effectType, Collection<PotionEffect> effects, Function<PotionEffect, Boolean> supplier) {
        for (PotionEffect potionEffect : effects) {
            if (potionEffect.getType().equals(effectType) && supplier.apply(potionEffect))
                return potionEffect;
        }
        return null;
    }
}
