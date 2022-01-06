package de.ruben.xcore.customenchantment.command;

import de.ruben.xcore.XCore;
import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xcore.customenchantment.model.enchantment.MinerEnchant;
import de.ruben.xcore.customenchantment.model.enchantment.TelekinesisEnchant;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TestEnchantmentCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        Player player = (Player) commandSender;

        CustomEnchantedItem item = new CustomEnchantedItem(player.getInventory().getItemInMainHand());

        item.addEnchantment(CustomEnchantment.BLOCKTRACKER, 1, player.getInventory().getHeldItemSlot(), player.getInventory());
        item.addEnchantment(CustomEnchantment.TELEKINESIS, 1, player.getInventory().getHeldItemSlot(), player.getInventory());
        item.addEnchantment(CustomEnchantment.SMELT, 10, player.getInventory().getHeldItemSlot(), player.getInventory());
        item.addEnchantment(CustomEnchantment.ERFAHRUNG, 5, player.getInventory().getHeldItemSlot(), player.getInventory());
        item.addEnchantment(CustomEnchantment.LEBENSRAUB, 5, player.getInventory().getHeldItemSlot(), player.getInventory());
        item.addEnchantment(CustomEnchantment.SCHATTEN, 10, player.getInventory().getHeldItemSlot(), player.getInventory());

        return false;
    }
}
