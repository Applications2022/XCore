package de.ruben.xcore.stock.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.ruben.xcore.currency.service.CashService;
import de.ruben.xcore.stock.XStocks;
import de.ruben.xcore.stock.model.*;
import de.ruben.xcore.stock.service.HoldingService;
import de.ruben.xcore.stock.service.StockContainerFetcher;
import de.ruben.xdevapi.XDevApi;
import de.ruben.xdevapi.custom.gui.ItemPreset;
import de.ruben.xdevapi.custom.gui.LabyGUITemplate;
import de.ruben.xdevapi.custom.gui.NoLabyGUITemplate;
import de.ruben.xdevapi.custom.gui.response.ConfirmationResponse;
import de.ruben.xdevapi.performance.concurrent.TaskBatch;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SellCryptoGui extends Gui {
    private String stockSymbol;
    private StockType stockType;

    public SellCryptoGui(String stockSymbol, StockType stockType) {
        super(4, "§9Aktien §8| §9Crypto-Handel", Set.of(InteractionModifier.PREVENT_ITEM_PLACE, InteractionModifier.PREVENT_ITEM_SWAP, InteractionModifier.PREVENT_ITEM_TAKE));

        this.stockSymbol = stockSymbol;
        this.stockType = stockType;

    }

    @Override
    public void open(@NotNull HumanEntity player) {

        this.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        this.getFiller().fill(ItemPreset.fillItem(event -> {
        }));

        this.setItem(31, ItemPreset.closeItem(event -> {
            this.close(player);
        }));

        this.setItem(27, ItemPreset.backItem(Material.ARROW, "§9Zurück", event -> {
            new BuySellGui((Player) player, stockType, stockSymbol).open(player);
        }));

        if (XStocks.getInstance().getRecentStockCache().containsKey(stockSymbol)) {
            StockContainer stockContainer = XStocks.getInstance().getRecentStockCache().get(stockSymbol);

            double amount = new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol);
            double price = stockContainer.getRegularMarketPrice();

            this.setItem(13, ItemBuilder.from(getInfoStack(price, amount)).asGuiItem());

            this.setItem(15,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§cVerkaufen §7(Eingabe in Euro)"))
                            .asGuiItem(event -> {

                                if (XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())) {

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel Euro du die Crypto-Währung verkaufen willst", "Eingabe in EUR", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if (!input.equals("") && !input.equals(" ") && !input.isEmpty()) {
                                                if (isDouble(input)) {
                                                    double sellAmount = Double.parseDouble(input) / stockContainer.getRegularMarketPrice();

                                                    if(Double.parseDouble(input) <= 0){
                                                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                        return;
                                                    }

                                                    double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if (confirmationResponse != ConfirmationResponse.NO) {
                                                            CashService cashService = new CashService();
                                                            HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                            if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                                cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                    holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                            + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));
                                                                });

                                                            } else {
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                            }

                                                        }
                                                    });


                                                } else {
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                } else {
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Euro"
                                            , "Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double sellAmount = aDouble / stockContainer.getRegularMarketPrice();

                                                if(aDouble <= 0){
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                    return AnvilGUI.Response.close();
                                                }

                                                double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();
                                                    if (confirmationResponse != ConfirmationResponse.NO) {
                                                        CashService cashService = new CashService();
                                                        HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                        if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                            cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                        + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));
                                                            });

                                                        } else {
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
            );

            this.setItem(11,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§cVerkaufen §7(Eingabe in Crypto)"))
                            .asGuiItem(event -> {

                                if (XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())) {

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel Euro du die Crypto-Währung verkaufen willst", "Eingabe in EUR", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if (!input.equals("") && !input.equals(" ") && !input.isEmpty()) {
                                                if (isDouble(input)) {
                                                    double sellAmount = Double.parseDouble(input);

                                                    if(sellAmount <= 0){
                                                        player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                        return;
                                                    }

                                                    double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if (confirmationResponse != ConfirmationResponse.NO) {
                                                            CashService cashService = new CashService();
                                                            HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                            if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                                cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                    holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                            + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));
                                                                });

                                                            } else {
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                            }

                                                        }
                                                    });


                                                } else {
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                } else {
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Euro"
                                            , "Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double sellAmount = aDouble;

                                                if(aDouble <= 0){
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§cFehler: §7Bitte gebe eine höhere Zahl an!");
                                                    return AnvilGUI.Response.close();
                                                }

                                                double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();
                                                    if (confirmationResponse != ConfirmationResponse.NO) {
                                                        CashService cashService = new CashService();
                                                        HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                        if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                            cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                        + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));
                                                            });

                                                        } else {
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
            );

            this.setItem(35, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE).name(Component.text("§cAlles Verkaufen")).asGuiItem(event -> {
                double holdingAmount = new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol);

                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf deiner Crypto-Währung Bestätigen", (gui, confirmationResponse, player1) -> {

                    if(confirmationResponse == ConfirmationResponse.NO){
                        player1.closeInventory();
                    }else if(confirmationResponse == ConfirmationResponse.YES){
                        player1.closeInventory();

                        player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Alle Cryptos werden verkauft...");

                        XDevApi.getInstance().getxScheduler().async(() -> {
                            if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol())){
                                StockContainer stockContainer1 = XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol());

                                double payAmount = stockContainer1.getRegularMarketPrice()*holdingAmount;
                                double recentAmount = holdingAmount;

                                new CashService().addValue(player1.getUniqueId(), payAmount, cashAccount -> {
                                    HoldingService holdingService = new HoldingService(player1.getUniqueId());

                                    holdingService.removeHoldingAmount(stockContainer1.getSymbol(), recentAmount);

                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"+amount+" "+stockContainer1.getSymbol()+" "+stockContainer1.getSymbol()+" §7 für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(payAmount)+"€ §7verkauft!");

                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), recentAmount, payAmount, HoldingHistoryType.SOLD));
                                });

                            }
                        });
                    }

                });

            }));


        } else {
            this.setItem(11, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            this.setItem(13, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());
            this.setItem(15, ItemBuilder.from(Material.CLOCK).name(Component.text("§7Lade...")).asGuiItem());

            fetchStock((Player) player);
        }
        super.open(player);
    }

    private void fetchStock(Player player) {
        StockContainerFetcher.fetchContainer(stockSymbol, stockContainer -> {

            double amount = new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol);
            double price = stockContainer.getRegularMarketPrice();

            this.updateItem(13, ItemBuilder.from(getInfoStack(stockContainer.getRegularMarketPrice(), new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol))).asGuiItem());

            this.setItem(15,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§cVerkaufen §7(Eingabe in Euro)"))
                            .asGuiItem(event -> {

                                if (XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())) {

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel Euro du die Crypto-Währung verkaufen willst", "Eingabe in EUR", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if (!input.equals("") && !input.equals(" ") && !input.isEmpty()) {
                                                if (isDouble(input)) {
                                                    double sellAmount = Double.parseDouble(input) / stockContainer.getRegularMarketPrice();
                                                    double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if (confirmationResponse != ConfirmationResponse.NO) {
                                                            CashService cashService = new CashService();
                                                            HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                            if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                                cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                    holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                            + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));
                                                                });

                                                            } else {
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                            }

                                                        }
                                                    });


                                                } else {
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                } else {
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Euro"
                                            , "Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double sellAmount = aDouble / stockContainer.getRegularMarketPrice();
                                                double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI(player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();
                                                    if (confirmationResponse != ConfirmationResponse.NO) {
                                                        CashService cashService = new CashService();
                                                        HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                        if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                            cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                        + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));
                                                            });

                                                        } else {
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
            );

            this.setItem(11,
                    ItemBuilder
                            .from(Material.SUNFLOWER)
                            .name(Component.text("§cVerkaufen §7(Eingabe in Crypto)"))
                            .asGuiItem(event -> {

                                if (XDevApi.getInstance().getLabyUsers().isLabyUser(player.getUniqueId())) {

                                    LabyGUITemplate.createInput((Player) player, "Gebe an, für wie viel Euro du die Crypto-Währung verkaufen willst", "Eingabe in EUR", 32, (uuid, s) -> {

                                        try {
                                            String input = new ObjectMapper().readTree(s).get("value").asText();

                                            if (!input.equals("") && !input.equals(" ") && !input.isEmpty()) {
                                                if (isDouble(input)) {
                                                    double sellAmount = Double.parseDouble(input);
                                                    double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                    NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                        player1.closeInventory();
                                                        if (confirmationResponse != ConfirmationResponse.NO) {
                                                            CashService cashService = new CashService();
                                                            HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                            if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                                cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                    holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                            + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));
                                                                });

                                                            } else {
                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                            }

                                                        }
                                                    });


                                                } else {
                                                    player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!");
                                                }
                                            }
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    });

                                } else {
                                    NoLabyGUITemplate.createDoubleInputGUI(Locale.GERMANY
                                            , XDevApi.getInstance()
                                            , "§9Eingabe in Euro"
                                            , "Eingabe"
                                            , XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Bitte gebe eine Kommazahl als Input an!"
                                            , aDouble -> {

                                                double sellAmount = aDouble;
                                                double money = stockContainer.getRegularMarketPrice() * sellAmount;

                                                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf bestätigen", (gui, confirmationResponse, player1) -> {
                                                    player1.closeInventory();
                                                    if (confirmationResponse != ConfirmationResponse.NO) {
                                                        CashService cashService = new CashService();
                                                        HoldingService holdingService = new HoldingService(player.getUniqueId());

                                                        if (holdingService.getHoldingAmount(stockSymbol) >= sellAmount) {

                                                            cashService.addValue(player.getUniqueId(), money, cashAccount -> {
                                                                holdingService.removeHoldingAmount(stockSymbol, sellAmount);

                                                                player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Du hast erfolgreich §b"
                                                                        + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(sellAmount) + " " + stockSymbol + " §7für §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(money) + "€ §7verkauft!");

                                                                holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), sellAmount, money, HoldingHistoryType.SOLD));

                                                            });

                                                        } else {
                                                            player.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix") + "§7Dazu hast du zu wenig der Crypto-Währung!");
                                                        }

                                                    }
                                                });

                                                return AnvilGUI.Response.close();
                                            }).open((Player) player);
                                }

                            })
            );

            this.setItem(35, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE).name(Component.text("§cAlles Verkaufen")).asGuiItem(event -> {
                double holdingAmount = new HoldingService(player.getUniqueId()).getHoldingAmount(stockSymbol);

                NoLabyGUITemplate.createStandardSelectConfirmationGUI((Player) player, "§cVerkauf deiner Crypto-Währung Bestätigen", (gui, confirmationResponse, player1) -> {

                    if(confirmationResponse == ConfirmationResponse.NO){
                        player1.closeInventory();
                    }else if(confirmationResponse == ConfirmationResponse.YES){
                        player1.closeInventory();

                        player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Alle Cryptos werden verkauft...");

                        XDevApi.getInstance().getxScheduler().async(() -> {
                            if(XStocks.getInstance().getRecentStockCache().containsKey(stockContainer.getSymbol())){
                                StockContainer stockContainer1 = XStocks.getInstance().getRecentStockCache().get(stockContainer.getSymbol());

                                double payAmount = stockContainer1.getRegularMarketPrice()*holdingAmount;
                                double recentAmount = holdingAmount;

                                new CashService().addValue(player1.getUniqueId(), payAmount, cashAccount -> {
                                    HoldingService holdingService = new HoldingService(player1.getUniqueId());

                                    holdingService.removeHoldingAmount(stockContainer1.getSymbol(), recentAmount);

                                    player1.sendMessage(XDevApi.getInstance().getMessageService().getMessage("prefix")+"§7Du hast erfolgreich §b"+amount+" "+stockContainer1.getSymbol()+" "+stockContainer1.getSymbol()+" §7 für §b"+XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(payAmount)+"€ §7verkauft!");

                                    holdingService.addHoldingHistory(new HoldingHistory(new Date(System.currentTimeMillis()), stockSymbol, stockContainer.getStockType(), recentAmount, payAmount, HoldingHistoryType.SOLD));
                                });

                            }
                        });
                    }

                });

            }));

        });
    }

    private ItemStack getInfoStack(Double price, double amount) {
        ItemStack itemStack = ItemBuilder
                .from(Material.TORCH)
                .name(Component.text("§bInfo:"))
                .lore(
                        Component.text(" "),
                        Component.text("§7➥ Aktuell im Besitz: §b" + XStocks.getInstance().getStockFormat().format(amount)),
                        Component.text("§7➥ Einzelner Verkaufspreis: §b" + XDevApi.getInstance().getxUtil().getStringUtil().moneyFormat(price) + "€"),
                        Component.text(" ")
                )
                .build();

        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setDouble("amount", amount);
        nbtItem.setDouble("price", price);

        return nbtItem.getItem();
    }

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}