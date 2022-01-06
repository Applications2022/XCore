package de.ruben.xcore.customenchantment.command;

import de.ruben.xcore.customenchantment.XEnchantment;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminEnchantTabCompleter implements TabCompleter {
    private static List<String> fullEnchants = new ArrayList<>();
    private static List<String> customEnchants = new ArrayList<>();
    private static List<String> minecraftEnchants = new ArrayList<>();

    public AdminEnchantTabCompleter(){
        customEnchants.addAll(CustomEnchantment.getByKey().keySet().stream().map(s1 -> s1.toUpperCase().replace(" ", "_")).collect(Collectors.toList()));
        minecraftEnchants.addAll(XEnchantment.getEnchantmentNames().values().stream().map(s1 -> s1.toUpperCase().replace(" ", "_")).collect(Collectors.toList()));

        fullEnchants.addAll(customEnchants);
        fullEnchants.addAll(minecraftEnchants);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("enchant")) {
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[1], fullEnchants, completions);

                return completions;
            }else if(args[0].endsWith("book")){
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[1], customEnchants, completions);

                return completions;
            }else{
                return null;
            }


        }else{
            return null;
        }
    }
}
