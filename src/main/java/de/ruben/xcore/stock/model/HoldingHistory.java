package de.ruben.xcore.stock.model;

import de.ruben.xdevapi.XDevApi;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoldingHistory {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);

    private Date date;
    private String stockStymbol;
    private StockType stockType;
    private double stockAmount;
    private double price;
    private HoldingHistoryType holdingHistoryType;

    public Document toDocument(){
        Document document = new Document();
        document.append("date", date);
        document.append("stockSymbol", stockStymbol);
        document.append("stockType", stockType.toString());
        document.append("stockAmount", stockAmount);
        document.append("price", price);
        document.append("type", holdingHistoryType.toString());

        return document;
    }

    public HoldingHistory fromDocument(Document document){

        this.date = document.getDate("date");
        this.stockStymbol = document.getString("stockSymbol");
        this.stockType = StockType.valueOf(document.getString("stockType"));
        this.stockAmount = document.getDouble("stockAmount");
        this.price = document.getDouble("price");
        this.holdingHistoryType = HoldingHistoryType.valueOf(document.getString("type"));

        return this;
    }

    public ItemStack toItemStack(){
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        ItemStack itemStack = ItemBuilder
                                .from(stockType.getMaterial())
                                .name(Component.text("§b"+stockStymbol))
                                .lore(
                                        Component.text(" "),
                                        Component.text("§7➥ Anzahl: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(stockAmount)),
                                        Component.text("§7➥ Typ: "+ (holdingHistoryType == HoldingHistoryType.SOLD ? "§cVerkauf" : "§2Kauf")),
                                        Component.text("§7➥ Preis: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(price)+"€"),
                                        Component.text(" "),
                                        Component.text("§7➥ Datum: §b"+dateFormat.format(date))
                                )
                                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setObject("holdingHistory", this);

        return nbtItem.getItem();
    }
}
