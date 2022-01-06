package de.ruben.xcore.clan.gui.conversation;

import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.model.Clan;
import de.ruben.xcore.clan.model.ClanRank;
import de.ruben.xcore.clan.service.ClanChat;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xdevapi.XDevApi;
import org.bukkit.ChatColor;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagColorConversation implements ConversationAbandonedListener {

    private ConversationFactory conversationFactory;

    private Clan clan;

    public TagColorConversation(Clan clan){
        this.conversationFactory = new ConversationFactory(XCore.getInstance())
                .withModality(true)
                .withPrefix(new ConversationPrefix() {
                    @Override
                    public @NotNull String getPrefix(@NotNull ConversationContext conversationContext) {
                        return "§9§lTag-Color ändern §8| §7";
                    }
                })
                .withFirstPrompt(new TagColorInput())
                .withEscapeSequence("stop")
                .addConversationAbandonedListener(this)
                .withTimeout(30)
                .thatExcludesNonPlayersWithMessage("Go away!");

        this.clan = clan;

    }

    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }

    @Override
    public void conversationAbandoned(@NotNull ConversationAbandonedEvent conversationAbandonedEvent) {
        if(!conversationAbandonedEvent.gracefulExit()){
            ((Player) conversationAbandonedEvent.getContext().getForWhom()).sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDer Prozess wurde abgebrochen.");
        }
    }

    public class TagColorInput extends StringPrompt {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return "§7Wie soll der neue Name deines Clans lauten? (Color Code (&6) oder Hex Farbcodes (#123456), §cstop §7eingeben um Vorgang abzubrechen)";
        }

        @Override
        public @Nullable Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
            Player player = (Player) conversationContext.getForWhom();

            s = s.replace('&', '§');

            if(s.length() <= 7 && (isColorCode(s) || isHexColor(s))){
                conversationContext.setSessionData("tagcolor", s);
                return new ConfirmPrompt();
            }else{
                return ClanCreateConversation.getMessagePromt("§cDer Prozess wurde abgebrochen, du ein Color Code oder einen Hex Color Code angeben musst!");
            }
        }

        private boolean isHexColor(String string){
            Pattern HEX_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");

            Matcher matcher = HEX_PATTERN.matcher(string);

            return matcher.matches();
        }

        private boolean isColorCode(String string){
            Pattern HEX_PATTERN = Pattern.compile("§([0-f]|[k-o]|r)");

            Matcher matcher = HEX_PATTERN.matcher(string);

            return matcher.matches();
        }
    }

    public class ConfirmPrompt extends FixedSetPrompt{

        public ConfirmPrompt(){
            super("ja", "nein");
        }

        @Override
        protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
            return input.equalsIgnoreCase("ja") || input.equalsIgnoreCase("nein");
        }

        @Override
        protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext conversationContext, @NotNull String s) {

            if(s.equalsIgnoreCase("ja")){
                return new EndPrompt();
            }else if(s.equalsIgnoreCase("nein")){
                return ClanCreateConversation.getMessagePromt("§cDu hast den Prozess abgebrochen!");
            }else{
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("§7Willst du die Tag Farbe deines Clans von §b"+clan.getTagColor()+"ALT §7zu "+ (String) conversationContext.getSessionData("tagcolor")+"NEU §7für §b1.000.000€ §7ändern? (§2Ja §7| §cNein§7)");
        }
    }

    public class EndPrompt extends MessagePrompt{

        @Override
        protected @Nullable Prompt getNextPrompt(@NotNull ConversationContext conversationContext) {
            return Prompt.END_OF_CONVERSATION;
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {

            Player player = (Player) conversationContext.getForWhom();

            String tagColor = ((String) conversationContext.getSessionData("tagcolor"));

            if(new CashService().getValue(player.getUniqueId()) >= 1000000){
                if(new ClanPlayerService().isInClan(player.getUniqueId())){
                    Clan clan = new ClanPlayerService().getClan(player.getUniqueId());

                    if(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRank(clan).hasPermission(ClanRank.ClanRankPermission.CHANGE_TAG_COLOR) || clan.isOwner(player)){
                        new CashService().removeValue(player.getUniqueId(), 1000000);
                        new ClanService().setClanTagColor(clan.getId(), tagColor);
                        new ClanService().getClanChat(clan).sendLogMessage("§7Die Farbe des Clan Tags wurde zu "+tagColor+"███ §7geändert!");
                        return "Du hast die Tag Farbe deines Clans erfolgreich geändert!";

                    }else{
                        return "§cDazu hast du in deinem Clan keine Rechte!";
                    }
                }else{
                    return "§cDu bist in keinem Clan!";
                }
            }else{
                return "§cDazu hast du zur Zeit noch zu wenig Geld!";
            }
        }
    }
}
