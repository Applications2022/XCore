package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.util.type.StackPile;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class GuillotineEnchant extends CustomEnchantment {
    public GuillotineEnchant() {
        super("guillotine");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 10;
    }

    @Override
    public @NotNull String getName() {
        return "Guillotine";
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
        return EnchantmentTarget.ALL.includes(var1);
    }

    @Override
    public @NotNull Component displayName() {
        return Component.text("§bGuillotine");
    }

    @Override
    public @NotNull ItemStack getBookItem(int level) {
        return ItemBuilder
                .from(Material.ENCHANTED_BOOK)
                .name(displayName())
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Pro Level erhöht sich die Chance um §b5%§7,"),
                        Component.text("§7➥ bei einem Kill, den Kopf des Spielers zu erhalten."),
                        Component.text(" "),
                        Component.text("§7➥ Aktuell: §b"+(level*5)+"%"),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Guillotine: §b"+level;
    }

    @Override
    public String getLore() {
        return "§7➥ Guillotine:";
    }

    @Override
    public String getWeiteresLore(int level) {
        return null;
    }

    @Override
    public String getEffekteLore(int level) {
        return "§7➥ Kopf-Chance: §b"+(level*5)+"%";
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {
        if(event instanceof PlayerDeathEvent){
            PlayerDeathEvent playerDeathEvent = (PlayerDeathEvent) event;

            Player killer = playerDeathEvent.getEntity().getKiller();

            if(killer != null){

                int randomNumber = ThreadLocalRandom.current().nextInt(100);

                if(randomNumber < (enchantmentLevel*5)){

                    ItemStack itemStack = ItemBuilder.from(Material.PLAYER_HEAD).setSkullOwner(Bukkit.getOfflinePlayer(playerDeathEvent.getEntity().getUniqueId())).build();

                    if(XDevApi.getInstance().getxUtil().getBukkitInventoryUtil().hasStorageContentSpaceFor(killer.getInventory(), new StackPile(itemStack))){
                        killer.getInventory().addItem(itemStack);
                    }else{
                        killer.getLocation().getWorld().dropItemNaturally(killer.getLocation(), itemStack);
                    }

                    killer.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast den Kopf von §b"+playerDeathEvent.getEntity().getName()+" §7erhalten!");

                }

            }


        }
    }
}
