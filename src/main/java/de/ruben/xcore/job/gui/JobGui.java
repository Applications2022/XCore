package de.ruben.xcore.job.gui;

import de.ruben.xcore.job.metrix.LevelMetrix;
import de.ruben.xcore.job.model.Job;
import de.ruben.xcore.job.model.JobPlayer;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.custom.gui.NoLabyGUITemplate;
import de.ruben.xdevapi.custom.gui.response.ConfirmationResponse;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import de.ruben.xcore.job.service.JobService;

import java.lang.management.PlatformLoggingMXBean;
import java.util.Set;

public class JobGui extends Gui {
    public JobGui(Player player) {
        super(6, "§9§lLevel",  Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

    }

    @Override
    public void open(@NotNull HumanEntity player) {
        JobService jobService = new JobService(player.getUniqueId());

        JobPlayer jobPlayer = jobService.getJobPlayer();

        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.setDefaultClickAction(event -> event.setCancelled(true));

        this.getFiller().fill(ItemPreset.fillItem(event -> {}));

        this.setItem(49, ItemPreset.closeItem(event -> player.closeInventory()));

        this.setItem(53, ItemBuilder
                .from(Material.NETHER_STAR)
                .name(Component.text("§bTop Spieler"))
                .asGuiItem(event -> {
                    new JobTopGui().open(player);
                }));

        Job minerJob = jobPlayer.getJob("Miner");

        this.setItem(11, ItemBuilder
                .from(Material.NETHERITE_PICKAXE)
                .name(minerJob.getLevel() >= 40 ? Component.text("§bMinenarbeiter §7(§aPrestige verfügbar!§7)") : Component.text("§bMinenarbeiter"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Prestige: §b"+minerJob.getPrestige()),
                        Component.text("§7➥ Aktuelles Level: §b"+minerJob.getLevel()),
                        Component.text("§7➥ Xp: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(minerJob.getCurentXP()) +"/"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(LevelMetrix.getXpNeeded(minerJob.getLevel()))),
                        Component.text("§7➥ Fortschritt: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((minerJob.getCurentXP() /LevelMetrix.getXpNeeded(minerJob.getLevel())*100))+"%"),
                        Component.text(" "),
                        Component.text("§7Linksklick: Infos zu Blöcken"),
                        Component.text(minerJob.getLevel() >= 40 ? "§7Shift+Rechtsklick: Prestige kaufen (§b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((minerJob.getPrestige()+1)*1000000)+"€)" : "")
                ).asGuiItem(event -> {
                    if(event.getClick() == ClickType.SHIFT_RIGHT && minerJob.getLevel() >= 40){
                        NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§bPrestige Kauf bestätigen!", (gui, confirmationResponse, player1) -> {
                            if(confirmationResponse == ConfirmationResponse.NO){
                                player.closeInventory();
                            }else{
                                player.closeInventory();
                                jobService.prestige("Miner");
                            }
                        });
                    }else {
                        new JobBlockInventory("Miner").open(player);
                    }
                })
        );

        Job lumberjackJob = jobPlayer.getJob("Holzfäller");

        this.setItem(13, ItemBuilder
                .from(Material.NETHERITE_AXE)
                .name(lumberjackJob.getLevel() >= 40 ? Component.text("§bHolzfäller §7(§aPrestige verfügbar!§7)") : Component.text("§bHolzfäller"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Prestige: §b"+lumberjackJob.getPrestige()),
                        Component.text("§7➥ Aktuelles Level: §b"+lumberjackJob.getLevel()),
                        Component.text("§7➥ Xp: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(lumberjackJob.getCurentXP()) +"/"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(LevelMetrix.getXpNeeded(lumberjackJob.getLevel()))),
                        Component.text("§7➥ Fortschritt: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((lumberjackJob.getCurentXP() /LevelMetrix.getXpNeeded(lumberjackJob.getLevel())*100))+"%"),
                        Component.text(" "),
                        Component.text("§7Linksklick: Infos zu Blöcken"),
                        Component.text(lumberjackJob.getLevel() >= 40 ? "§7Shift+Rechtsklick: Prestige kaufen (§b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((lumberjackJob.getPrestige()+1)*1000000)+"€)" : "")
                ).asGuiItem(event -> {
                    if(event.getClick() == ClickType.SHIFT_RIGHT && minerJob.getLevel() >= 40){
                        NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§bPrestige Kauf bestätigen!", (gui, confirmationResponse, player1) -> {
                            if(confirmationResponse == ConfirmationResponse.NO){
                                player.closeInventory();
                            }else{
                                player.closeInventory();
                                jobService.prestige("Holzfäller");
                            }
                        });
                    }else {
                        new JobBlockInventory("Holzfäller").open(player);
                    }
                })
        );

        Job diggerJob = jobPlayer.getJob("Gräber");

        this.setItem(15, ItemBuilder
                .from(Material.NETHERITE_SHOVEL)
                .name(diggerJob.getLevel() >= 40 ? Component.text("§bGräber §7(§aPrestige verfügbar!§7)") : Component.text("§bGräber"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Prestige: §b"+diggerJob.getPrestige()),
                        Component.text("§7➥ Aktuelles Level: §b"+diggerJob.getLevel()),
                        Component.text("§7➥ Xp: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(diggerJob.getCurentXP()) +"/"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(LevelMetrix.getXpNeeded(diggerJob.getLevel()))),
                        Component.text("§7➥ Fortschritt: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((diggerJob.getCurentXP() /LevelMetrix.getXpNeeded(diggerJob.getLevel())*100))+"%"),
                        Component.text(" "),
                        Component.text("§7Linksklick: Infos zu Blöcken"),
                        Component.text(diggerJob.getLevel() >= 40 ? "§7Shift+Rechtsklick: Prestige kaufen (§b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((diggerJob.getPrestige()+1)*1000000)+"€)" : "")
                ).asGuiItem(event -> {
                    if(event.getClick() == ClickType.SHIFT_RIGHT && minerJob.getLevel() >= 40){
                        NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§bPrestige Kauf bestätigen!", (gui, confirmationResponse, player1) -> {
                            if(confirmationResponse == ConfirmationResponse.NO){
                                player.closeInventory();
                            }else{
                                player.closeInventory();
                                jobService.prestige("Gräber");
                            }
                        });
                    }else {
                        new JobBlockInventory("Gräber").open(player);
                    }
                })
        );

        Job farmerJob = jobPlayer.getJob("Farmer");

        this.setItem(30, ItemBuilder
                .from(Material.NETHERITE_HOE)
                .name(farmerJob.getLevel() >= 40 ? Component.text("§bFarmer §7(§aPrestige verfügbar!§7)") : Component.text("§bFarmer"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Prestige: §b"+farmerJob.getPrestige()),
                        Component.text("§7➥ Aktuelles Level: §b"+farmerJob.getLevel()),
                        Component.text("§7➥ Xp: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(farmerJob.getCurentXP()) +"/"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(LevelMetrix.getXpNeeded(farmerJob.getLevel()))),
                        Component.text("§7➥ Fortschritt: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((farmerJob.getCurentXP() /LevelMetrix.getXpNeeded(farmerJob.getLevel())*100))+"%"),
                        Component.text(" "),
                        Component.text("§7Linksklick = Infos zu Blöcken"),
                        Component.text(farmerJob.getLevel() >= 40 ? "§7Shift+Rechtsklick: Prestige kaufen (§b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((farmerJob.getPrestige()+1)*1000000)+"€)" : "")
                ).asGuiItem(event -> {
                    if(event.getClick() == ClickType.SHIFT_RIGHT && minerJob.getLevel() >= 40){
                        NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§bPrestige Kauf bestätigen!", (gui, confirmationResponse, player1) -> {
                            if(confirmationResponse == ConfirmationResponse.NO){
                                player.closeInventory();
                            }else{
                                player.closeInventory();
                                jobService.prestige("Farmer");
                            }
                        });
                    }else {
                        new JobBlockInventory("Farmer").open(player);
                    }
                })
        );

        Job hunterJob = jobPlayer.getJob("Jäger");

        this.setItem(32, ItemBuilder
                .from(Material.NETHERITE_SWORD)
                .name(hunterJob.getLevel() >= 40 ? Component.text("§bJäger §7(§aPrestige verfügbar!§7)") : Component.text("§bJäger"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Prestige: §b"+hunterJob.getPrestige()),
                        Component.text("§7➥ Aktuelles Level: §b"+hunterJob.getLevel()),
                        Component.text("§7➥ Xp: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(hunterJob.getCurentXP()) +"/"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(LevelMetrix.getXpNeeded(hunterJob.getLevel()))),
                        Component.text("§7➥ Fortschritt: §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((hunterJob.getCurentXP() /LevelMetrix.getXpNeeded(hunterJob.getLevel())*100))+"%"),
                        Component.text(" "),
                        Component.text("§7Linksklick: Infos zu Mobs"),
                        Component.text(hunterJob.getLevel() >= 40 ? "§7Shift+Rechtsklick: Prestige kaufen (§b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat((hunterJob.getPrestige()+1)*1000000)+"€)" : "")
                ).asGuiItem(event -> {
                    if(event.getClick() == ClickType.SHIFT_RIGHT && minerJob.getLevel() >= 40){
                        NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§bPrestige Kauf bestätigen!", (gui, confirmationResponse, player1) -> {
                            if(confirmationResponse == ConfirmationResponse.NO){
                                player.closeInventory();
                            }else{
                                player.closeInventory();
                                jobService.prestige("Jäger");
                            }
                        });

                    }else {
                        new JobBlockInventory("Jäger").open(player);
                    }
                })
        );

        super.open(player);
    }
}
