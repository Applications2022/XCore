package de.ruben.xcore.customenchantment.model;

import org.bukkit.event.Event;

public interface CustomEnchantmentEventReaction<T extends Event> {
    public abstract void handleEvent(T event, int enchantmentLevel);
    public abstract boolean canHandle(T event);
}
