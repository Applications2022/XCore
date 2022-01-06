package de.ruben.xcore.customenchantment.model.enchantment;

import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.customenchantment.model.CustomEnchantment;
import de.ruben.xdevapi.XDevApi;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
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

public class TaschendiebEnchant extends CustomEnchantment {
    public TaschendiebEnchant() {
        super("taschendieb");
    }

    @Override
    public @NotNull int getMaxLevel() {
        return 5;
    }

    @Override
    public @NotNull String getName() {
        return "Taschendieb";
    }

    @Override
    public @NotNull EnchantmentTarget enchantmentTarget() {
        return  EnchantmentTarget.TOOL;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment var1) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack var1) {
        return  EnchantmentTarget.ALL.includes(var1);
    }

    @Override
    public @NotNull ItemStack getBookItem(int level) {
        return ItemBuilder
                .from(Material.ENCHANTED_BOOK)
                .name(displayName())
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Pro Level erhöht sich die Chance um §b10%§7,"),
                        Component.text("§7➥ bei einem Kill, dem getöteten Spieler, §b25%"),
                        Component.text("§7➥ §7seines Geldes zu stehlen."),
                        Component.text(" "),
                        Component.text("§7➥ Aktuell: §b"+(level*10)+"%"),
                        Component.text(" "),
                        Component.text("§7Aktuelles Level: §b"+level),
                        Component.text("§7Maximales Level §b"+getMaxLevel())

                )
                .build();
    }

    @Override
    public String getLore(int level) {
        return "§7➥ Taschendieb: §b"+level;
    }

    @Override
    public String getLore() {
        return "§7➥ Taschendieb: §b";
    }

    @Override
    public String getWeiteresLore(int level) {
        return null;
    }

    @Override
    public String getEffekteLore(int level) {
        return "§7➥ Geld-Chance: §b"+(level*10)+"%";
    }

    @Override
    public void handleEvent(Event event, int enchantmentLevel) {

        if(event instanceof PlayerDeathEvent){
            PlayerDeathEvent playerDeathEvent = (PlayerDeathEvent) event;

            Player killer = playerDeathEvent.getEntity().getKiller();

            if(killer != null){

                int randomNumber = ThreadLocalRandom.current().nextInt(100);

                if(randomNumber < (enchantmentLevel*10)){

                    double valueToGive = new CashService().getValue(playerDeathEvent.getEntity().getUniqueId())/4;

                    new CashService().removeValue(playerDeathEvent.getEntity().getUniqueId(), valueToGive, cashAccount -> {
                        new CashService().addValue(killer.getUniqueId(), valueToGive, cashAccount1 -> {

                            if(playerDeathEvent.getEntity() != null){
                                playerDeathEvent.getEntity().sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§b"+killer.getName()
                                        +" §7hat dir 25% deines Kontostandes gestohlen! (§b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(valueToGive)+"€§7)");
                            }

                            if(killer != null){
                                killer.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast §b"+playerDeathEvent.getEntity().getName()+" "+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(valueToGive)+"€ §7gestohlen!");
                            }

                        });
                    });

                }

            }


        }

    }
}
