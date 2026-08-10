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
                if (!response.isSuccessful()) {
                    throw new Exception("RPC error: HTTP " + response.code());
                }
                
                if (response.body() == null) {
                    throw new Exception("RPC returned empty response");
                }
                
                String responseBody = response.body().string();
                
                // Check for RPC errors
                if (responseBody.contains("error")) {
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (jsonResponse.has("error")) {
                        throw new Exception("RPC error: " + jsonResponse.get("error").toString());
                    }
                }
                
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                
                if (jsonResponse.has("result")) {
                    long lamports = jsonResponse.get("result").getAsLong();
                    return lamports / 1_000_000_000.0; // Convert lamports to SOL
                }
                
                throw new Exception("No result in RPC response");
            }
            
        } catch (Exception e) {
            throw new Exception("Balance check failed for " + address + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Check multiple addresses and return balances
     */
    public static double[] getMultipleBalances(String[] addresses, String rpcUrl) throws Exception {
        double[] balances = new double[addresses.length];
        
        for (int i = 0; i < addresses.length; i++) {
            try {
                balances[i] = getBalance(addresses[i], rpcUrl);
            } catch (Exception e) {
                System.err.println("Error checking balance for " + addresses[i] + ": " + e.getMessage());
                balances[i] = 0.0;
            }
            
            // Rate limiting
            Thread.sleep(50);
        }
        
        return balances;
    }
}
