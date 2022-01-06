package de.ruben.xcore.placeholder;

import de.ruben.xcore.currency.service.BankService;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.profile.model.PlayerProfile;
import de.ruben.xcore.profile.service.ProfileService;
import de.ruben.xcore.util.ShortNumberFormat;
import de.ruben.xdevapi.XDevApi;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.dsig.keyinfo.X509Data;

public class DataPlaceHolderExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "adata";
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
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        ProfileService profileService = new ProfileService();
        PlayerProfile profile = profileService.getProfile(player.getUniqueId());

        if(params.equalsIgnoreCase("money")){
            return ShortNumberFormat.format(new CashService().getValue(player.getUniqueId()).longValue());
        }else if(params.equalsIgnoreCase("bank")) {
            return ShortNumberFormat.format(new BankService().getValue(player.getUniqueId()).longValue());
        }else if(params.equalsIgnoreCase("onltime")){
            return "1h";
        }else if(params.equalsIgnoreCase("messages")){
            return ShortNumberFormat.format(profile.getMessages());
        }else if(params.equalsIgnoreCase("kills")){
            return ShortNumberFormat.format(profile.getMonsterKills()+profile.getPlayerKills());
        }else if(params.equalsIgnoreCase("death")){
            return ShortNumberFormat.format(profile.getDied());
        }else{
            return null;
        }
    }
}
