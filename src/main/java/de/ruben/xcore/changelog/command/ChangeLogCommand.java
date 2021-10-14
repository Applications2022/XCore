package de.ruben.xcore.changelog.command;

import de.ruben.xcore.changelog.XChangelog;
import de.ruben.xcore.changelog.gui.ChangelogGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChangeLogCommand implements CommandExecutor {

    private XChangelog xChangelog;

    public ChangeLogCommand(XChangelog xChangelog) {
        this.xChangelog = xChangelog;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        Player player = (Player) sender;

        new ChangelogGui(xChangelog, player).open(player);

        return false;
    }
}
