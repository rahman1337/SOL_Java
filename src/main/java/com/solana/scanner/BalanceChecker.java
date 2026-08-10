package com.solana.scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class BalanceChecker {
    
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    
    /**
     * Get balance of Solana address using RPC
     */
    public static double getBalance(String address, String rpcUrl) throws Exception {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("jsonrpc", "2.0");
            payload.addProperty("id", 1);
            payload.addProperty("method", "getBalance");
            
            com.google.gson.JsonArray paramArray = new com.google.gson.JsonArray();
            paramArray.add(address);
            payload.add("params", paramArray);
            
            RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json"));
            
            Request request = new Request.Builder()
                    .url(rpcUrl)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return 0.0;
                }
                
                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                
                if (jsonResponse.has("result")) {
                    long lamports = jsonResponse.get("result").getAsLong();
                    return lamports / 1_000_000_000.0; // Convert lamports to SOL
                }
                
                return 0.0;
            }
            
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Check multiple addresses and return balances
     */
    public static double[] getMultipleBalances(String[] addresses, String rpcUrl) throws Exception {
        double[] balances = new double[addresses.length];
        
        for (int i = 0; i < addresses.length; i++) {
            balances[i] = getBalance(addresses[i], rpcUrl);
            
            // Rate limiting
            Thread.sleep(50);
        }
        
        return balances;
    }
}
