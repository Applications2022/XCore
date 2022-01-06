package de.ruben.xcore.stock.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.Document;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoldingPlayer {

    private UUID uuid;
    private Map<String, Holding> holdings;
    private List<HoldingHistory> history;

    public Document toDocument(){
        Document document = new Document("_id", uuid);

        Map<String, Object> holdingsAsDocument = getHoldings().entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey().replace(".", "~"),
                o -> o.getValue().toDocument()
        ));


        document.append("holdings", new Document(holdingsAsDocument));
        document.append("history", history.stream().map(holdingHistory -> holdingHistory.toDocument()).collect(Collectors.toList()));

        return document;
    }

    public HoldingPlayer fromDocument(Document document) {
        this.uuid = document.get("_id", UUID.class);
        this.holdings = document.get("holdings", Document.class).entrySet().stream().collect(Collectors.toMap(
                o -> o.getKey().replace("~", "."),
                o -> new Holding().fromDocument(((Document) o.getValue()))
        ));

        this.history = document.getList("history", Document.class)
                .stream()
                .map(o -> new HoldingHistory().fromDocument((o)))
                .collect(Collectors.toList());

        return this;
    }

    public List<HoldingHistory> getHistorySorted() {
        return history
                .stream()
                .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
                .collect(Collectors.toList());
    }

    public Map<String, Holding> getHoldings(HoldingSortType holdingSortType) {
        switch (holdingSortType){
            case ALL:
                return getHoldings();
            case CRYPTOONLY:
                return getHoldings().entrySet().stream().filter(stringHoldingEntry -> stringHoldingEntry.getValue().getStockType() == StockType.CRYPTOCURRENCY).collect(Collectors.toMap(stringHoldingEntry -> stringHoldingEntry.getKey(), o -> o.getValue()));
            case STOCKONLY:
                return getHoldings().entrySet().stream().filter(stringHoldingEntry -> stringHoldingEntry.getValue().getStockType() == StockType.STOCK).collect(Collectors.toMap(stringHoldingEntry -> stringHoldingEntry.getKey(), o -> o.getValue()));
            default:
                return new HashMap<>();
        }
    }

}
