package de.ruben.xcore.clan.gui.conversation;

import de.ruben.xcore.XCore;
import de.ruben.xcore.clan.XClan;
import de.ruben.xcore.clan.service.ClanPlayerService;
import de.ruben.xcore.clan.service.ClanService;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xdevapi.XDevApi;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClanCreateConversation implements ConversationAbandonedListener{

    private ConversationFactory conversationFactory;

    public ClanCreateConversation(){
        this.conversationFactory = new ConversationFactory(XCore.getInstance())
                .withModality(true)
                .withPrefix(new ConversationPrefix() {
                    @Override
                    public @NotNull String getPrefix(@NotNull ConversationContext conversationContext) {
                        return "§9§lClan Erstellen §8| §7";
                    }
                })
                .withFirstPrompt(new NamePrompt())
                .withEscapeSequence("stop")
                .addConversationAbandonedListener(this)
                .withTimeout(30)
                .thatExcludesNonPlayersWithMessage("Go away!");

    }

    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }

    @Override
    public void conversationAbandoned(@NotNull ConversationAbandonedEvent conversationAbandonedEvent) {
        if(!conversationAbandonedEvent.gracefulExit()){
            ((Player) conversationAbandonedEvent.getContext().getForWhom()).sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cDer Erstellungsprozess wurde abgebrochen.");
        }
    }

    public class NamePrompt extends StringPrompt {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return "§7Wie soll dein Clan heißen? (Name max. 16 Zeichen, §cstop §7eingeben um Vorgang abzubrechen)";
        }

        @Override
        public @Nullable Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
            s = ChatColor.stripColor(s);

            Player player = (Player) conversationContext.getForWhom();

            if(s.length() > 0 && s.length() <= 32){
                conversationContext.setSessionData("name", s);
                return new TagInput();
            }else{
                return ClanCreateConversation.getMessagePromt("§cDer Erstellungsprozess wurde abgebrochen, da der Name maximal 32 Zeichen lang sein darf!");
            }
        }
    }

    public class TagInput extends StringPrompt {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return "§7Wie soll der Tag deines Clans lauten? (Tag max. 4 Zeichen)";
        }

        @Override
        public @Nullable Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {

            Player player = (Player) conversationContext.getForWhom();

            s = ChatColor.stripColor(s);

            if(s.length() > 0 && s.length() <= 4){
                conversationContext.setSessionData("tag", s);
                return new CreateClanInput();
            }else{
                return ClanCreateConversation.getMessagePromt("§cDer Erstellungsprozess wurde abgebrochen, da der Tag maximal 4 Zeichen lang sein darf!");
            }
        }
    }

    public class CreateClanInput extends FixedSetPrompt{

        public CreateClanInput(){
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
                return ClanCreateConversation.getMessagePromt("§cDu hast die Clan Erstellung abgebrochen!");
            }else{
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("Willst du den §b"+((String) conversationContext.getSessionData("name"))+" §7[§b"+((String) conversationContext.getSessionData("tag"))+"§7] Clan für §b1.000.000€ §7erstellen? (§2Ja §7| §cNein§7)");
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

            String name = ((String) conversationContext.getSessionData("name"));
            String tag = ((String) conversationContext.getSessionData("tag"));

            if(new CashService().getValue(player.getUniqueId()) >= 1000000){

                if(new ClanService().existClanName(name) || new ClanService().existClanTag(tag)){
                    return "§cEin Clan mit diesem Namen oder diesem tag exestiert bereits!";
                }else{
                    if(!new ClanPlayerService().isInClan(player.getUniqueId())) {
                        new CashService().removeValue(player.getUniqueId(), 1000000);
                        new ClanService().createClan(player.getUniqueId(), name, tag);
                        return XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("§7Du hast den Clan §b" + name + " §7[§b" + tag + "§7] erfolgreich erstellt. Wenn du nun Hilfe benötigst kannst du dich im Tutorialcenter oder bei /clan help informieren!");
                    }else{
                        return XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString("§cDu bist bereits in einem Clan. Deswegen kannst du keinen Clan erstellen!");
                    }
                    }
            }else{
                return "§cDazu hast du zur Zeit noch zu wenig Geld!";
            }
        }
    }

    public static MessagePrompt getMessagePromt(String message){
        return new MessagePrompt() {
            @Override
            protected @Nullable Prompt getNextPrompt(@NotNull ConversationContext conversationContext) {
                return END_OF_CONVERSATION;
            }

            @Override
            public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
                return XDevApi.getInstance().getxUtil().getStringUtil().fullyFormattedString(message);
            }
        };
    }
}
