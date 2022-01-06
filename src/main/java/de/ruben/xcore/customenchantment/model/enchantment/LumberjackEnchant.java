package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.XCore;
import de.ruben.xcore.customenchantment.XEnchantment;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xdevapi.XDevApi;
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
import java.util.List;
import java.util.function.Consumer;

public class LumberjackEnchant extends CustomEnchantment {
    public LumberjackEnchant() {
        super("lumberjack");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
        return "Holzfäller";
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
                        Component.text("§7➥ Du bekommst §bBlöcke §7und §bItems §7,"),
                        Component.text("§7➥ direkt in dein Inventar."),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Holzfäller";
    }

    @Override
    public String getLore() {
        return "§7➥ Holzfäller";
    }

    @Override
    public String getWeiteresLore(int level) {
        return null;
    }

    @Override
    public String getEffekteLore(int level) {
        return null;
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {
        if(event instanceof BlockBreakEvent){
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;
            if(blockBreakEvent.getBlock().getType().name().toLowerCase().endsWith("log")) {
                if (!blockBreakEvent.getBlock().hasMetadata("brokenBlock")) {
                    getBlocks(blockBreakEvent.getBlock(), 10, blocks -> blocks.forEach(block -> {
                        if(block.hasMetadata("brokenBlock")) {

                            ItemStack stackInHand = blockBreakEvent.getPlayer().getInventory().getItemInMainHand();

                            if(stackInHand != null && stackInHand.getType() != Material.AIR) {
                                ((CraftPlayer) blockBreakEvent.getPlayer()).getHandle().playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
                                block.removeMetadata("brokenBlock", XCore.getInstance());
                            }else{
                                block.removeMetadata("brokenBlock", XCore.getInstance());
                            }
                        }
                    }));
                }
            }

        }
    }

    public void getBlocks(Block start, int radius, Consumer<List<Block>> consumer){
        XEnchantment.getInstance().getThreadPoolExecutor().execute(() -> {
            List<Block> blocks = new ArrayList<>();
            for(double x = start.getLocation().getBlockX() + radius; x >= start.getLocation().getBlockX() - radius; x--){
                for(double y = start.getLocation().getBlockY() + radius; y >= start.getLocation().getBlockY() - radius; y--){
                    for(double z = start.getLocation().getBlockZ() + radius; z >= start.getLocation().getBlockZ() - radius; z--){
                        Location loc = new Location(start.getWorld(), x, y, z);
                        if(loc.getBlock() != null && loc.getBlock().getType().name().toLowerCase().endsWith("log") || loc.getBlock().getType() == Material.CRIMSON_STEM || loc.getBlock().getType() == Material.WARPED_STEM ) {
                            loc.getBlock().setMetadata("brokenBlock", new FixedMetadataValue(XCore.getInstance(), true));

                            blocks.add(loc.getBlock());

                            if(blocks.size() >= 25){
                                break;
                            }
                        }
                    }
                    if(blocks.size() >= 25){
                        break;
                    }
                }
                if(blocks.size() >= 25){
                    break;
                }
            }

            Bukkit.getScheduler().runTask(XCore.getInstance(), () -> consumer.accept(blocks));
        });
    }
}
