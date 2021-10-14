package de.ruben.xcore.customenchantment.command;

import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
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

        item.addEnchantment(CustomEnchantment.MINER, 1, player.getInventory().getHeldItemSlot(), player.getInventory());
        item.addEnchantment(CustomEnchantment.TELEKINESIS, 1, player.getInventory().getHeldItemSlot(), player.getInventory());

        player.sendMessage("Sucess!");
        return false;
    }
}
