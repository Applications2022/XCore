package de.ruben.xcore.stock.model;

import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Holding{
    private String symbol;
    private double amount;
    private double buyPrice;
    private StockType stockType;

    public Document toDocument(){
        Document document = new Document();
        document.append("symbol", symbol);
        document.append("amount", amount);
        document.append("buyPrice", buyPrice);
        document.append("stockType", stockType.toString());
        return document;
    }

    public Holding fromDocument(Document document){
        this.symbol = document.getString("symbol");
        this.amount = document.getDouble("amount");
        this.buyPrice = document.getDouble("buyPrice");
        this.stockType = StockType.valueOf(document.getString("stockType"));

        return this;
    }

    public ItemStack toItemStack(){
        ItemStack itemStack = ItemBuilder
                .from(stockType.getMaterial())
                .name(Component.text("§b"+amount+"x §7"+symbol))
                .lore(
                        Component.text(" "),
                        Component.text("§7Klicke für mehr Info!"),
                        Component.text(" ")
                )
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setObject("holding_item", this);

        return nbtItem.getItem();
    }


}
