package de.ruben.xcore.stock.model;

import de.ruben.xcore.stock.XStocks;
import de.ruben.xdevapi.XDevApi;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockContainer {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);


    private StockType stockType;
    private String symbol;
    private Double regularMarketPrice;
    private Double regularMarketPreviousClose;
    private Double change;
    private String shortName;
    private String displayName;
    private String currency;
    private Long loadTime;

    public ItemStack toItemStack(){
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        String changeString = (getChange() >= 0 ? "§a+" : "§c") + XStocks.getInstance().getStockFormat().format(getChange()) + "€";

        double changePercentage = (getRegularMarketPrice() / ((getRegularMarketPrice()-getChange())/100))-100;

        String changeStringpercentage = (changePercentage >= 0 ? "§a+" : "§c") + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(changePercentage)+"%";


        ItemStack itemStack =  ItemBuilder
                .from(stockType.getMaterial())
                .name(Component.text("§b"+(getShortName() == null ? getSymbol() : getShortName())))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Symbol: §b"+getSymbol()),
                        Component.text("§7➥ Aktueller Preis: §b"+ XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(getRegularMarketPrice())+ "€"),
                        Component.text("§7➥ Änderung (Vortag): §b"+changeString+" §7("+changeStringpercentage+"§7)"),
                        Component.text(" "),
                        Component.text("§7Stand: §b"+dateFormat.format(new Date(loadTime)))
                )
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setObject("stock", this);

        return nbtItem.getItem();
    }

    public Double getRegularMarketPrice() {
        if(currency.equals("USD")){
            return XStocks.getInstance().getCurrentUSDEURExchange()*regularMarketPrice;
        }
        return regularMarketPrice;
    }

    public Double getChange() {
        if(currency.equals("USD")){
            return XStocks.getInstance().getCurrentUSDEURExchange()*change;
        }
        return change;
    }
}
