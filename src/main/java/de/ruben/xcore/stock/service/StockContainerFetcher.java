package de.ruben.xcore.stock.service;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.ruben.xcore.stock.model.StockContainer;
import de.ruben.xcore.stock.model.StockType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.function.Consumer;

public class StockContainerFetcher {

    public static void fetchContainer(String symbol, Consumer<StockContainer> callback){
        OkHttpClient okHttpClient = new OkHttpClient();

        symbol = symbol.replace("?", "-");

        Request request = new Request.Builder()
                .url("https://query1.finance.yahoo.com/v7/finance/options/"+symbol.toUpperCase())
                .build();

        String finalSymbol = symbol;
        okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    System.err.println("Fehleler! Yahoo Finance ist nicht erreichbar!!");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String json = response.body().string();

                    try {
                        Object responseObject = new JSONParser().parse(json);

                        JSONObject jsonObject = (JSONObject) responseObject;

                        JSONObject optionChainObject = (JSONObject) jsonObject.get("optionChain");

                        if(((JSONArray) optionChainObject.get("result")).isEmpty()){
                            System.out.println("Quote has no Data: "+ finalSymbol);
                            callback.accept(null);
                            return;
                        }

                        JSONObject resultDataObject = (JSONObject) ((JSONArray) optionChainObject.get("result")).get(0);


                        JSONObject quoteData = (JSONObject) resultDataObject.get("quote");

                        if(quoteData.get("quoteType").toString().equals("MUTUALFUND") || quoteData.get("quoteType").toString().equals("CURRENCY")){
                            callback.accept(null);
                            return;
                        }

                        if(!(String.valueOf(quoteData.get("currency")).equalsIgnoreCase("USD") || String.valueOf(quoteData.get("currency")).equalsIgnoreCase("EUR"))){
                            callback.accept(null);
                            return;
                        }

                        StockContainer stockContainer = new StockContainer();
                        stockContainer.setChange((Double) quoteData.get("regularMarketChange"));
                        stockContainer.setCurrency(String.valueOf(quoteData.get("currency")));
                        stockContainer.setDisplayName(String.valueOf(quoteData.get("displayName")));
                        stockContainer.setLoadTime(System.currentTimeMillis());
                        stockContainer.setRegularMarketPreviousClose((Double) quoteData.get("regularMarketPreviousClose"));
                        stockContainer.setRegularMarketPrice((Double) quoteData.get("regularMarketPrice"));
                        stockContainer.setShortName(String.valueOf(quoteData.get("shortName")));
                        StockType stockType = String.valueOf(quoteData.get("quoteType")).equals("CRYPTOCURRENCY") ? StockType.CRYPTOCURRENCY : StockType.STOCK;
                        stockContainer.setStockType(stockType);
                        stockContainer.setSymbol(String.valueOf(quoteData.get("symbol")));

                        callback.accept(stockContainer);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });


    }

    public static void fetchCurrentUSDEURExchange(Consumer<Double> callback){
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://query1.finance.yahoo.com/v7/finance/options/USDEUR=X")
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                System.err.println("Fehleler! Yahoo Finance ist nicht erreichbar!!");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Object responseObject = null;
                try {
                    responseObject = new JSONParser().parse(response.body().string());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                JSONObject jsonObject = (JSONObject) responseObject;

                JSONObject optionChainObject = (JSONObject) jsonObject.get("optionChain");

                if(((JSONArray) optionChainObject.get("result")).isEmpty()){
                    callback.accept(null);
                    return;
                }

                JSONObject resultDataObject = (JSONObject) ((JSONArray) optionChainObject.get("result")).get(0);


                JSONObject quoteData = (JSONObject) resultDataObject.get("quote");

                callback.accept((Double) quoteData.get("regularMarketPrice"));
            }
        });
    }

}
