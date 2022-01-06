package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.XCore;
import de.ruben.xcore.customenchantment.XEnchantment;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xdevapi.XDevApi;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MinerEnchant extends CustomEnchantment {

    public MinerEnchant() {
        super("miner");
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public @NotNull String getName() {
        return "Miner";
    }

    @Override
    public @NotNull EnchantmentTarget enchantmentTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment var1) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack var1) {
        return EnchantmentTarget.TOOL.includes(var1);
    }

    @Override
    public @NotNull ItemStack getBookItem(int level) {
        return ItemBuilder
                .from(Material.ENCHANTED_BOOK)
                .name(displayName())
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Du baust mit §b1 Klick§7, eine Fläche von"),
                        Component.text("§7➥ §b3§7x§b3 §7bzw von §b5§7x§b5§7 Blöcken ab."),
                        Component.text(" "),
                        Component.text("§7➥ Aktuell: §b"+((level*2)+1)+"§7x§b"+((level*2)+1)),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Miner: §b"+level;
    }

    @Override
    public String getLore() {
        return "§7➥ Miner:";
    }

    @Override
    public String getWeiteresLore(int level) {
        return null;
    }

    @Override
    public String getEffekteLore(int level) {
        int fläche = (level*2)+1;
        return "§7➥ Abbaufläche: §b"+fläche+"x"+fläche+"x"+fläche;
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {

        if(event instanceof BlockBreakEvent) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
            if(!blockBreakEvent.getBlock().hasMetadata("brokenBlock")) {
                getBlocks(blockBreakEvent.getBlock(), enchantmentLevel, blocks -> blocks.forEach(block -> {
                    if(block.hasMetadata("brokenBlock")) {
                        ItemStack stackInHand = blockBreakEvent.getPlayer().getInventory().getItemInMainHand();

                        if(stackInHand != null && stackInHand.getType() != Material.AIR) {
                            ((CraftPlayer) blockBreakEvent.getPlayer()).getHandle().playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
                            block.removeMetadata("brokenBlock", XCore.getInstance());
                        }else{
                            block.removeMetadata("brokenBlock", XCore.getInstance());
                        }
                    }
                }), blockBreakEvent.getPlayer().getInventory().getItemInMainHand());
            }
        }
    }

    public void getBlocks(Block start, int radius, Consumer<List<Block>> consumer, ItemStack inhand){
        XEnchantment.getInstance().getThreadPoolExecutor().execute(() -> {
            List<Block> blocks = new ArrayList<>();
            for(double x = start.getLocation().getBlockX() - radius; x <= start.getLocation().getBlockX() + radius; x++){
                for(double y = start.getLocation().getBlockY() - radius; y <= start.getLocation().getBlockY() + radius; y++){
                    for(double z = start.getLocation().getBlockZ() - radius; z <= start.getLocation().getBlockZ() + radius; z++){
                        Location loc = new Location(start.getWorld(), x, y, z);
                        if(loc.getBlock() != null && loc.getBlock().getType() != Material.AIR && loc.getBlock().isValidTool(inhand)) {
                            loc.getBlock().setMetadata("brokenBlock", new FixedMetadataValue(XCore.getInstance(), true));
                            blocks.add(loc.getBlock());
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTask(XCore.getInstance(), () -> consumer.accept(blocks));
        });
    }

}
