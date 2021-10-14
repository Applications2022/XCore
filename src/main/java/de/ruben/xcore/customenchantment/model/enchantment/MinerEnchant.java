package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.XCore;
import de.ruben.xcore.customenchantment.XEnchantment;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
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
import java.util.List;
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
    public int getStarterLevel() {
        return 1;
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
    public @NotNull Component displayName() {
        return Component.text("Miner");
    }

    @Override
    public @NotNull ItemStack getBookItem(int level) {
        return new ItemStack(Material.ACACIA_WOOD);
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
    public void handleEvent(Event event, int enchantmentLevel) {
        if(event instanceof BlockBreakEvent){
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
            if(!blockBreakEvent.getBlock().hasMetadata("brokenBlock")) {
                getBlocks(blockBreakEvent.getBlock(), enchantmentLevel, blocks -> blocks.forEach(block -> ((CraftPlayer) blockBreakEvent.getPlayer()).getHandle().playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()))));
            }
        }
    }

    @Override
    public boolean canHandle(Event event) {
        return false;
    }

    public void getBlocks(Block start, int radius, Consumer<List<Block>> consumer){
        XEnchantment.getInstance().getThreadPoolExecutor().execute(() -> {
            List<Block> blocks = new ArrayList<>();
            for(double x = start.getLocation().getBlockX() - radius; x <= start.getLocation().getBlockX() + radius; x++){
                for(double y = start.getLocation().getBlockY() - radius; y <= start.getLocation().getBlockY() + radius; y++){
                    for(double z = start.getLocation().getBlockZ() - radius; z <= start.getLocation().getBlockZ() + radius; z++){
                        Location loc = new Location(start.getWorld(), x, y, z);
                        if(loc.getBlock() != null && loc.getBlock().getType() != Material.AIR) {
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
