package de.ruben.xcore.currency.command;

import de.ruben.xcore.currency.account.gui.BankGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerBankCommands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        String commandLabel = command.getLabel();

        if(commandLabel.equalsIgnoreCase("bank")){
            Player player = (Player) sender;

            new BankGui(player).open(player);
        }
        return false;
    }
}
