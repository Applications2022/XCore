package de.ruben.xcore.job.gui;

import de.ruben.xcore.job.metrix.JobXpMetrix;
import de.ruben.xcore.job.metrix.LevelMetrix;
import de.ruben.xcore.job.model.Job;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import de.ruben.xcore.job.service.JobService;

import java.util.Map;
import java.util.Set;

public class JobBlockInventory extends Gui {

    private Map<?, Integer> map;

    private String job;

    public JobBlockInventory(String job) {
        super(Math.round(JobXpMetrix.getMap(job).size()/7+((JobXpMetrix.getMap(job).size()%7) == 0 ? 0 : 1))+2, "§9§lJob §8| §9§l"+job,  Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.job = job;
        this.map = JobXpMetrix.getMap(job);

        this.setDefaultClickAction(event -> {});
    }

    @Override
    public void open(@NotNull HumanEntity player) {


        this.getFiller().fillBorder(ItemPreset.fillItem(event ->  {}));

        this.setItem(getInventory().getSize()-5, ItemPreset.closeItem(event -> player.closeInventory()));

        this.setItem(getInventory().getSize()-9, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> {
            new JobGui((Player) player).open(player);
        }));

        map.forEach((o, integer) -> {

            Material material = Material.AIR;
            if(o instanceof Material){
                material = (Material) o;

                if(material == Material.POTATOES){
                    material = Material.POTATO;
                }else if(material == Material.CARROTS){
                    material = Material.CARROT;
                }else if(material == Material.BEETROOTS){
                    material = Material.BEETROOT_SEEDS;
                }
            }else if( o instanceof EntityType){
                if(((EntityType) o) == EntityType.MUSHROOM_COW){
                    material = Material.MOOSHROOM_SPAWN_EGG;
                }else {
                    material = Material.matchMaterial(((EntityType) o).name().toUpperCase() + "_SPAWN_EGG");
                }
            }

            Job jobModel = new JobService(player.getUniqueId()).getJobPlayer().getJob(job);
            int level = jobModel.getLevel();
            double money = ((level * 0.025) + (integer*0.025) ) * jobModel.getPrestige();


            this.addItem(
                    ItemBuilder
                            .from(material)
                            .lore(
                                    Component.text(" "),
                                    Component.text("§7➥ Xp: §b"+integer),
                                    Component.text("§7➥ Aktueller Verdienst: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) +"€"),
                                    Component.text(" ")
                            )
                            .asGuiItem()
            );

        });
        super.open(player);
    }
}
