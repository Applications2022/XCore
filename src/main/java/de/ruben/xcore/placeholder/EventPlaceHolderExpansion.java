package de.ruben.xcore.placeholder;

import de.ruben.xcore.XCore;
import de.ruben.xcore.changelog.XChangelog;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.util.global.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class EventPlaceHolderExpansion extends PlaceholderExpansion {

    private static Long date2Long = 0L;
    private static String title = "";
    private static String info = "";

    @Override
    public @NotNull String getIdentifier() {
        return "aevent";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ruben";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        if(params.equalsIgnoreCase("date")){
            if(date2Long == 0){
                return "---";
            }else {
                Date date = new Date(System.currentTimeMillis());
                Date date2 = new Date((date2Long));

                long diffInMillies = date2.getTime() - date.getTime();

                if(diffInMillies >= 0) {
                    long diffInSeconds = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                    String timeString = XDevApi.getInstance().getxUtil().getGlobal().getTimeUtil().convertSecondsHMS(diffInSeconds);
                    return timeString.startsWith(" ") ? timeString.substring(1) : timeString;
                }else{
                    return "Abgelaufen!";
                }
            }
        }else if(params.equalsIgnoreCase("title")){
            if(title.equals("")){
                return "Kein Event geplant!";
            }else {
                return title;
            }
        }else if(params.equalsIgnoreCase("info")){
            Date date = new Date(System.currentTimeMillis());
            Date date2 = new Date((date2Long));

            long diffInMillies = date2.getTime() - date.getTime();

            if(title.equalsIgnoreCase("") || diffInMillies <= 0){
                return XDevApi.getInstance().getMessageService().getMessage("prefix")+" §bInfo: §cZur Zeit steht kein Event an!";
            }else{
                return XDevApi.getInstance().getMessageService().getMessage("prefix")+" §bInfo: §7"+ChatColor.translateAlternateColorCodes('&', info);
            }
        }else{
            return null;
        }
    }

    public static void setDate2Long(Long date2Long) {
        EventPlaceHolderExpansion.date2Long = date2Long;
    }

    public static void setTitle(String title) {
        EventPlaceHolderExpansion.title = title;
    }

    public static void setInfo(String info) {
        EventPlaceHolderExpansion.info = info;
    }
}
