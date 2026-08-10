package com.solana.scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class BalanceChecker {
    
    // Multiple RPC endpoints for failover
    private static final String[] RPC_URLS = {
        "https://solana-rpc.publicnode.com",
        "https://api.mainnet-beta.solana.com",
        "https://rpc.ankr.com/solana",
        "https://solana-api.projectserum.com"
    };
    
    private static int currentRpcIndex = 0;
    private static final Object rpcLock = new Object();
    
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    
    /**
     * Get balance of Solana address using RPC with automatic failover
     */
    public static double getBalance(String address, String primaryRpcUrl) throws Exception {
        int maxRetries = 3;
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                // Try primary RPC first, then fallback to alternatives
                String rpcUrl = (attempt == 0) ? primaryRpcUrl : getNextRpcUrl();
                return checkBalanceOnRpc(address, rpcUrl);
                
            } catch (Exception e) {
                lastException = e;
                
                // Log retry attempt
                if (attempt < maxRetries - 1) {
                    System.err.println("[RETRY] Attempt " + (attempt + 1) + "/" + maxRetries + " failed, retrying...");
                    Thread.sleep(500 * (attempt + 1)); // Exponential backoff
                }
            }
        }
        
        // All retries failed
        throw new Exception("Balance check failed after " + maxRetries + " attempts: " + lastException.getMessage(), lastException);
    }
    
    /**
     * Check balance on specific RPC endpoint
     */
    private static double checkBalanceOnRpc(String address, String rpcUrl) throws Exception {
        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("jsonrpc", "2.0");
            payload.addProperty("id", System.nanoTime());
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
                
                // Check HTTP status
                if (!response.isSuccessful()) {
                    throw new Exception("HTTP " + response.code() + " from " + rpcUrl);
                }
                
                if (response.body() == null) {
                    throw new Exception("Empty response from RPC");
                }
                
                String responseBody = response.body().string();
                
                // Parse JSON
                JsonObject jsonResponse;
                try {
                    jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                } catch (Exception e) {
                    throw new Exception("Invalid JSON response: " + responseBody.substring(0, Math.min(100, responseBody.length())));
                }
                
                // Check for RPC error response
                if (jsonResponse.has("error")) {
                    JsonObject error = jsonResponse.getAsJsonObject("error");
                    String errorMsg = error.has("message") ? error.get("message").getAsString() : error.toString();
                    throw new Exception("RPC Error: " + errorMsg);
                }
                
                // Extract result
                if (!jsonResponse.has("result")) {
                    throw new Exception("No result in RPC response");
                }
                
                long lamports = jsonResponse.get("result").getAsLong();
                return lamports / 1_000_000_000.0; // Convert lamports to SOL
            }
            
        } catch (java.net.SocketTimeoutException e) {
            throw new Exception("RPC timeout on " + rpcUrl, e);
        } catch (java.net.ConnectException e) {
            throw new Exception("Connection failed to " + rpcUrl, e);
        } catch (Exception e) {
            throw new Exception("RPC check failed on " + rpcUrl + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Get next available RPC URL for failover
     */
    private static String getNextRpcUrl() {
        synchronized (rpcLock) {
            String url = RPC_URLS[currentRpcIndex];
            currentRpcIndex = (currentRpcIndex + 1) % RPC_URLS.length;
            return url;
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
