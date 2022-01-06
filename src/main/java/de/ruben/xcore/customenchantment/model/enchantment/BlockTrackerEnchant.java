package de.ruben.xcore.customenchantment.model.enchantment;

import com.google.protobuf.DescriptorProtos;
import de.ruben.xcore.customenchantment.model.CustomEnchantedItem;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xdevapi.XDevApi;
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

public class BlockTrackerEnchant extends CustomEnchantment {

    public BlockTrackerEnchant() {
        super("blocktracker");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 1;
    }

    @Override
    public @NotNull String getName() {
        return "Blocktracker";
    }

    @Override
    public @NotNull EnchantmentTarget enchantmentTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public ItemStack enchantItem(ItemStack itemStack, int level) {

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setInteger(getItemKey(), level);

        Map<String, Object> extraData = new HashMap<>();
        extraData.put("blocks", 0);
        extraData.put("lorepos", 0);

        nbtItem.setObject(extraDataKey(), extraData);

        return nbtItem.getItem();
    }

    @Override
    public ItemStack disenchantItem(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.removeKey(getItemKey());

        Map<String, Object> extraData = nbtItem.getObject(extraDataKey(), Map.class);
        int lorepos = ((Integer) extraData.get("lorepos"));

        nbtItem.removeKey(extraDataKey());

        ItemStack finalStack = nbtItem.getItem();

        finalStack.editMeta(itemMeta -> {
           List<String> lore = itemMeta.getLore();

           lore.remove(lorepos);

           itemMeta.setLore(lore);

           finalStack.setItemMeta(itemMeta);

        });

        return finalStack;
    }

    @Override
    public boolean hasExtraData() {
        return true;
    }

    @Override
    public ItemStack repopulateExtraData(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);

        Map<String, Object> extraData = nbtItem.getObject(extraDataKey(), Map.class);

        List<String> lore = itemStack.getItemMeta().getLore();

        for(String string : lore){
            if(string.startsWith("§7➥ §7Blöcke: §b")){
                int lorepos = lore.indexOf(string);
                extraData.replace("lorepos", lorepos);
                break;
            }
        }

        nbtItem.setObject(extraDataKey(), extraData);

        return nbtItem.getItem();

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
                        Component.text("§7➥ Blöcke, die du abbaust, werden unter"),
                        Component.text("§7➥ deinem Item gezählt und angezeigt."),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Blocktracker";
    }

    @Override
    public String getLore() {
        return "§7➥ Blocktracker";
    }

    @Override
    public String getWeiteresLore(int level) {
        return "§7➥ §7Blöcke: §b0";
    }

    @Override
    public String getEffekteLore(int level) {
        return null;
    }

    @Override
    public Map<String, Object> getExtraData(ItemStack itemStack) {
        return new NBTItem(itemStack).getObject(extraDataKey(), Map.class);
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {
        
        if(event instanceof BlockBreakEvent) {
            BlockBreakEvent blockBreakEvent = (BlockBreakEvent) event;

            ItemStack itemStack = blockBreakEvent.getPlayer().getInventory().getItemInMainHand();
            CustomEnchantedItem customEnchantedItem = new CustomEnchantedItem(itemStack);

            Map<String, Object> extraData = customEnchantedItem.getExtraData(CustomEnchantment.BLOCKTRACKER);

            int lorePos = (int) Math.round((Double) extraData.get("lorepos"));
            int blocks = (int) Math.round(((Double) extraData.get("blocks"))+1);

            extraData.replace("blocks", blocks);

            NBTItem nbtItem = new NBTItem(itemStack);
            nbtItem.setObject(extraDataKey(), extraData);

            ItemStack finalItemStack = nbtItem.getItem();
            finalItemStack.editMeta(itemMeta -> {
                List<Component> lore = itemMeta.lore();

                lore.set(lorePos, Component.text("§7➥ §7Blöcke: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(blocks)));

                itemMeta.lore(lore);

                finalItemStack.setItemMeta(itemMeta);
            });

            blockBreakEvent.getPlayer().getInventory().setItemInMainHand(finalItemStack);

        }
    }
}
