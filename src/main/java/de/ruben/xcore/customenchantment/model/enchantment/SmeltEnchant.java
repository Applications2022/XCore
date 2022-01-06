package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class SmeltEnchant extends CustomEnchantment {
    public SmeltEnchant() {
        super("smelt");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
        return "Schmelzen";
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
                        Component.text("§7➥ Wenn du §bEisen§7- und §bGold§7-§bErz §7abbaust,"),
                        Component.text("§7➥ erhältst du direkt die Barren."),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Schmelzen";
    }

    @Override
    public String getLore() {
        return "§7➥ Schmelzen";
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
        if(event instanceof BlockBreakEvent) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;

            blockBreakEvent.setDropItems(false);

            CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(blockBreakEvent.getPlayer().getInventory().getItemInMainHand());

            Collection<ItemStack> drops = getDrops(blockBreakEvent.getBlock().getDrops(customEnchantedItem.getItemStack()), customEnchantedItem);

            drops.forEach(itemStack -> {
                blockBreakEvent.getBlock().getLocation().getWorld().dropItemNaturally(blockBreakEvent.getBlock().getLocation(), itemStack);
            });
        }
    }

    private Collection<ItemStack> getDrops(Collection<ItemStack> drops, CustomEnchantedItem customEnchantedItem){

        if(customEnchantedItem.hasCustomEnchantment(CustomEnchantment.SMELT)){

            return drops.stream().map(itemStack -> {
                if(itemStack.getType().name().toLowerCase().endsWith("ore")){

                    String newMaterialName = itemStack.getType().name().toUpperCase().replace("_ORE", "");

                    if(Material.getMaterial(newMaterialName) == null){
                        newMaterialName = newMaterialName+"_INGOT";
                    }

                    return new ItemStack(Material.getMaterial(newMaterialName), itemStack.getAmount());


                }else{
                    return itemStack;
                }
            }).collect(Collectors.toList());

        }else{
            return drops;
        }
    }
}
