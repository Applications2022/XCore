package de.ruben.xcore.tutorialcenter.command;

import de.ruben.xcore.tutorialcenter.model.TutorialModule;
import de.ruben.xcore.tutorialcenter.service.TutorialModuleService;
import de.ruben.xdevapi.XDevApi;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AdminTutorialCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        Player player = (Player) sender;

        TutorialModuleService tutorialModuleService = new TutorialModuleService();

        if(!player.hasPermission("addictZone.admintutorials")){
            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("noperm"));
            return true;
        }

        if(args.length == 2){

            if(args[0].equalsIgnoreCase("create")){
                Integer integer = Integer.parseInt(args[1]);

                if(tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert bereits!");
                    return true;
                }

                tutorialModuleService.createTutorialModule(integer, null, new ArrayList(), ItemBuilder.from(Material.PAPER).name(Component.text("§b"+integer)).build());

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast das Tutorial mit der id §b"+integer+" §7erfolgreich erstellt!");
            }else if(args[0].equalsIgnoreCase("delete")){
                Integer integer = Integer.parseInt(args[1]);

                if(!tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert nicht!");
                    return true;
                }

                tutorialModuleService.deleteTutorialModule(integer);

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast das Tutorial mit der id §b"+integer+" §7erfolgreich gelöscht!");
            }else if(args[0].equalsIgnoreCase("setitem")){
                Integer integer = Integer.parseInt(args[1]);

                if(!tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert nicht!");
                    return true;
                }

                ItemStack itemStack = player.getInventory().getItemInMainHand();

                if(itemStack == null || itemStack.getType() == Material.AIR){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu musst ein Item in die Hand nehmen!");
                    return true;
                }

                TutorialModule tutorialModule = tutorialModuleService.getTutorialModule(integer);
                tutorialModule.setItemStack(itemStack);
                tutorialModuleService.modifyTutorialModule(tutorialModule);

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast das AnzeigeItem des Tutorials mit der Id §b"+integer+" §7erfolgreich gesetzt!");
            }else if(args[0].equalsIgnoreCase("setbook")){
                Integer integer = Integer.parseInt(args[1]);

                if(!tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert nicht!");
                    return true;
                }

                ItemStack itemStack = player.getInventory().getItemInMainHand();

                if(itemStack == null || itemStack.getType() == Material.AIR || itemStack.getType() != Material.WRITTEN_BOOK){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDu musst ein Buch in die Hand nehmen!");
                    return true;
                }

                TutorialModule tutorialModule = tutorialModuleService.getTutorialModule(integer);
                tutorialModule.setAlternativeBookMeta(((BookMeta) itemStack.getItemMeta()));
                tutorialModuleService.modifyTutorialModule(tutorialModule);

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast das Buch des Tutorials mit der Id §b"+integer+" §7erfolgreich gesetzt!");
            }else if(args[0].equalsIgnoreCase("list")){
                Integer integer = Integer.parseInt(args[1]);

                if(!tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert nicht!");
                    return true;
                }

                TutorialModule tutorialModule = tutorialModuleService.getTutorialModule(integer);

                player.sendMessage("§7§m--------------------------------------------------");
                player.sendMessage(" ");
                player.sendMessage("§7Id: §b"+integer);
                player.sendMessage("§7URL's:");
                tutorialModule.getPictureURLS().forEach(s1 -> {
                    TextComponent urlComponent = new TextComponent("§7- §b"+s1+" ");
                    TextComponent removeComponent = new TextComponent("§7[§c-§7]");

                    removeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/admintutorial removeurl "+integer+" "+s1));
                    removeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klicke um url zu löschen").create()));

                    urlComponent.addExtra(removeComponent);

                    player.sendMessage(urlComponent);
                });

                player.sendMessage(" ");
                player.sendMessage("§7§m--------------------------------------------------");
            }else if(args[0].equalsIgnoreCase("open")){
                Integer integer = Integer.parseInt(args[1]);

                if(!tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert nicht!");
                    return true;
                }

                TutorialModule tutorialModule = tutorialModuleService.getTutorialModule(integer);

                tutorialModule.performOpen(player, 0);
            }else{
                sendHelpMessage(player);
            }

        }else if(args.length == 3){
            if(args[0].equalsIgnoreCase("addurl")){
                Integer integer = Integer.parseInt(args[1]);

                if(!tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert nicht!");
                    return true;
                }

                tutorialModuleService.addURL(integer, args[2]);

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich eine URL zu dem Tutorial mit der Id §b"+integer+" §7hinzugefügt!");
            }else if(args[0].equalsIgnoreCase("removeurl")){
                Integer integer = Integer.parseInt(args[1]);

                if(!tutorialModuleService.existTutorialModule(integer)){
                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cEin Tutorial mit dieser id exestiert nicht!");
                    return true;
                }

                tutorialModuleService.removeURL(integer, args[2]);

                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich eine URL von dem Tutorial mit der Id §b"+integer+" §7entfernt!");
            }else{
                sendHelpMessage(player);
            }
        }else{
            sendHelpMessage(player);
        }

        return false;
    }

    private void sendHelpMessage(Player player){
        player.sendMessage("§7§m--------------------------------------------------");
        player.sendMessage(" ");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial create §7<§bid§7>");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial delete §7<§bid§7>");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial addurl <§7<§bid§7> §7<§burl§7>");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial removeurl <§7<§bid§7> §7<§burl§7>");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial setItem <id>");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial setBook <id>");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial list <id>");
        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b/admintutorial open <id>");
        player.sendMessage(" ");
        player.sendMessage("§7§m--------------------------------------------------");

    }
}
