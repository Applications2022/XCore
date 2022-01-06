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

public class ChangeTagConversation implements ConversationAbandonedListener {

    private ConversationFactory conversationFactory;

    private Clan clan;

    public ChangeTagConversation(Clan clan){
        this.conversationFactory = new ConversationFactory(XCore.getInstance())
                .withModality(true)
                .withPrefix(new ConversationPrefix() {
                    @Override
                    public @NotNull String getPrefix(@NotNull ConversationContext conversationContext) {
                        return "§9§lTag ändern §8| §7";
                    }
                })
                .withFirstPrompt(new TagInputPromt())
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

    public class TagInputPromt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return "§7Wie soll der neue Tag deines Clans lauten? (Name max. 4 Zeichen, §cstop §7eingeben um Vorgang abzubrechen)";
        }

        @Override
        public @Nullable Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
            s = ChatColor.stripColor(s);

            Player player = (Player) conversationContext.getForWhom();

            if(s.length() > 0 && s.length() <= 4){
                conversationContext.setSessionData("tag", s);
                return new ConfirmPrompt();
            }else{
                return ClanCreateConversation.getMessagePromt("§cDer Prozess wurde abgebrochen, da der Tag maximal 4 Zeichen lang sein darf!");
            }
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
            return XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("§7Willst du den Tag deines Clans von §b"+clan.getTagColor() + clan.getTag() +" §7zu "+clan.getTagColor()+(String) conversationContext.getSessionData("tag")+" §7für §b100.000€ §7ändern? (§2Ja §7| §cNein§7)");
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

            String tag = ((String) conversationContext.getSessionData("tag"));

            if(new CashService().getValue(player.getUniqueId()) >= 100000){
                if(new ClanPlayerService().isInClan(player.getUniqueId())){
                    Clan clan = new ClanPlayerService().getClan(player.getUniqueId());

                    if(clan.getClanMembers().get(player.getUniqueId().toString()).getClanRank(clan).hasPermission(ClanRank.ClanRankPermission.CHANGE_TAG_NAME) || clan.isOwner(player)){
                        if(!new ClanService().existClanTag(tag)) {
                            new CashService().removeValue(player.getUniqueId(), 100000);
                            new ClanService().setClanTag(clan.getId(), tag);
                            new ClanService().getClanChat(clan).sendLogMessage("§7Der Tag des Clans wurde zu " + clan.getTagColor() + tag + " §7geändert!");
                            return "Du hast den Tag deines Clans erfolgreich geändert!";
                        }else{
                            return "Ein Clan mit diesem tag existiert bereits!";
                        }
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
