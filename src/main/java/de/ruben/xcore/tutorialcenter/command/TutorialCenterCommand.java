package de.ruben.xcore.tutorialcenter.command;

import de.ruben.xcore.tutorialcenter.gui.TutorialCenterGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TutorialCenterCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        new TutorialCenterGui().open(player, 0);
        return false;
    }
}
