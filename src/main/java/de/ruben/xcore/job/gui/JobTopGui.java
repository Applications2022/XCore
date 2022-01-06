package de.ruben.xcore.job.gui;

import de.ruben.xdevapi.custom.gui.ItemPreset;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import de.ruben.xcore.job.service.JobService;

import java.util.Set;

public class JobTopGui extends Gui {
    public JobTopGui() {
        super(6, "§9§lJobs §8| §9§lTop-Spieler", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));
    }

    @Override
    public void open(@NotNull HumanEntity player) {
        this.getFiller().fill(ItemPreset.fillItem(event -> {}));
        JobService jobService = new JobService();

        this.setItem(11, jobService.getTopItem("Miner", Material.NETHERITE_PICKAXE));
        this.setItem(13, jobService.getTopItem("Holzfäller", Material.NETHERITE_AXE));
        this.setItem(15, jobService.getTopItem("Gräber", Material.NETHERITE_SHOVEL));
        this.setItem(30, jobService.getTopItem("Farmer", Material.NETHERITE_HOE));
        this.setItem(32, jobService.getTopItem("Jäger", Material.NETHERITE_SWORD));

        this.setItem(49, ItemPreset.closeItem(event -> player.closeInventory()));

        this.setItem(45, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> new JobGui((Player) player).open(player)));

        super.open(player);
    }
}
