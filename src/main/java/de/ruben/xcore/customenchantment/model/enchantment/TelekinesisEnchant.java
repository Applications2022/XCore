package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.util.type.StackPile;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TelekinesisEnchant extends CustomEnchantment {


    public TelekinesisEnchant() {
        super("telekinesis");
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
        return "Telekinesis";
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
        return enchantmentTarget().includes(var1);
    }

    @Override
    public @NotNull ItemStack getBookItem(int level) {
        ItemStack bookStack = ItemBuilder
                .from(Material.ENCHANTED_BOOK)
                .name(displayName())
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Du bekommst §bBlöcke §7und §bItems"),
                        Component.text("§7➥ direkt in dein §bInventar."),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level: §b"+getMaxLevel()),
                        Component.text(" ")
                )
                .build();

        NBTItem nbtItem = new NBTItem(bookStack);
        nbtItem.setInteger(getBookKey(), level);

        return nbtItem.getItem();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Telekinesis";
    }

    @Override
    public String getLore() {
        return "§7➥ Telekinesis";
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

            blockBreakEvent.setDropItems(false);

            CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(blockBreakEvent.getPlayer().getInventory().getItemInMainHand());

            Collection<ItemStack> drops = getDrops(blockBreakEvent.getBlock().getDrops(customEnchantedItem.getItemStack()), customEnchantedItem);

            List<StackPile> stackPile = drops.stream().map(StackPile::new).collect(Collectors.toCollection(ArrayList::new));

            if(stackPile.size() > 0) {
                if (XDevApi.getInstance().getxUtil().getBukkitInventoryUtil().hasStorageContentSpaceFor(blockBreakEvent.getPlayer().getInventory(), stackPile.toArray(new StackPile[0]))) {
                    blockBreakEvent.getPlayer().getInventory().addItem(drops.toArray(new ItemStack[stackPile.size()]));
                } else {
                    stackPile.forEach(stackPile1 -> blockBreakEvent.getBlock().getWorld().dropItemNaturally(blockBreakEvent.getBlock().getLocation(), stackPile1.getStack()));
                }
            }
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
