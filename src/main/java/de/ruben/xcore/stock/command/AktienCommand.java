package de.ruben.xcore.stock.command;

import de.ruben.xcore.stock.gui.StockGui;
import de.ruben.xcore.stock.model.StockType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AktienCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        new StockGui().open((Player) sender, StockType.CRYPTOCURRENCY);

        return false;
    }
}
